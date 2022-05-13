package com.parasol.authentication.api_response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ApiModel("LoginResultResponse")
@Builder
public class LoginResultResponse {
    @ApiModelProperty(name="isSuccess",example = "")
    Boolean isSuccess;
}
