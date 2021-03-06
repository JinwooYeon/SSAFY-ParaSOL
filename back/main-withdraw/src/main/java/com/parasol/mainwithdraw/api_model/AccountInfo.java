package com.parasol.mainwithdraw.api_model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ToString
@ApiModel("AccountInfo")
public class AccountInfo {
    @ApiModelProperty(name = "bank_account_number", example = "110-437-525252")
    @NotBlank
    private String accountNumber;
}
