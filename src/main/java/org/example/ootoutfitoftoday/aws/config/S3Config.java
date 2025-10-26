package org.example.ootoutfitoftoday.aws.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
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

    /**
     * AWS Region Bean 생성
     * - 다른 AWS 서비스에서도 재사용 가능
     */
    @Bean
    public Region awsRegion() {
        String regionStr = awsS3Properties.getRegion().getStaticRegion();
        log.info("AWS Region 설정: {}", regionStr);
        return Region.of(regionStr);
    }

    /**
     * EC2 IAM Role Credentials Provider Bean 생성
     * - InstanceProfileCredentialsProvider: EC2 IAM Role 자동 사용
     * - 다른 AWS 서비스에서도 재사용 가능
     */
    @Bean
    public InstanceProfileCredentialsProvider instanceProfileCredentialsProvider() {
        log.info("InstanceProfileCredentialsProvider 생성 - EC2 IAM Role 사용");
        return InstanceProfileCredentialsProvider.create();
    }

    /**
     * S3Client Bean 생성
     * - Region과 CredentialsProvider를 DI로 주입받음
     */
    @Bean
    public S3Client s3Client(Region awsRegion, InstanceProfileCredentialsProvider credentialsProvider) {
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
     * - Presigned URL 생성을 위한 Bean
     * - Region과 CredentialsProvider를 DI로 주입받음
     */
    @Bean
    public S3Presigner s3Presigner(Region awsRegion, InstanceProfileCredentialsProvider credentialsProvider) {
        log.info("S3Presigner 초기화 시작 - Region: {}", awsRegion.id());

        S3Presigner s3Presigner = S3Presigner.builder()
                .region(awsRegion)
                .credentialsProvider(credentialsProvider)
                .build();

        log.info("S3Presigner 초기화 완료");
        return s3Presigner;
    }
}