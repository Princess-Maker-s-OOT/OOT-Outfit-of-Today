package org.example.ootoutfitoftoday.domain.image.service.command;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.aws.config.AwsS3Properties;
import org.example.ootoutfitoftoday.domain.image.dto.request.ImageSaveRequest;
import org.example.ootoutfitoftoday.domain.image.dto.request.PresignedUrlRequest;
import org.example.ootoutfitoftoday.domain.image.dto.response.ImageSaveResponse;
import org.example.ootoutfitoftoday.domain.image.dto.response.PresignedUrlResponse;
import org.example.ootoutfitoftoday.domain.image.entity.Image;
import org.example.ootoutfitoftoday.domain.image.entity.ImageType;
import org.example.ootoutfitoftoday.domain.image.exception.ImageErrorCode;
import org.example.ootoutfitoftoday.domain.image.exception.ImageException;
import org.example.ootoutfitoftoday.domain.image.repository.ImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ImageCommandServiceImpl implements ImageCommandService {

    private static final int PRESIGNED_URL_EXPIRATION_MINUTES = 5;
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    private final S3Presigner s3Presigner;
    private final AwsS3Properties awsS3Properties;
    private final ImageRepository imageRepository;

    // Presigned URL 생성
    @Override
    public PresignedUrlResponse generatePresignedUrl(Long userId, PresignedUrlRequest request) {

        validateFileName(request.fileName());

        ImageType imageType = ImageType.fromString(request.type());

        String s3Key = generateS3Key(imageType, request.fileName());

        String presignedUrl = createPresignedUrl(s3Key);

        String fileUrl = generateFileUrl(s3Key);

        return PresignedUrlResponse.of(
                presignedUrl,
                fileUrl,
                s3Key,
                (int) Duration.ofMinutes(PRESIGNED_URL_EXPIRATION_MINUTES).getSeconds()
        );
    }

    // 이미지 메타데이터 저장
    @Override
    public ImageSaveResponse saveImage(ImageSaveRequest request) {

        // S3 Key 중복 체크
        imageRepository.findByS3KeyAndIsDeletedFalse(request.s3Key())
                .ifPresent(image -> {
                    throw new ImageException(ImageErrorCode.IMAGE_ALREADY_EXISTS);
                });

        // ImageType 변환
        ImageType imageType = ImageType.fromString(request.type());

        // Image 엔티티 생성 및 저장
        Image image = Image.create(
                request.url(),
                request.fileName(),
                request.s3Key(),
                request.contentType(),
                request.size(),
                imageType
        );

        Image savedImage = imageRepository.save(image);

        return ImageSaveResponse.from(savedImage);
    }

    // 파일명 검증
    private void validateFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new ImageException(ImageErrorCode.INVALID_FILE_NAME);
        }

        String extension = getFileExtension(fileName);

        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new ImageException(ImageErrorCode.INVALID_FILE_EXTENSION);
        }
    }

    // 파일 확장자 추출
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            throw new ImageException(ImageErrorCode.INVALID_FILE_NAME);
        }
        return fileName.substring(lastDotIndex + 1);
    }

    // S3 키 생성 (userId 제거)
    private String generateS3Key(ImageType imageType, String originalFileName) {
        String extension = getFileExtension(originalFileName);
        String uniqueFileName = UUID.randomUUID().toString() + "." + extension;

        return String.format("%s/%s",
                imageType.getFolder(),
                uniqueFileName);
    }

    // Presigned URL 생성
    private String createPresignedUrl(String s3Key) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(awsS3Properties.getS3().getBucket())
                    .key(s3Key)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(PRESIGNED_URL_EXPIRATION_MINUTES))
                    .putObjectRequest(putObjectRequest)
                    .build();

            PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

            return presignedRequest.url().toString();

        } catch (Exception e) {
            throw new ImageException(ImageErrorCode.PRESIGNED_URL_GENERATION_FAILED);
        }
    }

    // 파일 최종 URL 생성
    private String generateFileUrl(String s3Key) {
        String region = awsS3Properties.getRegion().getStaticRegion();
        String bucket = awsS3Properties.getS3().getBucket();

        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, s3Key);
    }
}