package com.parasol.BaaS.service;

import com.parasol.BaaS.api_model.AccountFormattedHistory;
import com.parasol.BaaS.api_model.AccountInfo;
import com.parasol.BaaS.api_param.*;
import com.parasol.BaaS.api_request.*;
import com.parasol.BaaS.api_response.*;
import com.parasol.BaaS.auth.jwt.UserDetail;
import com.parasol.BaaS.db.entity.BankConnection;
import com.parasol.BaaS.db.entity.User;
import com.parasol.BaaS.db.repository.BankConnectionRepository;
import com.parasol.BaaS.db.repository.UserRepository;
import com.parasol.BaaS.modules.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.TimeZone;
import java.util.stream.Collectors;

@Service
public class AccountService {

    @Autowired
    QueryAccountBalanceRequestFactory queryAccountBalanceRequestFactory;

    @Autowired
    QueryAccountListRequestFactory queryAccountListRequestFactory;

    @Autowired
    QueryAccountHistoryRequestFactory queryAccountHistoryRequestFactory;

    @Autowired
    DepositRequestFactory depositRequestFactory;

    @Autowired
    WithdrawRequestFactory withdrawRequestFactory;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BankConnectionRepository bankConnectionRepository;

    public Mono<QueryAccountBalanceResponse> getBalance(
            QueryAccountBalanceRequest request
    ) {
        Authentication authentication = request.getAuthentication();

        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        UserDetail userDetail = (UserDetail) authentication.getDetails();
        String id = userDetail.getUsername();

        if (!StringUtils.hasText(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findByUserId(id)
                .orElseThrow(() -> { throw new ResponseStatusException(HttpStatus.NOT_FOUND); });

        String bankName = request.getBankName();
        String bankAccountNumber = request.getBankAccountNumber();

        BankConnection bankConnection = getBankConnection(user, bankName);

        if (!bankName.equals("SBJ"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "We can support SBJ Bank only.");

        QueryAccountBalanceParam param = QueryAccountBalanceParam.builder()
                .accountNumber(bankAccountNumber)
                .id(bankConnection.getBankId())
                .password(bankConnection.getBankPassword())
                .build();

        return queryAccountBalanceRequestFactory.create(param)
                .doOnError( (throwable) -> {
                    WebClientResponseException ex = (WebClientResponseException)throwable;

                    if (ex.getStatusCode().is4xxClientError())
                        throw new ResponseStatusException(ex.getStatusCode());
                    else if (ex.getStatusCode().is5xxServerError())
                        throw new ResponseStatusException(ex.getStatusCode());
                })
                .map(result -> QueryAccountBalanceResponse.builder()
                        .bankName(bankName)
                        .bankAccountNumber(bankAccountNumber)
                        .bankAccountBalance(result.getBalance())
                        .build()
                );
    }

    public Mono<QueryAccountListResponse> getAccountList(
            QueryAccountListRequest request
    ) throws IllegalStateException, IllegalArgumentException, AccessDeniedException, NoSuchElementException {
        Authentication authentication = request.getAuthentication();

        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "give me a token");
        }

        UserDetail userDetail = (UserDetail) authentication.getDetails();
        String id = userDetail.getUsername();

        if (!StringUtils.hasText(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findByUserId(id)
                .orElseThrow(() -> { throw new ResponseStatusException(HttpStatus.NOT_FOUND); });

        String bankName = request.getBankName();

        BankConnection bankConnection = getBankConnection(user, bankName);

        if (!bankName.equals("SBJ"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "We can support SBJ Bank only.");

        QueryAccountListParam param = QueryAccountListParam.builder()
                .id(bankConnection.getBankId())
                .password(bankConnection.getBankPassword())
                .build();

        return queryAccountListRequestFactory.create(param)
                .doOnError( (throwable) -> {
                    WebClientResponseException ex = (WebClientResponseException)throwable;

                    if (ex.getStatusCode().is4xxClientError())
                        throw new ResponseStatusException(ex.getStatusCode());
                    else if (ex.getStatusCode().is5xxServerError())
                        throw new ResponseStatusException(ex.getStatusCode());
                })
                .map(result -> QueryAccountListResponse.builder()
                        .bankName(bankName)
                        .bankAccounts(result.getAccounts())
                        .build()
                );
    }

    public Mono<QueryAccountHistoryResponse> getAccountHistory(
            QueryAccountHistoryRequest request
    ) throws IllegalStateException, IllegalArgumentException, AccessDeniedException, NoSuchElementException {
        Authentication authentication = request.getAuthentication();

        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "give me a token");
        }

        UserDetail userDetail = (UserDetail) authentication.getDetails();
        String id = userDetail.getUsername();

        if (!StringUtils.hasText(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findByUserId(id)
                .orElseThrow(() -> { throw new ResponseStatusException(HttpStatus.NOT_FOUND); });

        String bankName = request.getBankName();
        String bankAccountNumber = request.getBankAccountNumber();

        BankConnection bankConnection = getBankConnection(user, bankName);

        if (!bankName.equals("SBJ"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "We can support SBJ Bank only.");

        QueryAccountHistoryParam param = QueryAccountHistoryParam.builder()
                .accountNumber(bankAccountNumber)
                .id(bankConnection.getBankId())
                .password(bankConnection.getBankPassword())
                .build();

        return queryAccountHistoryRequestFactory.create(param)
                .doOnError( (throwable) -> {
                    WebClientResponseException ex = (WebClientResponseException)throwable;

                    if (ex.getStatusCode().is4xxClientError())
                        throw new ResponseStatusException(ex.getStatusCode());
                    else if (ex.getStatusCode().is5xxServerError())
                        throw new ResponseStatusException(ex.getStatusCode());
                })
                .map(result -> QueryAccountHistoryResponse.builder()
                        .bankName(bankName)
                        .bankAccountNumber(bankAccountNumber)
                        .bankAccountHistories(
                                result.getAccountHistories()
                                        .stream()
                                        .map(history ->
                                            AccountFormattedHistory.builder()
                                                    .txId(history.getId())
                                                    .txDateTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(history.getDate()), TimeZone.getTimeZone("Asia/Seoul").toZoneId()))
                                                    .type(history.getType())
                                                    .amount(history.getAmount())
                                                    .build()
                                        )
                                        .collect(Collectors.toList())
                        )
                        .build()
                );
    }

    public Mono<DepositResponse> deposit(
            DepositRequest request
    ) throws IllegalStateException, IllegalArgumentException, AccessDeniedException, NoSuchElementException {
        String bankName = request.getBankName();
        Long amount = request.getAmount();
        String nameFrom = request.getNameFrom();
        AccountInfo accountTo = request.getAccountTo();

        if (!bankName.equals("SBJ"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "We can support SBJ Bank only.");

        DepositParam param = DepositParam.builder()
                .amount(amount)
                .accountTo(accountTo)
                .nameFrom(nameFrom)
                .build();

        return depositRequestFactory.create(param)
                .doOnError( (throwable) -> {
                    WebClientResponseException ex = (WebClientResponseException)throwable;

                    if (ex.getStatusCode().is4xxClientError())
                        throw new ResponseStatusException(ex.getStatusCode());
                    else if (ex.getStatusCode().is5xxServerError())
                        throw new ResponseStatusException(ex.getStatusCode());
                })
                .map(result -> DepositResponse.builder()
                        .amount(amount)
                        .nameFrom(nameFrom)
                        .accountTo(accountTo)
                        .build()
                );
    }

    public Mono<WithdrawResponse> withdraw(
            WithdrawRequest request
    ) throws IllegalStateException, IllegalArgumentException, AccessDeniedException, NoSuchElementException {
        Authentication authentication = request.getAuthentication();

        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "give me a token");
        }

        UserDetail userDetail = (UserDetail) authentication.getDetails();
        String id = userDetail.getUsername();

        if (!StringUtils.hasText(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findByUserId(id)
                .orElseThrow(() -> { throw new ResponseStatusException(HttpStatus.NOT_FOUND); });

        String bankName = request.getBankName();
        String accountPassword = request.getBankAccountPassword();
        Long amount = request.getAmount();
        String nameTo = request.getNameTo();
        AccountInfo accountFrom = request.getAccountFrom();

        BankConnection bankConnection = getBankConnection(user, bankName);

        if (!bankName.equals("SBJ"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "We can support SBJ Bank only.");

        WithdrawParam param = WithdrawParam.builder()
                .accountPassword(accountPassword)
                .amount(amount)
                .accountFrom(accountFrom)
                .nameTo(nameTo)
                .id(bankConnection.getBankId())
                .password(bankConnection.getBankPassword())
                .build();

        return withdrawRequestFactory.create(param)
                .doOnError( (throwable) -> {
                    WebClientResponseException ex = (WebClientResponseException)throwable;

                    if (ex.getStatusCode().is4xxClientError())
                        throw new ResponseStatusException(ex.getStatusCode());
                    else if (ex.getStatusCode().is5xxServerError())
                        throw new ResponseStatusException(ex.getStatusCode());
                })
                .map(result -> WithdrawResponse.builder()
                        .amount(amount)
                        .nameTo(nameTo)
                        .accountFrom(accountFrom)
                        .build()
                );
    }

    public BankConnection getBankConnection(User user, String bankName) throws IllegalStateException {
        return bankConnectionRepository
                .findByUser_UserSeqAndBankName(user.getUserSeq(), bankName)
                .orElseThrow(() -> { throw new ResponseStatusException(HttpStatus.NOT_FOUND); });
    }

}
