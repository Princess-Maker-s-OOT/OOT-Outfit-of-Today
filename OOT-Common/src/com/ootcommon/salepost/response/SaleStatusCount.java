package com.ootcommon.salepost.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ootcommon.salepost.enums.SaleStatus;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class SaleStatusCount implements Serializable {

    private static final long serialVersionUID = 1L;

    private final SaleStatus saleStatus;
    private final long count;

    @JsonCreator
    public SaleStatusCount(
            @JsonProperty("saleStatus") SaleStatus saleStatus,
            @JsonProperty("count") long count
    ) {
        this.saleStatus = saleStatus;
        this.count = count;
    }
}