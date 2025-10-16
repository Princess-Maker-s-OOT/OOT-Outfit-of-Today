package org.example.ootoutfitoftoday.domain.clothes.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Size {

    XS("Extra Small"),
    S("Small"),
    M("Medium"),
    XL("Extra Large"),
    XXL("Double Extra Large"),
    XXXL("Triple Extra Large");

    private final String size;
}
