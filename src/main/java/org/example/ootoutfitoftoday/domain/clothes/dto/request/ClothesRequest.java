package org.example.ootoutfitoftoday.domain.clothes.dto.request;

import com.ootcommon.clothes.enums.ClothesColor;
import com.ootcommon.clothes.enums.ClothesSize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class ClothesRequest {

    private Long categoryId;
    private ClothesSize clothesSize;
    private ClothesColor clothesColor;

    @NotBlank
    @Size(max = 255, message = "255자 이하로 입력해 주세요!")
    private String description;

    private List<Long> images;
}
