package com.wemakeprice.edu.whomakeprice.web.bind;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static com.wemakeprice.edu.whomakeprice.web.bind.CurrenciesController.PATH_NAME_CURRENCY_CODE;
import static com.wemakeprice.edu.whomakeprice.web.bind.CurrenciesController.PATH_VALUE_CURRENCY_CODE;
import static com.wemakeprice.edu.whomakeprice.web.bind.Currency.currencies;
import static com.wemakeprice.edu.whomakeprice.web.bind.Currency.reference;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequestUri;

@Validated
@RestController
@RequestMapping(path = ExchangeRatesController.REQUEST_MAPPING_PATH)
@Slf4j
public class ExchangeRatesController {

    // -----------------------------------------------------------------------------------------------------------------
    static final String REQUEST_MAPPING_PATH = CurrenciesController.REQUEST_MAPPING_PATH + "/exchangeRates";

    // -----------------------------------------------------------------------------------------------------------------
    static final String PATH_NAME_TARGET_CURRENCY_CODE = "targetCurrencyCode";

    static final String PATH_VALUE_TARGET_CURRENCY_CODE = PATH_VALUE_CURRENCY_CODE;

    static final String PATH_TEMPLATE_TARGET_CURRENCY_CODE
            = "{" + PATH_NAME_TARGET_CURRENCY_CODE + ":" + PATH_VALUE_TARGET_CURRENCY_CODE + "}";

    // -----------------------------------------------------------------------------------------------------------------
    static final Map<Currency, Map<Currency, ExchangeRate>> EXCHANGE_RATES = new ConcurrentHashMap<>();

    // -----------------------------------------------------------------------------------------------------------------
    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(@PathVariable(PATH_NAME_CURRENCY_CODE) final String sourceCurrencyCode,
                                    @Valid @RequestBody final ExchangeRate exchangeRate) {
        if (!Objects.equals(exchangeRate.getSourceCurrency().getCode(), sourceCurrencyCode)) {
            throw new ResponseStatusException(
                    BAD_REQUEST, "$.sourceCurrency.code(" + exchangeRate.getSourceCurrency().getCode()
                    + ") != /{" + PATH_NAME_CURRENCY_CODE + "}(" + sourceCurrencyCode + ")");
        }
        EXCHANGE_RATES.compute(exchangeRate.getSourceCurrency(), (k1, v1) -> {
            if (v1 == null) {
                v1 = new ConcurrentHashMap<>();
            }
            v1.compute(exchangeRate.getTargetCurrency(), (k2, v2) -> {
                if (v2 != null) {
                    throw new ResponseStatusException(CONFLICT, "$ conflicts with " + v2);
                }
                return exchangeRate;
            });
            return v1;
        });
        final URI location = fromCurrentRequestUri()
                .pathSegment(exchangeRate.getTargetCurrency().getCode())
                .build()
                .toUri();
        return created(location).build();
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public Collection<ExchangeRate> read(@PathVariable(PATH_NAME_CURRENCY_CODE) final String sourceCurrencyCode) {
        final Map<Currency, ExchangeRate> exchangeRates = EXCHANGE_RATES.get(reference(sourceCurrencyCode));
        if (exchangeRates == null) {
            return emptyList();
        }
        return exchangeRates.values();
    }

    // ------------------------------------------------------------------------------------------------- /{currencyCode}
    @GetMapping(path = "/" + PATH_TEMPLATE_TARGET_CURRENCY_CODE, produces = APPLICATION_JSON_VALUE)
    public ExchangeRate readSingle(@PathVariable(PATH_NAME_CURRENCY_CODE) final String sourceCurrencyCode,
                                   @PathVariable(PATH_NAME_TARGET_CURRENCY_CODE) final String targetCurrencyCode) {
        final Currency sourceCurrency = currencies().get(sourceCurrencyCode);
        if (sourceCurrency == null) {
            throw new ResponseStatusException(
                    NOT_FOUND, "no currency identified by /{" + PATH_NAME_CURRENCY_CODE + "}(" + sourceCurrencyCode
                    + ")");
        }
        final ExchangeRate exchangeRate = ofNullable(EXCHANGE_RATES.get(sourceCurrency))
                .map(m -> m.get(reference(targetCurrencyCode))).orElse(null);
        if (exchangeRate == null) {
            throw new ResponseStatusException(
                    NOT_FOUND, "no exchangeRate identified by /{" + PATH_NAME_TARGET_CURRENCY_CODE + "}("
                    + targetCurrencyCode + ")");
        }
        return exchangeRate;
    }

    @ResponseStatus(NO_CONTENT)
    @PutMapping(path = "/" + PATH_TEMPLATE_TARGET_CURRENCY_CODE, produces = APPLICATION_JSON_VALUE)
    public void updateSingle(@PathVariable(PATH_NAME_CURRENCY_CODE) final String sourceCurrencyCode,
                             @PathVariable(PATH_NAME_TARGET_CURRENCY_CODE) final String targetCurrencyCode,
                             @Valid @RequestBody final ExchangeRate exchangeRate) {
        final Currency sourceCurrency = currencies().get(sourceCurrencyCode);
        if (sourceCurrency == null) {
            throw new ResponseStatusException(
                    NOT_FOUND, "no currency identified by /{" + PATH_NAME_CURRENCY_CODE + "}(" + sourceCurrencyCode
                    + ")");
        }
        if (!Objects.equals(exchangeRate.getTargetCurrency().getCode(), targetCurrencyCode)) {
            throw new ResponseStatusException(
                    BAD_REQUEST, "$.targetCurrency.code(" + exchangeRate.getTargetCurrency().getCode() + ") != /{"
                    + PATH_NAME_TARGET_CURRENCY_CODE + "(" + targetCurrencyCode + ")");
        }
        final Map<Currency, ExchangeRate> c1 = EXCHANGE_RATES.compute(sourceCurrency, (k1, v1) -> {
            if (v1 == null) {
                v1 = new ConcurrentHashMap<>();
            }
            final ExchangeRate c2 = v1.compute(exchangeRate.getTargetCurrency(), (k2, v2) -> {
                if (v2 != null) {
                    v2.setSourceAmount(exchangeRate.getSourceAmount());
                    v2.setTargetAmount(exchangeRate.getTargetAmount());
                    return v2;
                }
                return exchangeRate;
            });
            return v1;
        });
    }

    @ResponseStatus(NO_CONTENT)
    @DeleteMapping(path = "/" + PATH_TEMPLATE_TARGET_CURRENCY_CODE)
    public void deleteSingle(@PathVariable(PATH_NAME_CURRENCY_CODE) final String sourceCurrencyCode,
                             @PathVariable(PATH_NAME_TARGET_CURRENCY_CODE) final String targetCurrencyCode) {
        final Currency sourceCurrency = currencies().get(sourceCurrencyCode);
        if (sourceCurrency == null) {
            if (true) {
                return;
            }
            throw new ResponseStatusException(
                    NOT_FOUND, "no currency identified by /{" + PATH_NAME_CURRENCY_CODE + "}(" + sourceCurrencyCode
                    + ")");
        }
        final Map<Currency, ExchangeRate> c1 = EXCHANGE_RATES.compute(reference(sourceCurrencyCode), (k1, v1) -> {
            if (v1 != null) {
                final ExchangeRate v2 = v1.remove(reference(targetCurrencyCode));
            }
            return v1 == null ? null : v1.isEmpty() ? null : v1;
        });
    }
}
