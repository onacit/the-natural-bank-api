package com.wemakeprice.edu.whomakeprice.web.bind;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;
import java.util.Objects;

@Setter
@Getter
@Slf4j
public class Client {

    // -----------------------------------------------------------------------------------------------------------------
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Client)) {
            return false;
        }
        final Client user = (Client) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return super.toString() + "{"
                + "id=" + id
                + ",name=" + name
                + "}";
    }

    // -----------------------------------------------------------------------------------------------------------------
    @NotBlank
    private String id;

    @NotBlank
    private String name;
}
