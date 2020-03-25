package com.wemakeprice.edu.whomakeprice.web.bind;

import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.Objects;

@Setter
@Getter
public class ExchangeRate {

    // -----------------------------------------------------------------------------------------------------------------
    @AssertFalse
    private boolean isTargetCurrencyEqualsToSourceCurrency() {
        return Objects.equals(targetCurrency, sourceCurrency);
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public String toString() {
        return super.toString() + "{"
               + "sourceCurrency=" + sourceCurrency
               + ",sourceAmount=" + sourceAmount
               + ",targetCurrency=" + targetCurrency
               + ",targetAmount=" + targetAmount
               + "}";
    }

    // -----------------------------------------------------------------------------------------------------------------
    @Valid
    @NotNull
    private Currency sourceCurrency;

    @Positive
    private int sourceAmount;

    @Valid
    @NotNull
    private Currency targetCurrency;

    @Positive
    private int targetAmount;
}
