package org.example.ootoutfitoftoday.domain.image.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.image.dto.request.PresignedUrlRequest;
import org.example.ootoutfitoftoday.domain.image.dto.response.PresignedUrlResponse;
import org.example.ootoutfitoftoday.domain.image.exception.ImageSuccessCode;
import org.example.ootoutfitoftoday.domain.image.service.command.ImageCommandService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "이미지 관리", description = "S3 이미지 업로드 관련 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/images")
public class ImageController {

    private final ImageCommandService imageCommandService;

    /**
     * Presigned URL 생성
     *
     * @param authUser: 인증된 사용자 정보
     * @param request   Presigned URL 생성 요청 객체 (파일명, 확장자 등 포함)
     * @return PresignedUrlResponse 객체를 포함한 성공 응답
     */
    @Operation(
            summary = "Presigned URL 생성",
            description = "S3에 이미지 업로드를 위한 Presigned URL을 생성합니다. 생성된 URL로 5분 이내에 이미지를 업로드할 수 있습니다.",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "생성 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 파일명 또는 파일 형식"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "500", description = "Presigned URL 생성 실패")
            }
    )
    @PostMapping("/presigned-urls")
    public ResponseEntity<Response<PresignedUrlResponse>> generatePresignedUrl(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody PresignedUrlRequest request
    ) {

        PresignedUrlResponse response = imageCommandService.generatePresignedUrl(
                authUser.getUserId(),
                request
        );

        return Response.success(response, ImageSuccessCode.PRESIGNED_URL_CREATED);
    }
}
