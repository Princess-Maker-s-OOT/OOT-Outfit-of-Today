package org.example.ootoutfitoftoday.domain.clothes.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.ApiResponse;
import org.example.ootoutfitoftoday.domain.clothes.dto.request.ClothesRequest;
import org.example.ootoutfitoftoday.domain.clothes.dto.response.ClothesResponse;
import org.example.ootoutfitoftoday.domain.clothes.exception.ClothesSuccessCode;
import org.example.ootoutfitoftoday.domain.clothes.service.command.ClothesCommandService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/clothes")
public class ClothesController {

    private final ClothesCommandService clothesCommandService;

    @PostMapping
    public ResponseEntity<ApiResponse<ClothesResponse>> createClothes(
            @Valid @RequestBody ClothesRequest clothesRequest
    ) {
        ClothesResponse clothesResponse = clothesCommandService.createClothes(clothesRequest);

        return ApiResponse.success(clothesResponse, ClothesSuccessCode.CLOTHES_CREATED);
    }
}
