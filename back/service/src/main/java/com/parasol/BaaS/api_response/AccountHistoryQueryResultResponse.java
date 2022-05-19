package com.parasol.BaaS.api_response;

import com.parasol.BaaS.api_model.AccountHistories;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class AccountHistoryQueryResultResponse extends AccountHistories {
}
