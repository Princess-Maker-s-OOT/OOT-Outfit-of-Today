package com.ootcommon.user.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class NewUsers implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int daily;
    private final int weekly;
    private final int monthly;

    @JsonCreator
    public NewUsers(
            @JsonProperty("daily") int daily,
            @JsonProperty("weekly") int weekly,
            @JsonProperty("monthly") int monthly
    ) {
        this.daily = daily;
        this.weekly = weekly;
        this.monthly = monthly;
    }
}
