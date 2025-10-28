package org.example.ootoutfitoftoday.domain.salepost.util;

import org.example.ootoutfitoftoday.domain.salepost.entity.SalePost;

import java.util.List;

public record SliceContent(
        List<SalePost> content,
        boolean hasNext
) {

    public static SliceContent from(List<SalePost> content, boolean hasNext) {

        return new SliceContent(content, hasNext);
    }
}
