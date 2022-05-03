package com.parasol.core.api_model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@ToString
@ApiModel("ClientInfo")
public class ClientInfo {
    @ApiModelProperty(name="name", example="김싸피")
    @NotBlank
    private String name;
    @ApiModelProperty(name="resident_number", example = "000000-0000000")
    @NotBlank
    @Size(max=14, min=14)
    private String residentNumber;
}
