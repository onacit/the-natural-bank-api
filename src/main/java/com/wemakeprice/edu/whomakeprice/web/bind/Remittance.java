package com.wemakeprice.edu.whomakeprice.web.bind;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;
import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.Objects;

@Setter
@Getter
@Slf4j
public class Remittance {

    // -----------------------------------------------------------------------------------------------------------------
    @AssertFalse
    private boolean isReceiverEqualsSender() {
        return Objects.equals(receiver, sender);
    }

    // -----------------------------------------------------------------------------------------------------------------
    @Positive
    private int amount;

    @Valid
    @NotNull
    private Client receiver;

    @Valid
    @NotNull
    private Client sender;
}
