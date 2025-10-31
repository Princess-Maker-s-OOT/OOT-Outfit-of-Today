package org.example.ootoutfitoftoday.domain.clothes.dto.request;

import lombok.Getter;

import java.util.List;

@Getter
public class ClothesImageUnlinkRequest {

    private List<Long> imageIds;
}
