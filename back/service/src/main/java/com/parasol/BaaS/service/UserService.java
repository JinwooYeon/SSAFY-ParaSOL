package com.parasol.BaaS.service;

import com.auth0.jwt.JWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parasol.BaaS.api_model.AccessToken;
import com.parasol.BaaS.api_model.AuthToken;
import com.parasol.BaaS.api_model.Password;
import com.parasol.BaaS.api_model.RefreshToken;
import com.parasol.BaaS.api_param.OAuthLoginParam;
import com.parasol.BaaS.api_request.*;
import com.parasol.BaaS.api_response.*;
import com.parasol.BaaS.api_result.GooglePayload;
import com.parasol.BaaS.api_result.OAuthLoginResult;
import com.parasol.BaaS.auth.jwt.UserDetail;
import com.parasol.BaaS.auth.jwt.util.JwtTokenUtil;
import com.parasol.BaaS.db.entity.*;
import com.parasol.BaaS.db.repository.*;
import com.parasol.BaaS.modules.OAuthRequestFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Random;

@Service
@Slf4j
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BankConnectionRepository bankConnectionRepository;

    @Autowired
    private BioInfoRepository bioInfoRepository;

    @Autowired
    private PayLedgerRepository payLedgerRepository;

    @Autowired
    private PayHistoryRepository payHistoryRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OAuthRequestFactory oAuthRequestFactory;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    @Value("${oauth.token.uri}")
    private String tokenUri;

    public Mono<LoginResponse> login(
            LoginRequest request
    ) throws IllegalArgumentException, NoSuchElementException {
        String id = request.getId();
        String password = request.getPassword();

        if (!StringUtils.hasText(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        if (!StringUtils.hasText(password)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        User user = getUserByUserId(id);

        if (!passwordEncoder.matches(password, user.getUserPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        AuthToken newToken = JwtTokenUtil.getToken(id);
        String newAccessToken = newToken.getAccessToken().getAccessToken();
        String newRefreshToken = newToken.getRefreshToken().getRefreshToken();

        Token savedToken = tokenRepository.findByUser_UserId(id)
                .orElse(
                        Token.builder()
                                .user(user)
                                .refreshToken(newRefreshToken)
                                .build()
                );
        savedToken.setRefreshToken(newRefreshToken);
        tokenRepository.save(savedToken);

        return Mono.just(
                LoginResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .build()
        );
    }

    public Mono<LoginResponse> loginOauth(
            LoginRequest request
    ) throws IllegalArgumentException, NoSuchElementException {
        String id = request.getId();

        if (!StringUtils.hasText(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        User user = getUserByUserId(id);

        if (userRepository.existsByUserId(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        AuthToken newToken = JwtTokenUtil.getToken(id);
        String newAccessToken = newToken.getAccessToken().getAccessToken();
        String newRefreshToken = newToken.getRefreshToken().getRefreshToken();

        Token savedToken = tokenRepository.findByUser_UserId(id)
                .orElse(
                        Token.builder()
                                .user(user)
                                .refreshToken(newRefreshToken)
                                .build()
                );
        savedToken.setRefreshToken(newRefreshToken);
        tokenRepository.save(savedToken);

        return Mono.just(
                LoginResponse.builder()
                        .accessToken(newAccessToken)
                        .refreshToken(newRefreshToken)
                        .build()
        );
    }

    public Mono<LoginResponse> loginOauthRedirect(
            OAuthLoginRequest request
    ) throws IllegalArgumentException, NoSuchElementException {
        String state = request.getState();
        String code = request.getCode();
        String scope = request.getScope();
        String authuser = request.getAuthuser();
        String prompt = request.getPrompt();

        OAuthLoginParam param = OAuthLoginParam.builder()
                .code(code)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .redirectUri(redirectUri)
                .grantType("authorization_code")
                .build();

        return oAuthRequestFactory.create(tokenUri, param)
                .map(result -> {
                            log.info("access_token:" + result.getAccessToken());
                            log.info("refresh_token:" + result.getRefreshToken());
                            log.info("id_token:" + result.getIdToken());

                            try {
                                String payloadJson = JWT.decode(result.getIdToken()).getPayload();
                                String payloadBytes = new String(Base64.decodeBase64URLSafe(payloadJson), StandardCharsets.UTF_8);
                                log.info(payloadBytes);

                                ObjectMapper mapper = new ObjectMapper();
                                GooglePayload userData = mapper.readValue(payloadBytes, GooglePayload.class);

                                User user = userRepository.findByUserId(userData.getEmail())
                                        .orElse(
                                            User.builder()
                                                    .userId(userData.getEmail())
                                                    .userName(userData.getName())
                                                    .userPassword(passwordEncoder.encode(userData.getAtHash()))
                                                    .build()
                                        );
                                userRepository.save(user);

                                PayLedger payLedger = payLedgerRepository.findByOwnerUserId(user.getUserId())
                                        .orElse(
                                                PayLedger.builder()
                                                        .owner(user)
                                                        .balance(0L)
                                                        .build()
                                        );

                                payLedgerRepository.save(payLedger);

                                Long userSeq = user.getUserSeq();
                                String userId = user.getUserId();

                                if (!StringUtils.hasText(userId)) {
                                    throw new ResponseStatusException(HttpStatus.NOT_FOUND);
                                }

                                AuthToken newToken = JwtTokenUtil.getToken(userId);
                                String newAccessToken = newToken.getAccessToken().getAccessToken();
                                String newRefreshToken = newToken.getRefreshToken().getRefreshToken();

                                Token savedToken = tokenRepository.findByUser_UserSeq(userSeq)
                                        .orElse(
                                                Token.builder()
                                                        .user(user)
                                                        .refreshToken(newRefreshToken)
                                                        .build()
                                        );
                                savedToken.setRefreshToken(newRefreshToken);
                                tokenRepository.save(savedToken);

                                return LoginResponse.builder()
                                        .accessToken(newAccessToken)
                                        .refreshToken(newRefreshToken)
                                        .build();
                            } catch (JsonProcessingException e) {
                                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
                            }
                        }
                );
    }

    public Mono<IdCheckResponse> idCheck(
            IdCheckRequest request
    ) throws IllegalArgumentException {
        String id = request.getId();

        if (!StringUtils.hasText(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        Optional<User> user = userRepository.findByUserId(id);
        if (user.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        return Mono.just(
                IdCheckResponse.builder()
                        .build()
        );
    }

    public Mono<PasswordResetResponse> resetPassword(
            PasswordResetRequest request
    ) throws IllegalArgumentException, NoSuchElementException {
        String id = request.getId();
        String name = request.getName();

        if (!StringUtils.hasText(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        if (!StringUtils.hasText(name)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        User user = getUserByUserId(id);

        if (!name.equals(user.getUserName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        String newPassword = createPassword();
        user.setUserPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return Mono.just(
                PasswordResetResponse.builder()
                        .password(newPassword)
                        .build()
        );
    }

    // AuthToken ?????????
    public Mono<ReissueTokenResponse> reissueAuthToken(
            ReissueTokenRequest request
    ) throws IllegalStateException, IllegalArgumentException, AccessDeniedException {
        Authentication authentication = request.getAuthentication();

        if (authentication == null) {
            throw new AccessDeniedException("give me a token");
        }

        UserDetail userDetail = (UserDetail) authentication.getDetails();

        String id = userDetail.getUsername();
        String refreshToken = authentication.getPrincipal().toString();

        Token savedToken = tokenRepository.findByUser_UserId(id)
                .orElseThrow(() -> { throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR); });

        String oldRefreshToken = savedToken.getRefreshToken();
        String message = JwtTokenUtil.handleError(oldRefreshToken);

        if(!StringUtils.hasText(message)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (!message.equals("success")) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (!oldRefreshToken.equals(refreshToken)) {
            throw new IllegalStateException();
        }

        AuthToken newToken = JwtTokenUtil.getToken(id);
        String newAccessToken = newToken.getAccessToken().getAccessToken();
        String newRefreshToken = newToken.getRefreshToken().getRefreshToken();

        savedToken.setRefreshToken(newRefreshToken);
        tokenRepository.save(savedToken);

        return Mono.just(
                ReissueTokenResponse.builder()
                        .accessToken(newAccessToken)
                        .refreshToken(newRefreshToken)
                        .build()
        );
    }

    public Mono<QueryUserInfoResponse> getUserInfo(
            QueryUserInfoRequest request
    ) throws IllegalStateException, NoSuchElementException, AccessDeniedException {
        Authentication authentication = request.getAuthentication();

        if (authentication == null) {
            throw new AccessDeniedException("give me a token");
        }

        UserDetail userDetail = (UserDetail) authentication.getDetails();
        String id = userDetail.getUsername();

        if (!StringUtils.hasText(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        User user = getUserByUserId(id);

        return Mono.just(
                QueryUserInfoResponse.builder()
                        .id(user.getUserId())
                        .name(user.getUserName())
                        .build()
        );
    }

    public Mono<RegisterResponse> createUser(
            RegisterRequest request
    ) throws IllegalArgumentException {
        String id = request.getId();
        String password = request.getPassword();
        String name = request.getName();

        if (!StringUtils.hasText(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        if (!StringUtils.hasText(password)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        if (!StringUtils.hasText(name)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        User user = User.builder()
                .userId(id)
                .userPassword(passwordEncoder.encode(password))
                .userName(name)
                .build();

        userRepository.save(user);

        PayLedger payLedger = PayLedger.builder()
                .owner(user)
                .balance(0L)
                .build();

        payLedgerRepository.save(payLedger);

        return Mono.just(
                RegisterResponse.builder()
                        .id(user.getUserId())
                        .name(user.getUserName())
                        .build()
        );
    }

    public Mono<UpdateResponse> updateUser(
            PasswordUpdateRequest request
    ) throws IllegalArgumentException, NoSuchElementException, AccessDeniedException {
        Authentication authentication = request.getAuthentication();

        if (authentication == null) {
            throw new AccessDeniedException("give me a token");
        }

        UserDetail userDetail = (UserDetail) authentication.getDetails();
        String id = userDetail.getUsername();

        String password = request.getPassword();
        String newPassword = request.getNewPassword();

        if(!StringUtils.hasText(password)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        if(!StringUtils.hasText(newPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        User user = getUserByUserId(id);

        if (!passwordEncoder.matches(password, user.getUserPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        if (passwordEncoder.matches(newPassword, user.getUserPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        user.setUserPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return Mono.just(
                UpdateResponse.builder()
                        .id(user.getUserId())
                        .name(user.getUserName())
                        .build()
        );
    }

    @Transactional
    public Mono<DeleteResponse> deleteUser(
            DeleteRequest request
    ) throws IllegalStateException, AccessDeniedException {
        Authentication authentication = request.getAuthentication();

        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "give me a token");
        }

        UserDetail userDetail = (UserDetail) authentication.getDetails();
        String id = userDetail.getUsername();

        Long bankConnectionDeleteResult = bankConnectionRepository.deleteByUser_UserId(id);
        Long bioInfoDeleteResult = bioInfoRepository.deleteByOwnerUserId(id);
        Long payHistoryDeleteResult = payHistoryRepository.deleteByUser_UserId(id);
        Long payLedgerDeleteResult = payLedgerRepository.deleteByOwnerUserId(id);
        Long tokenDeleteResult = tokenRepository.deleteByUserUserId(id);
        Long userDeleteResult = userRepository.deleteByUserId(id);

        return Mono.just(
                DeleteResponse.builder()
                        .build()
        );
    }

    // ?????? ???????????? 8??????
    public String createPassword(
    ) {
        StringBuilder password = new StringBuilder();
        Random rnd = new Random();

        for (int i = 0; i < 8; i++) { // 8??????
            int index = rnd.nextInt(3); // 0~2 ?????? ??????

            switch (index) {
                case 0:
                    password.append((char) ((int) (rnd.nextInt(26)) + 97));
                    //  a~z  (ex. 1+97=98 => (char)98 = 'b')
                    break;
                case 1:
                    password.append((char) ((int) (rnd.nextInt(26)) + 65));
                    //  A~Z
                    break;
                case 2:
                    password.append((rnd.nextInt(10)));
                    // 0~9
                    break;
            }
        }
        return password.toString();
    }

    public User getUserByUserId(
            String id
    ) throws NoSuchElementException {
        return userRepository.findByUserId(id)
                .orElseThrow(() -> { throw new ResponseStatusException(HttpStatus.NOT_FOUND); });
    }

}
