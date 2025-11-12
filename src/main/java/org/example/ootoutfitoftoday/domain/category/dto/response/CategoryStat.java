package org.example.ootoutfitoftoday.domain.category.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class CategoryStat implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final long count;

    @JsonCreator
    @QueryProjection
    public CategoryStat(
            @JsonProperty("name") String name,
            @JsonProperty("count") long count
    ) {
        this.name = name;
        this.count = count;
    }
}
