package org.example.ootoutfitoftoday.domain.category.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CategoryRequest {

    private Long parentId;

    @NotBlank
    @Size(min = 1, max = 30, message = "30자 이하로 입력해 주세요!")
    private String name;
}
