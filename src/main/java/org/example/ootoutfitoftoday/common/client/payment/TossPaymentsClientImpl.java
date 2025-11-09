package org.example.ootoutfitoftoday.common.client.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.payment.exception.PaymentErrorCode;
import org.example.ootoutfitoftoday.domain.payment.exception.PaymentException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentsClientImpl implements TossPaymentsClient {

    @Value("${toss.secret-key}")
    private String secretKey;

    @Value("${toss.api.url.confirm}")
    private String confirmUrl;

    private final @Qualifier("tossRestTemplate") RestTemplate restTemplate;

    @Override
    public void confirmPayment(
            String paymentKey,
            String orderId,
            BigDecimal amount
    ) {
        try {
            // Authorization 헤더
            String encodedAuth = Base64.getEncoder()
                    .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

            // 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + encodedAuth);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Body 설정
            Map<String, Object> body = new HashMap<>();
            body.put("paymentKey", paymentKey);
            body.put("orderId", orderId);
            body.put("amount", amount);

            // HTTP 요청
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            restTemplate.postForEntity(confirmUrl, entity, String.class);

        } catch (HttpClientErrorException e) {
            // 예: 잘못된 paymentKey (400 Bad Request)
            log.warn("토스 클라이언트 에러: {}", e.getResponseBodyAsString());
            throw new PaymentException(PaymentErrorCode.TOSS_API_CLIENT_ERROR);
        } catch (HttpServerErrorException e) {
            // 예: 토스 서버 장애 (500 Internal Server Error)
            log.error("토스 서버 에러: {}, body={}",e.getStatusCode(), e.getResponseBodyAsString());
            throw new PaymentException(PaymentErrorCode.TOSS_API_SERVER_ERROR);
        } catch (ResourceAccessException e) {
            // 예: 타임아웃 (10초 초과) 또는 네트워크 오류
            log.error("토스 API 타임아웃", e);
            throw new PaymentException(PaymentErrorCode.TOSS_API_TIMEOUT);
        } catch (Exception e) {
            // 그 외 예외
            log.error("토스 API 예외", e);
            throw new PaymentException(PaymentErrorCode.TOSS_API_ERROR);
        }
    }
}
