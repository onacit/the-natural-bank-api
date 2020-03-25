package com.wemakeprice.edu.whomakeprice.web.bind;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

@Setter
@Getter
@Slf4j
public class Account {

    // -----------------------------------------------------------------------------------------------------------------
    @Override
    public String toString() {
        return super.toString() + "{"
                + "balance=" + balance
                + ",currency=" + currency
                + "}";
    }

    // -----------------------------------------------------------------------------------------------------------------
    @PositiveOrZero
    private int balance;

    @Valid
    @NotNull
    private Currency currency;
}
