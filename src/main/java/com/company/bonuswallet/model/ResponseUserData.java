package com.company.bonuswallet.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResponseUserData {
    String qrId;
    String token;
    BigDecimal bonus;
}
