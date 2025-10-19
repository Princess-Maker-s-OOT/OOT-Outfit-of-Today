package org.example.ootoutfitoftoday.domain.clothes.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.ApiPageResponse;
import org.example.ootoutfitoftoday.common.response.ApiResponse;
import org.example.ootoutfitoftoday.domain.clothes.dto.request.ClothesRequest;
import org.example.ootoutfitoftoday.domain.clothes.dto.response.ClothesResponse;
import org.example.ootoutfitoftoday.domain.clothes.enums.ClothesColor;
import org.example.ootoutfitoftoday.domain.clothes.enums.ClothesSize;
import org.example.ootoutfitoftoday.domain.clothes.exception.ClothesSuccessCode;
import org.example.ootoutfitoftoday.domain.clothes.service.command.ClothesCommandService;
import org.example.ootoutfitoftoday.domain.clothes.service.query.ClothesQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/clothes")
public class ClothesController {

    private final ClothesCommandService clothesCommandService;
    private final ClothesQueryService clothesQueryService;

    @PostMapping
    public ResponseEntity<ApiResponse<ClothesResponse>> createClothes(
            @Valid @RequestBody ClothesRequest clothesRequest
    ) {
        ClothesResponse clothesResponse = clothesCommandService.createClothes(clothesRequest);

        return ApiResponse.success(clothesResponse, ClothesSuccessCode.CLOTHES_CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiPageResponse<ClothesResponse>> getClothes(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) ClothesColor clothesColor,
            @RequestParam(required = false) ClothesSize clothesSize,
            @PageableDefault Pageable pageable
    ) {
        Page<ClothesResponse> clothes = clothesQueryService.getClothes(
                categoryId,
                clothesColor,
                clothesSize,
                pageable
        );

        return ApiPageResponse.success(clothes, ClothesSuccessCode.CLOTHES_OK);
    }
}
