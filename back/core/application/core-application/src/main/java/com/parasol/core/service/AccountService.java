package com.parasol.core.service;

import com.parasol.core.VO.Balance;
import com.parasol.core.api_model.*;
import com.parasol.core.entity.Account;
import com.parasol.core.entity.Client;
import com.parasol.core.repository.AccountRepository;
import com.parasol.core.utils.AccountManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Validated
@Service
public class AccountService {
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private ValidationService validationService;
    @Autowired
    private ClientService clientService;
    @Autowired
    private TransactionHistoryService transactionHistoryService;


    public String Create(@Valid AccountOpenRequest accountOpenRequest) {
        Client client = clientService.findById(accountOpenRequest.getCusNo());
        Account account = new Account();

        if (client == null)
            return null;

        account.setClient(client);
        account.setPassword(accountOpenRequest.getAccountPassword());
        account.setId(AccountManager.GenerateAccountNumber());

        return accountRepository.save(account).getId();
    }


    public AccountListQueryResultResponse getAllAccount(@Valid AccountListQueryRequest request) {
        Client client = clientService.findById(request.getCusNo());
        AccountListQueryResultResponse listresult = new AccountListQueryResultResponse();
        List<AccountNumber> result = new ArrayList<>();

        for (Account e : accountRepository.findByClient(client)) {
            AccountNumber ele = new AccountNumber();
            AccountInfo accountInfo = new AccountInfo();

            accountInfo.setAccountNumber(e.getId());
            ele.setAccountNumber(accountInfo.getAccountNumber());
            result.add(ele);
        }

        listresult.setAccounts(result);

        return listresult;
    }

    public AccountBalanceQueryResultResponse getBalance(AccountQueryRequest request) {
        String accountNumber = request.getAccountNumber();

        Account account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> { throw new ResponseStatusException(HttpStatus.NOT_FOUND); });

        Long balance = account.getBalance();

        return AccountBalanceQueryResultResponse.builder()
                .balance(balance)
                .build();
    }

    @Transactional
    public DepositResponse deposit(@Valid DepositRequest request) {
        try {
            Long amount = request.getAmount();
            AccountInfo accountTo = request.getAccountTo();
            String nameFrom = request.getNameOpponent();

            if (accountTo == null) {
                throw new NullPointerException("AccountService :: deposit :: accountTo is null");
            }

            if (!StringUtils.hasText(nameFrom)) {
                throw new NullPointerException("AccountService :: deposit :: nameFrom is null");
            }

            String accountNumberTo = accountTo.getAccountNumber();
            Account depositAccount = accountRepository.findById(accountNumberTo)
                    .orElseThrow(IllegalStateException::new);

            Balance beforeBalance = new Balance(depositAccount.getBalance());
            Balance afterBalance = new Balance(depositAccount.getBalance() + amount);

            Long validAfterBalance = validationService.calculateBalance(afterBalance);

            depositAccount.setBalance(validAfterBalance);
            accountRepository.save(depositAccount);

            transactionHistoryService.createDepositHistory(accountNumberTo,
                    accountNumberTo,
                    nameFrom,
                    amount);

            return DepositResponse.builder()
                    .isSuccess(true)
                    .build();
        } catch (Exception ex) {
            ex.printStackTrace();

            return DepositResponse.builder()
                    .isSuccess(false)
                    .build();
        }
    }

    @Transactional
    public WithdrawResponse withdraw(@Valid WithdrawRequest request) {
        try {
            Long amount = request.getAmount();
            AccountInfo accountFrom = request.getAccountFrom();
            String nameTo = request.getNameOpponent();

            if (accountFrom == null) {
                throw new NullPointerException("AccountService :: withdraw :: accountFrom is null");
            }

            if (!StringUtils.hasText(nameTo)) {
                throw new NullPointerException("AccountService :: deposit :: nameTo is null");
            }

            String accountNumberFrom = accountFrom.getAccountNumber();
            Account withdrawAccount = accountRepository.findById(accountNumberFrom)
                    .orElseThrow(() -> { throw new ResponseStatusException(HttpStatus.FORBIDDEN); });

            validationService.equalPassword(request.getAccountPassword(), withdrawAccount.getPassword());

            Balance beforeBalance = new Balance(withdrawAccount.getBalance());
            Balance afterBalance = new Balance(withdrawAccount.getBalance() - amount);

            Long validAfterBalance = validationService.calculateBalance(afterBalance);

            withdrawAccount.setBalance(validAfterBalance);
            accountRepository.save(withdrawAccount);

            transactionHistoryService.createWithdrawHistory(accountNumberFrom,
                    accountNumberFrom,
                    nameTo,
                    amount);

            return WithdrawResponse.builder()
                    .isSuccess(true)
                    .build();
        } catch (Exception ex) {
            ex.printStackTrace();

            return WithdrawResponse.builder()
                    .isSuccess(false)
                    .build();
        }
    }

    public TransactionExecutionResultResponse remit(@Valid AccountRequest request) {
        TransactionExecutionResultResponse resultResponse = new TransactionExecutionResultResponse();

        Optional<Account> accountTo = accountRepository.findById(request.getAccountTo().getAccountNumber());
        Optional<Account> accountFrom = accountRepository.findById(request.getAccountFrom().getAccountNumber());

        Long toBalance = validationService.calculateBalance(new Balance(accountTo.get().getBalance() + request.getAmount()));
        Long fromBalance = validationService.calculateBalance(new Balance(accountFrom.get().getBalance() - request.getAmount()));

        accountTo.get().setBalance(toBalance);
        accountFrom.get().setBalance(fromBalance);

        transactionHistoryService.createRemitHistory(request.getAccountFrom().getAccountNumber(),
                request.getAccountTo().getAccountNumber(),
                request.getNameOpponent(),
                request.getAmount());

        resultResponse.setSuccess(true);
        return resultResponse;
    }
}
