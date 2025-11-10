package org.example.ootoutfitoftoday.domain.salepost.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class NewSalePost implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long daily;
    private final long weekly;
    private final long monthly;

    @JsonCreator
    public NewSalePost(
            @JsonProperty("daily") long daily,
            @JsonProperty("weekly") long weekly,
            @JsonProperty("monthly") long monthly
    ) {
        this.daily = daily;
        this.weekly = weekly;
        this.monthly = monthly;
    }
}
