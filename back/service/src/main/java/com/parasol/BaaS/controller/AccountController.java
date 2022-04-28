package com.parasol.BaaS.controller;

import com.parasol.BaaS.api_request.*;
import com.parasol.BaaS.api_response.AccountBalanceQueryResultResponse;
import com.parasol.BaaS.api_response.AccountHistoryQueryResultResponse;
import com.parasol.BaaS.api_response.AccountListQueryResultResponse;
import com.parasol.BaaS.api_response.TransactionExecuteResultResponse;
import com.parasol.BaaS.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountController {

    @Autowired
    private AccountService accountService;

    @GetMapping("/account/balance")
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

    @GetMapping("/account")
    @ResponseBody
    public AccountListQueryResultResponse getAccountList(
            @RequestParam("bankName") String bankName
    ) {
        QueryAccountListRequest request = new QueryAccountListRequest();
        request.setBankName(bankName);

        AccountListQueryResultResponse result = accountService.getAccountList(request);
        return result;
    }

    @GetMapping("/account/history")
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

    @GetMapping("/account/deposit")
    @ResponseBody
    public TransactionExecuteResultResponse deposit(
            @RequestBody DepositRequest request
    ) {
        TransactionExecuteResultResponse result = accountService.deposit(request);
        return result;
    }

    @GetMapping("/account/withdraw")
    @ResponseBody
    public TransactionExecuteResultResponse withdraw(
            @RequestBody WithdrawRequest request
    ) {
        TransactionExecuteResultResponse result = accountService.withdraw(request);
        return result;
    }
}
