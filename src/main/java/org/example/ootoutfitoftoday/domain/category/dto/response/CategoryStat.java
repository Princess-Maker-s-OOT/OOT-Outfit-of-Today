package org.example.ootoutfitoftoday.domain.category.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class CategoryStat {

    private final String name;
    private final long count;

    @QueryProjection
    public CategoryStat(String name, long count) {
        this.name = name;
        this.count = count;
    }
}
