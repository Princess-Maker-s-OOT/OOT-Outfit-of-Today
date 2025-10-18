package org.example.ootoutfitoftoday.domain.clothes.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ClothesSize {

    XS("Extra Small"),
    S("Small"),
    M("Medium"),
    L("Large"),
    XL("Extra Large"),
    XXL("Double Extra Large"),
    XXXL("Triple Extra Large"),
    FREE("Free");

    private final String size;
}
