package org.example.ootoutfitoftoday.aws.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * AWS S3 설정
 * - EC2 IAM Role을 사용하여 S3에 접근
 * - cloud.aws.stack.auto=true일 때만 활성화 (dev 프로파일)
 * - local 프로파일에서는 비활성화
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "cloud.aws.stack.auto",
        havingValue = "true"
)
public class S3Config {

    private final AwsS3Properties awsS3Properties;

    // =========================================
    // AWS 로컬 키 환경변수 (로컬 개발용)
    // - 로컬 환경에서 AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY가 설정되어 있으면 사용
    // - EC2에서는 값이 없으므로 InstanceProfile 사용
    // =========================================
    @Value("${AWS_ACCESS_KEY_ID:}")
    private String accessKey;

    @Value("${AWS_SECRET_ACCESS_KEY:}")
    private String secretKey;

    /**
     * AWS Region Bean 생성
     * - EC2, 로컬 모두 공통
     */
    @Bean
    public Region awsRegion() {
        String regionStr = awsS3Properties.getRegion().getStaticRegion();
        log.info("AWS Region 설정: {}", regionStr);
        return Region.of(regionStr);
    }

    /**
     * AWS Credentials Provider Bean
     * - 로컬 환경용: StaticCredentialsProvider 사용
     * - EC2 환경용: InstanceProfileCredentialsProvider 사용 (IAM Role 자동)
     */
    @Bean
    public AwsCredentialsProvider awsCredentialsProvider() {
        if (!accessKey.isBlank() && !secretKey.isBlank()) {
            // =========================================
            // 로컬 개발용 (환경변수 AWS_ACCESS_KEY_ID / AWS_SECRET_ACCESS_KEY 사용)
            // =========================================
            log.info("StaticCredentialsProvider 생성 - 로컬 키 사용");
            return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
        } else {
            // =========================================
            // EC2 서버용 (IAM Role 자동 사용)
            // =========================================
            log.info("InstanceProfileCredentialsProvider 생성 - EC2 IAM Role 사용");
            return InstanceProfileCredentialsProvider.create();
        }
    }

    /**
     * S3Client Bean 생성
     * - Region + CredentialsProvider 주입
     * - 로컬, EC2 공통
     */
    @Bean
    public S3Client s3Client(Region awsRegion, AwsCredentialsProvider credentialsProvider) {
        String bucketName = awsS3Properties.getS3().getBucket();
        log.info("S3Client 초기화 시작 - Region: {}, Bucket: {}", awsRegion.id(), bucketName);

        S3Client s3Client = S3Client.builder()
                .region(awsRegion)
                .credentialsProvider(credentialsProvider)
                .build();

        log.info("S3Client 초기화 완료");
        return s3Client;
    }

    /**
     * S3Presigner Bean 생성
     * - Presigned URL 생성용
     * - Region + CredentialsProvider 주입
     * - 로컬, EC2 공통
     */
    @Bean
    public S3Presigner s3Presigner(Region awsRegion, AwsCredentialsProvider credentialsProvider) {
        log.info("S3Presigner 초기화 시작 - Region: {}", awsRegion.id());

        S3Presigner s3Presigner = S3Presigner.builder()
                .region(awsRegion)
                .credentialsProvider(credentialsProvider)
                .build();

        log.info("S3Presigner 초기화 완료");
        return s3Presigner;
    }
}
