package org.example.ootoutfitoftoday.domain.clothes.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.example.ootoutfitoftoday.domain.clothes.enums.ClothesColor;
import org.example.ootoutfitoftoday.domain.clothes.enums.ClothesSize;

@Getter
public class ClothesRequest {

    private Long categoryId;
    private ClothesSize clothesSize;
    private ClothesColor clothesColor;

    @NotBlank
    @Size(min = 1, max = 255, message = "255자 이하로 입력해 주세요!")
    private String description;
}
