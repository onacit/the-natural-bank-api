package com.wemakeprice.edu.whomakeprice.web.bind;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = ClientsController.REQUEST_MAPPING_PATH)
@Slf4j
public class ClientsController {

    // -----------------------------------------------------------------------------------------------------------------
    static final String REQUEST_MAPPING_PATH = "/clients";

    // -----------------------------------------------------------------------------------------------------------------
    static final String PATH_NAME_CLIENT_ID = "clientId";

    static final String PATH_VALUE_CLIENT_ID = "\\.+";

    static final String PATH_TEMPLATE_CLIENT_ID = "{" + PATH_NAME_CLIENT_ID + ":" + PATH_VALUE_CLIENT_ID + "}";

    // -----------------------------------------------------------------------------------------------------------------
    static final Map<String, Client> CLIENTS = new ConcurrentHashMap<>();

    static final Map<Client, Map<Currency, Account>> ACCOUNTS = new ConcurrentHashMap<>();

    // -----------------------------------------------------------------------------------------------------------------
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public Collection<Client> read() {
        return CLIENTS.values();
    }

    // ----------------------------------------------------------------------------------------------------- /{clientId}
    @GetMapping(path = "/" + PATH_TEMPLATE_CLIENT_ID, produces = APPLICATION_JSON_VALUE)
    public Client readSingle(@PathVariable(PATH_NAME_CLIENT_ID) final String clientId) {
        final Client client = CLIENTS.get(clientId);
        if (client == null) {
            throw new ResponseStatusException(
                    NOT_FOUND, "no client identified by /{" + PATH_NAME_CLIENT_ID + "}(" + clientId + ")");
        }
        return client;
    }

    @ResponseStatus(NO_CONTENT)
    @PutMapping(path = "/" + PATH_TEMPLATE_CLIENT_ID, consumes = APPLICATION_JSON_VALUE)
    public void updateSingle(@PathVariable(PATH_NAME_CLIENT_ID) final String clientId,
                             @Valid @RequestBody final Client client) {
        if (!Objects.equals(client.getId(), clientId)) {
            throw new ResponseStatusException(
                    BAD_REQUEST, "$.id(" + client.getId() + ") != /{" + PATH_NAME_CLIENT_ID + "}(" + clientId + ")");
        }
        final Client c1 = CLIENTS.compute(clientId, (k1, v1) -> {
            if (v1 != null) {
                v1.setName(client.getName());
                return v1;
            }
            return client;
        });
    }

    @ResponseStatus(NO_CONTENT)
    @DeleteMapping(path = "/" + PATH_TEMPLATE_CLIENT_ID)
    public void deleteSingle(@PathVariable(PATH_NAME_CLIENT_ID) final String clientId) {
        CLIENTS.computeIfPresent(clientId, (k1, v1) -> {
            ACCOUNTS.computeIfPresent(v1, (k2, v2) -> {
                v2.values().stream().filter(a -> a.getBalance() > 0).findAny().ifPresent(a -> {
                    // https://stackoverflow.com/q/25122472/330457
                    throw new ResponseStatusException(CONFLICT, "a non-zero balanced account exists; " + a);
                });
                return null;
            });
            return null;
        });
    }
}
