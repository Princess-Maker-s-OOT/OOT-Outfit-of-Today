
package org.example.ootoutfitoftoday.domain.clothes.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.common.response.SliceResponse;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.clothes.dto.request.ClothesRequest;
import org.example.ootoutfitoftoday.domain.clothes.dto.response.ClothesResponse;
import org.example.ootoutfitoftoday.domain.clothes.enums.ClothesColor;
import org.example.ootoutfitoftoday.domain.clothes.enums.ClothesSize;
import org.example.ootoutfitoftoday.domain.clothes.exception.ClothesSuccessCode;
import org.example.ootoutfitoftoday.domain.clothes.service.command.ClothesCommandService;
import org.example.ootoutfitoftoday.domain.clothes.service.query.ClothesQueryService;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "옷 관리", description = "옷관련 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/clothes")
public class ClothesController {

    private final ClothesCommandService clothesCommandService;
    private final ClothesQueryService clothesQueryService;

    @Operation(
            summary = "옷 등록",
            description = "회원이 자신의 옷을 등록합니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "등록 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "찾을 수 없음")
            })
    @PostMapping
    public ResponseEntity<Response<ClothesResponse>> createClothes(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody ClothesRequest clothesRequest
    ) {

        ClothesResponse clothesResponse = clothesCommandService.createClothes(authUser.getUserId(), clothesRequest);

        return Response.success(clothesResponse, ClothesSuccessCode.CLOTHES_CREATED);
    }

    @Operation(
            summary = "옷 전체 조회",
            description = "회원이 자신의 옷을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            })
    @GetMapping
    public ResponseEntity<SliceResponse<ClothesResponse>> getClothes(
            @AuthenticationPrincipal AuthUser authUser,
            @Parameter(description = "카테고리 ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "옷 색상") @RequestParam(required = false) ClothesColor clothesColor,
            @Parameter(description = "옷 사이즈") @RequestParam(required = false) ClothesSize clothesSize,
            @Parameter(description = "마지막 조회 옷 아이디") @RequestParam(required = false) Long lastClothesId,
            @Parameter(description = "페이지당 개수") @RequestParam(defaultValue = "10") int size
    ) {

        Slice<ClothesResponse> clothes = clothesQueryService.getClothes(
                authUser.getUserId(),
                categoryId,
                clothesColor,
                clothesSize,
                lastClothesId,
                size
        );

        return SliceResponse.success(clothes, ClothesSuccessCode.CLOTHES_OK);
    }

    @Operation(
            summary = "해당 옷 조회",
            description = "회원이 자신의 옷을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "찾을 수 없음")
            })
    @GetMapping("/{clothesId}")
    public ResponseEntity<Response<ClothesResponse>> getClothesById(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long clothesId
    ) {

        ClothesResponse clothesResponse = clothesQueryService.getClothesById(authUser.getUserId(), clothesId);

        return Response.success(clothesResponse, ClothesSuccessCode.CLOTHES_OK);
    }

    @Operation(
            summary = "해당 옷 수정",
            description = "회원이 자신의 옷을 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "찾을 수 없음")
            })
    @PutMapping("/{clothesId}")
    public ResponseEntity<Response<ClothesResponse>> updateClothes(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long clothesId,
            @Valid @RequestBody ClothesRequest clothesRequest
    ) {

        ClothesResponse clothesResponse = clothesCommandService.updateClothes(authUser.getUserId(), clothesId, clothesRequest);

        return Response.success(clothesResponse, ClothesSuccessCode.CLOTHES_UPDATE);
    }

    @Operation(
            summary = "해당 옷 삭제",
            description = "회원이 자신의 옷을 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "찾을 수 없음")
            })
    @DeleteMapping("/{clothesId}")
    public ResponseEntity<Response<Void>> deleteClothes(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long clothesId
    ) {
        clothesCommandService.deleteClothes(authUser.getUserId(), clothesId);

        return Response.success(null, ClothesSuccessCode.CLOTHES_DELETE);
    }
}
