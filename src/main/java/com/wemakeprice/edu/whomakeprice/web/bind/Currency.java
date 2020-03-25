package com.wemakeprice.edu.whomakeprice.web.bind;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import java.util.*;

import static java.util.Collections.synchronizedMap;
import static java.util.Collections.unmodifiableSortedMap;
import static java.util.Currency.getAvailableCurrencies;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Getter
@Slf4j
public class Currency {

    // -----------------------------------------------------------------------------------------------------------------
    private static Map<String, Currency> REFERENCES = synchronizedMap(new WeakHashMap<>());

    /**
     * Returns a reference of currency for specified code.
     *
     * @param code the code from which the reference is created.
     * @return a reference of currency.
     */
    static Currency reference(final String code) {
        return REFERENCES.computeIfAbsent(code, k -> {
            final Currency reference = new Currency();
            reference.code = code;
            return reference;
        });
    }

    // -----------------------------------------------------------------------------------------------------------------
    private static SortedMap<String, Currency> CURRENCIES;

    /**
     * Returns an unmodifiable sorted map of currency codes and currencies.
     *
     * @return an unmodifiable sorted map of currency codes and currencies
     */
    static Map<String, Currency> currencies() {
        if (CURRENCIES == null) {
            final SortedMap<String, Currency> currencies = getAvailableCurrencies()
                    .stream()
                    .map(v -> {
                        final Currency currency = new Currency();
                        currency.code = v.getCurrencyCode();
                        currency.symbol = v.getSymbol();
                        currency.name = v.getDisplayName();
                        return currency;
                    })
                    .collect(toMap(
                            Currency::getCode,
                            identity(),
                            (c1, c2) -> {
                                throw new RuntimeException("duplicate currencies: " + c1 + ", " + c2);
                            },
                            TreeMap::new)
                    );
            CURRENCIES = unmodifiableSortedMap(currencies);
        }
        return CURRENCIES;
    }

    // -----------------------------------------------------------------------------------------------------------------
    @AssertTrue
    private boolean isCodeValid() {
        return currencies().containsKey(code);
    }

    // -----------------------------------------------------------------------------------------------------------------
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Currency)) {
            return false;
        }
        final Currency currency = (Currency) o;
        return Objects.equals(code, currency.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return super.toString() + "{"
                + "code=" + code
                + ",symbol=" + symbol
                + ",name=" + name
                + "}";
    }

    // -----------------------------------------------------------------------------------------------------------------
    @NotBlank
    private String code;

    @NotBlank
    private String symbol;

    @NotBlank
    private String name;
}
