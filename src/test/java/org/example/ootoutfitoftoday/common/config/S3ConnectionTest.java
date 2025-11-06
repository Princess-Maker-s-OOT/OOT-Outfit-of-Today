package org.example.ootoutfitoftoday.common.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;

@ActiveProfiles("test")
@SpringBootTest
public class S3ConnectionTest {

    private static final String BUCKET_NAME = "oot-dev-image";

    @Autowired
    private S3Client s3Client;

    @Test
    void s3BucketObjectsTest() {

        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(BUCKET_NAME)
                .maxKeys(10)
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(request);

        if (response.contents().isEmpty()) {
            System.out.println("⚠️ 버킷이 비어있습니다.");
        } else {
            response.contents().forEach(obj ->
                    System.out.println("✅ Object: " + obj.key())
            );
        }
    }
}
