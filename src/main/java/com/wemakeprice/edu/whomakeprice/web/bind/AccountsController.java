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

import static com.wemakeprice.edu.whomakeprice.web.bind.ClientsController.*;
import static com.wemakeprice.edu.whomakeprice.web.bind.CurrenciesController.PATH_NAME_CURRENCY_CODE;
import static com.wemakeprice.edu.whomakeprice.web.bind.CurrenciesController.PATH_TEMPLATE_CURRENCY_CODE;
import static com.wemakeprice.edu.whomakeprice.web.bind.Currency.*;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequestUri;

@Validated
@RestController
@RequestMapping(path = AccountsController.REQUEST_MAPPING_PATH)
@Slf4j
public class AccountsController {

    // -----------------------------------------------------------------------------------------------------------------
    static final String REQUEST_MAPPING_PATH = ClientsController.REQUEST_MAPPING_PATH + "/accounts";

    // -----------------------------------------------------------------------------------------------------------------
    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(@PathVariable(PATH_NAME_CLIENT_ID) final String clientId,
                                    @Valid @RequestBody final Account account) {
        CLIENTS.compute(clientId, (k1, v1) -> { // k1: clientId, v1: client
            if (v1 == null) {
                throw new ResponseStatusException(
                        NOT_FOUND, "no client identified by /{" + PATH_NAME_CLIENT_ID + "}(" + clientId + ")");
            }
            ACCOUNTS.compute(v1, (k2, v2) -> { // k2: client, v2: currency-account map
                if (v2 == null) {
                    v2 = new ConcurrentHashMap<>();
                }
                v2.compute(account.getCurrency(), (k3, v3) -> { // k3: currency, v3: account
                    if (v3 != null) {
                        throw new ResponseStatusException(
                                CONFLICT, "account with /{" + PATH_NAME_CURRENCY_CODE
                                + "}(" + account.getCurrency().getCode() + ") already exists");
                    }
                    return account;
                });
                return v2;
            });
            return v1;
        });
        final URI location = fromCurrentRequestUri()
                .pathSegment(account.getCurrency().getCode())
                .build()
                .toUri();
        return created(location).build();
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public Collection<Account> read(@PathVariable(PATH_NAME_CLIENT_ID) final String clientId) {
        final Client client = CLIENTS.get(clientId);
        if (client == null) {
            throw new ResponseStatusException(
                    NOT_FOUND, "no client identified by /{" + PATH_NAME_CLIENT_ID + "}(" + clientId + ")");
        }
        final Map<Currency, Account> accounts = ACCOUNTS.get(client);
        if (accounts == null) {
            return emptyList();
        }
        return accounts.values();
    }

    // ------------------------------------------------------------------------------------------------- /{currencyCode}
    @GetMapping(path = "/" + PATH_TEMPLATE_CURRENCY_CODE, produces = APPLICATION_JSON_VALUE)
    public Account readSingle(@PathVariable(PATH_NAME_CLIENT_ID) final String clientId,
                              @PathVariable(PATH_NAME_CURRENCY_CODE) final String currencyCode) {
        final Client client = CLIENTS.get(clientId);
        if (client == null) {
            throw new ResponseStatusException(
                    NOT_FOUND, "no client identified by /{" + PATH_NAME_CLIENT_ID + "}(" + clientId + ")");
        }
        final Account account = ofNullable(ACCOUNTS.get(client)).map(m -> m.get(reference(currencyCode))).orElse(null);
        if (account == null) {
            throw new ResponseStatusException(
                    NOT_FOUND, "no account identified by /{" + PATH_NAME_CURRENCY_CODE + "}(" + currencyCode + ")");
        }
        return account;
    }

    @ResponseStatus(NO_CONTENT)
    @PutMapping(path = "/" + PATH_TEMPLATE_CURRENCY_CODE, consumes = APPLICATION_JSON_VALUE)
    public void updateSingle(@PathVariable(PATH_NAME_CLIENT_ID) final String clientId,
                             @PathVariable(PATH_NAME_CURRENCY_CODE) final String currencyCode,
                             @Valid @RequestBody final Account account) {
        if (!Objects.equals(account.getCurrency().getCode(), currencyCode)) {
            throw new ResponseStatusException(
                    BAD_REQUEST, "$.currency.code(" + account.getCurrency().getCode() + ") != /{"
                    + PATH_NAME_CURRENCY_CODE + "}(" + currencyCode + ")");
        }
        final Client client = CLIENTS.get(clientId);
        if (client == null) {
            throw new ResponseStatusException(
                    NOT_FOUND, "no client identified by /{" + PATH_NAME_CLIENT_ID + "}(" + clientId + ")");
        }
        ACCOUNTS.compute(client, (k2, v2) -> { // k2: client, v2: currency-account map
            if (v2 == null) {
                v2 = new ConcurrentHashMap<>();
            }
            v2.compute(account.getCurrency(), (k3, v3) -> { // k3: currency, v3: account
                if (v3 != null) {
                    v3.setBalance(account.getBalance());
                    return v3;
                }
                return account;
            });
            return v2;
        });
    }

    @ResponseStatus(NO_CONTENT)
    @DeleteMapping(path = "/" + PATH_TEMPLATE_CURRENCY_CODE)
    public void deleteSingle(@PathVariable(PATH_NAME_CLIENT_ID) final String clientId,
                             @PathVariable(PATH_NAME_CURRENCY_CODE) final String currencyCode) {
        CLIENTS.compute(clientId, (k1, v1) -> { // k1: clientId, v2: client
            if (v1 == null) {
                throw new ResponseStatusException(
                        NOT_FOUND, "no client identified by /{" + PATH_NAME_CLIENT_ID + "}(" + clientId + ")");
            }
            ACCOUNTS.computeIfPresent(v1, (k2, v2) -> { // k2: client, v2: currency-account map
                v2.computeIfPresent(reference(currencyCode), (k3, v3) -> { // k3: currency, v3: account
                    if (v3.getBalance() > 0) {
                        throw new ResponseStatusException(CONFLICT, "account is not zero-balanced; " + v3);
                    }
                    return null;
                });
                return v2.isEmpty() ? null : v2;
            });
            return v1;
        });
    }
}
