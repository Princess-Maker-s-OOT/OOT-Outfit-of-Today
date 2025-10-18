package org.example.ootoutfitoftoday.domain.clothes.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ClothesColor {

    BLACK("검정색"),
    WHITE("흰색"),
    GRAY("회색"),
    NAVY("네이비"),
    BEIGE("베이지"),
    RED("빨강"),
    BLUE("파랑"),
    GREEN("초록"),
    YELLOW("노랑"),
    PINK("핑크");

    private final String color;
}
