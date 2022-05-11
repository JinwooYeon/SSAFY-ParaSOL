package com.parasol.BaaS.controller;

import com.parasol.BaaS.api_request.*;
import com.parasol.BaaS.api_response.AccountBalanceQueryResultResponse;
import com.parasol.BaaS.api_response.AccountHistoryQueryResultResponse;
import com.parasol.BaaS.api_response.AccountListQueryResultResponse;
import com.parasol.BaaS.api_response.TransactionExecuteResultResponse;
import com.parasol.BaaS.auth.jwt.UserDetail;
import com.parasol.BaaS.db.entity.User;
import com.parasol.BaaS.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("account")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @GetMapping("balance")
    @ResponseBody
    public AccountBalanceQueryResultResponse getBalance(
            @RequestParam("bankName") String bankName,
            @RequestParam("bankAccountNumber") String bankAccountNumber
    ) {
        QueryAccountBalanceRequest request = new QueryAccountBalanceRequest();
        request.setBankName(bankName);
        request.setBankAccountNumber(bankAccountNumber);

        AccountBalanceQueryResultResponse result = accountService.getBalance(request);
        return result;
    }

    @GetMapping
    @ResponseBody
    public Mono<AccountListQueryResultResponse> getAccountList(
            Authentication authentication,
            @RequestParam("bankName") String bankName
    ) {
        if (authentication == null) {
            throw new IllegalStateException("give me a token");
        }

        UserDetail userDetail = (UserDetail) authentication.getDetails();
        User user = userDetail.getUser();

        return accountService.getAccountList(user, bankName);
    }

    @GetMapping("history")
    @ResponseBody
    public AccountHistoryQueryResultResponse getAccountHistory(
            @RequestParam("bankName") String bankName,
            @RequestParam("bankAccountNumber") String bankAccountNumber
    ) {
        QueryAccountHistoryRequest request = new QueryAccountHistoryRequest();
        request.setBankName(bankName);
        request.setBankAccountNumber(bankAccountNumber);

        AccountHistoryQueryResultResponse result = accountService.getAccountHistory(request);
        return result;
    }

    @PostMapping("deposit")
    @ResponseBody
    public TransactionExecuteResultResponse deposit(
            @RequestBody DepositRequest request
    ) {
        TransactionExecuteResultResponse result = accountService.deposit(request);
        return result;
    }

    @PostMapping("withdraw")
    @ResponseBody
    public TransactionExecuteResultResponse withdraw(
            @RequestBody WithdrawRequest request
    ) {
        TransactionExecuteResultResponse result = accountService.withdraw(request);
        return result;
    }
}
