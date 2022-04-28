package com.parasol.core.api_model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ApiModel("AccountOpenRequest")
public class AccountOpenRequest extends ClientInfo {
    @ApiModelProperty(name="account_password", example = "0809")
    private int accountPassword;
}
