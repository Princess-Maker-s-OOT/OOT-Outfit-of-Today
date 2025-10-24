package org.example.ootoutfitoftoday.domain.clothes.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.PageResponse;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.clothes.dto.request.ClothesRequest;
import org.example.ootoutfitoftoday.domain.clothes.dto.response.ClothesResponse;
import org.example.ootoutfitoftoday.domain.clothes.enums.ClothesColor;
import org.example.ootoutfitoftoday.domain.clothes.enums.ClothesSize;
import org.example.ootoutfitoftoday.domain.clothes.exception.ClothesSuccessCode;
import org.example.ootoutfitoftoday.domain.clothes.service.command.ClothesCommandService;
import org.example.ootoutfitoftoday.domain.clothes.service.query.ClothesQueryService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/clothes")
public class ClothesController {

    private final ClothesCommandService clothesCommandService;
    private final ClothesQueryService clothesQueryService;

    @PostMapping
    public ResponseEntity<Response<ClothesResponse>> createClothes(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody ClothesRequest clothesRequest
    ) {

        ClothesResponse clothesResponse = clothesCommandService.createClothes(authUser.getUserId(), clothesRequest);

        return Response.success(clothesResponse, ClothesSuccessCode.CLOTHES_CREATED);
    }

    @GetMapping
    public ResponseEntity<PageResponse<ClothesResponse>> getClothes(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) ClothesColor clothesColor,
            @RequestParam(required = false) ClothesSize clothesSize,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction
    ) {

        Page<ClothesResponse> clothes = clothesQueryService.getClothes(
                categoryId,
                authUser.getUserId(),
                clothesColor,
                clothesSize,
                page,
                size,
                sort,
                direction
        );

        return PageResponse.success(clothes, ClothesSuccessCode.CLOTHES_OK);
    }

    @GetMapping("/{clothesId}")
    public ResponseEntity<Response<ClothesResponse>> getClothesById(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long clothesId
    ) {

        ClothesResponse clothesResponse = clothesQueryService.getClothesById(authUser.getUserId(), clothesId);

        return Response.success(clothesResponse, ClothesSuccessCode.CLOTHES_OK);
    }

    @PutMapping("/{clothesId}")
    public ResponseEntity<Response<ClothesResponse>> updateClothes(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long clothesId,
            @Valid @RequestBody ClothesRequest clothesRequest
    ) {

        ClothesResponse clothesResponse = clothesCommandService.updateClothes(authUser.getUserId(), clothesId, clothesRequest);

        return Response.success(clothesResponse, ClothesSuccessCode.CLOTHES_UPDATE);
    }

    @DeleteMapping("/{clothesId}")
    public ResponseEntity<Response<Void>> deleteClothes(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long clothesId
    ) {
        clothesCommandService.deleteClothes(authUser.getUserId(), clothesId);

        return Response.success(null, ClothesSuccessCode.CLOTHES_DELETE);
    }
}
