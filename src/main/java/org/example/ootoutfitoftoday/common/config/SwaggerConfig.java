package org.example.ootoutfitoftoday.common.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "bearerAuth", // 👈 @SecurityRequirement 에서 참조되는 이름
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info().title("OOT(Outfit of Today) | 디지털 옷장 관리 서비스 기반 의류 중고 거래 API")
                        .description("OOT(Outfit of Today) 백엔드 서비스의 공식 API 문서입니다.\n\n" +
                                "이 플랫폼은 **디지털 옷장 관리**를 핵심으로 제공하며, 이를 통해 " +
                                "**자주 안 입는 옷에 대해 대여 또는 판매를 추천**하여 옷의 활용도를 높입니다.\n\n" +
                                "**주요 특징:**\n" +
                                "1. **회원 기반 서비스:** 옷장 관리, 중고 거래는 회원 전용 기능입니다.\n" +
                                "2. **비회원 접근:** 공개 옷장 및 중고 거래 게시물 조회는 비회원도 가능합니다.\n" +
                                "3. **직거래 기반:** 모든 중고 거래는 사용자 간의 직거래를 기본으로 합니다.\n" +
                                "4. **독립적 거래:** 옷장 서비스를 이용하지 않는 회원도 판매 및 구매 활동에 참여할 수 있습니다.\n\n" +
                                "API는 인증, 옷장, 아이템, 중고 거래 게시물 등 모든 기능을 RESTful하게 제공합니다.")
                        .version("v1.0.0"));
    }
}