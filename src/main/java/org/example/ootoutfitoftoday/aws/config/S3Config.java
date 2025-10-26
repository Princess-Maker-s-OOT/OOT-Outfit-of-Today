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

@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "cloud.aws.stack.auto",
        havingValue = "true"
)
public class S3Config {

    private final AwsS3Properties awsS3Properties;

    @Bean
    public S3Client s3Client() {
        String regionStr = awsS3Properties.getRegion().getStaticRegion();
        String bucketName = awsS3Properties.getS3().getBucket();

        log.info("S3Client 초기화 시작 - Region: {}, Bucket: {}", regionStr, bucketName);

        S3Client s3Client = S3Client.builder()
                .region(Region.of(regionStr))
                .credentialsProvider(InstanceProfileCredentialsProvider.create())
                .build();

        log.info("S3Client 초기화 완료");
        return s3Client;
    }

    @Bean
    public S3Presigner s3Presigner() {
        String regionStr = awsS3Properties.getRegion().getStaticRegion();

        log.info("S3Presigner 초기화 시작 - Region: {}", regionStr);

        S3Presigner s3Presigner = S3Presigner.builder()
                .region(Region.of(regionStr))
                .credentialsProvider(InstanceProfileCredentialsProvider.create())
                .build();

        log.info("S3Presigner 초기화 완료");
        return s3Presigner;
    }
}