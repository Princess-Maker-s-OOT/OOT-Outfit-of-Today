package org.example.ootoutfitoftoday.common.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    private final String region;
    private final String accessKey;
    private final String secretKey;

    public S3Config() {
        // .env 파일 로드
        Dotenv dotenv = Dotenv.configure()
                .directory("./") // .env가 프로젝트 루트에 있을 경우
                .filename(".env")
                .load();

        this.region = dotenv.get("AWS_REGION").trim();
        this.accessKey = dotenv.get("AWS_ACCESS_KEY_ID").trim();
        this.secretKey = dotenv.get("AWS_SECRET_ACCESS_KEY").trim();

        // AWS SDK에서 읽도록 시스템 프로퍼티 세팅
        System.setProperty("aws.region", this.region);
        System.setProperty("aws.accessKeyId", this.accessKey);
        System.setProperty("aws.secretAccessKey", this.secretKey);
    }

    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
}