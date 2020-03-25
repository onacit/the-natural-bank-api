package com.wemakeprice.edu.whomakeprice.web.bind;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;

import static com.wemakeprice.edu.whomakeprice.web.bind.Currency.currencies;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = CurrenciesController.REQUEST_MAPPING_PATH)
@Slf4j
public class CurrenciesController {

    // -----------------------------------------------------------------------------------------------------------------
    static final String REQUEST_MAPPING_PATH = "/currencies";

    // -----------------------------------------------------------------------------------------------------------------
    static final String PATH_NAME_CURRENCY_CODE = "currencyCode";

    static final String PATH_VALUE_CURRENCY_CODE = ".+";

    static final String PATH_TEMPLATE_CURRENCY_CODE
            = "{" + PATH_NAME_CURRENCY_CODE + ":" + PATH_VALUE_CURRENCY_CODE + "}";

    // -----------------------------------------------------------------------------------------------------------------
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public Collection<Currency> read() {
        return currencies().values();
    }

    // ------------------------------------------------------------------------------------------------- /{currencyCode}
    @GetMapping(path = "/" + PATH_TEMPLATE_CURRENCY_CODE, produces = APPLICATION_JSON_VALUE)
    public Currency readCurrency(@PathVariable(PATH_NAME_CURRENCY_CODE) final String currencyCode) {
        final Currency currency = currencies().get(currencyCode);
        if (currency == null) {
            throw new ResponseStatusException(
                    NOT_FOUND, "no currency identified by /{" + PATH_NAME_CURRENCY_CODE + "}(" + currencyCode + ")");
        }
        return currency;
    }
}
