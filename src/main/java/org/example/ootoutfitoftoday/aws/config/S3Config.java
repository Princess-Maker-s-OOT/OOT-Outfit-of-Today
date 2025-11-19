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

@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "cloud.aws.stack.auto",
        havingValue = "true"
)
public class S3Config {

    private final AwsS3Properties awsS3Properties;

    @Value("${AWS_ACCESS_KEY_ID:}")
    private String accessKey;

    @Value("${AWS_SECRET_ACCESS_KEY:}")
    private String secretKey;

    @Bean
    public Region awsRegion() {
        String regionStr = awsS3Properties.getRegion().getStaticRegion();
        log.info("AWS Region 설정: {}", regionStr);

        return Region.of(regionStr);
    }

    @Bean
    public AwsCredentialsProvider awsCredentialsProvider() {
        if (!accessKey.isBlank() && !secretKey.isBlank()) {
            log.info("StaticCredentialsProvider 생성 - 로컬 키 사용");

            return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));

        } else {
            log.info("InstanceProfileCredentialsProvider 생성 - EC2 IAM Role 사용");

            return InstanceProfileCredentialsProvider.create();
        }
    }

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