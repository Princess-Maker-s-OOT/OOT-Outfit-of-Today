package org.example.ootoutfitoftoday.kakao.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.kakao.config.KakaoMapProperties;
import org.example.ootoutfitoftoday.kakao.dto.KakaoPlaceResponse;
import org.example.ootoutfitoftoday.kakao.exception.KakaoMapErrorCode;
import org.example.ootoutfitoftoday.kakao.exception.KakaoMapException;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoMapClient {

    private static final String KEYWORD_SEARCH_PATH = "/v2/local/search/keyword.json"; // 카카오 키워드 장소 검색 API의 경로
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String KAKAO_AK_PREFIX = "KakaoAK "; // 카카오 API 키 앞에 붙여야 하는 인증 타입 접두사(KakaoAK)

    private final RestTemplate restTemplate;
    private final KakaoMapProperties kakaoMapProperties;

    /**
     * 키워드로 장소 검색
     *
     * @param keyword 검색 키워드
     * @param x       중심 x좌표 (경도, longitude)
     * @param y       중심 y좌표 (위도, latitude)
     * @param radius  검색 반경 (미터)
     * @param page    결과 페이지 번호 (1~45, 기본값 1)
     * @param size    한 페이지에 보여질 문서 개수 (1~15, 기본값 15)
     * @return KakaoPlaceResponse 검색 결과
     */
    public KakaoPlaceResponse searchByKeyword(
            String keyword,
            String x,
            String y,
            Integer radius,
            Integer page,
            Integer size
    ) {
        validateKeyword(keyword); // 매개변수가 유효한지 검증

        URI uri = buildSearchUri(keyword, x, y, radius, page, size);
        HttpHeaders headers = createHeaders();

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            log.info("카카오맵 API 호출: keyword={}, x={}, y={}, radius={}", keyword, x, y, radius);

            ResponseEntity<KakaoPlaceResponse> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    entity,
                    KakaoPlaceResponse.class
            );

            KakaoPlaceResponse body = response.getBody();
            if (body == null) {
                throw new KakaoMapException(KakaoMapErrorCode.INVALID_API_RESPONSE);
            }

            log.info("카카오맵 API 호출 성공: totalCount={}", body.getMeta().getTotalCount());
            return body;

        } catch (HttpClientErrorException e) {
            log.error("카카오맵 API 클라이언트 에러: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            handleClientError(e);
            throw new KakaoMapException(KakaoMapErrorCode.API_CALL_FAILED);

        } catch (HttpServerErrorException e) {
            log.error("카카오맵 API 서버 에러: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new KakaoMapException(KakaoMapErrorCode.API_CALL_FAILED);

        } catch (Exception e) {
            log.error("카카오맵 API 호출 중 예상치 못한 오류 발생", e);
            throw new KakaoMapException(KakaoMapErrorCode.API_CALL_FAILED);
        }
    }

    /**
     * 키워드로 장소 검색
     *
     * @param keyword 검색 키워드
     * @return KakaoPlaceResponse 검색 결과
     */
    public KakaoPlaceResponse searchByKeyword(String keyword) {
        return searchByKeyword(keyword, null, null, null, null, null);
    }

    private void validateKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new KakaoMapException(KakaoMapErrorCode.INVALID_SEARCH_KEYWORD);
        }
    }

    private URI buildSearchUri(
            String keyword,
            String x,
            String y,
            Integer radius,
            Integer page,
            Integer size
    ) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(kakaoMapProperties.getBaseUrl())
                .path(KEYWORD_SEARCH_PATH)
                .queryParam("query", keyword);

        if (x != null && !x.trim().isEmpty()) {
            builder.queryParam("x", x);
        }
        if (y != null && !y.trim().isEmpty()) {
            builder.queryParam("y", y);
        }
        if (radius != null) {
            builder.queryParam("radius", radius);
        }
        if (page != null) {
            builder.queryParam("page", page);
        }
        if (size != null) {
            builder.queryParam("size", size);
        }

        return builder.build().encode().toUri();
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION_HEADER, KAKAO_AK_PREFIX + kakaoMapProperties.getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private void handleClientError(HttpClientErrorException e) {
        if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            throw new KakaoMapException(KakaoMapErrorCode.INVALID_API_KEY);
        } else if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
            throw new KakaoMapException(KakaoMapErrorCode.API_QUOTA_EXCEEDED);
        }
    }
}