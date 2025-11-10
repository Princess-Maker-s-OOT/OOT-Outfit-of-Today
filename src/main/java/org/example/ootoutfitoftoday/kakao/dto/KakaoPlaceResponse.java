package org.example.ootoutfitoftoday.kakao.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

/**
 * 카카오맵 장소 검색 API 응답 DTO
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoPlaceResponse(

        @JsonProperty("meta")
        Meta meta,

        @JsonProperty("documents")
        List<Document> documents
) {

    /**
     * 검색 메타 정보
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Meta(

            @JsonProperty("total_count")
            Integer totalCount,

            @JsonProperty("pageable_count")
            Integer pageableCount,

            @JsonProperty("is_end")
            Boolean isEnd,

            @JsonProperty("same_name")
            SameName sameName
    ) {
    }

    /**
     * 질의어의 지역 및 키워드 분석 정보
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SameName(

            @JsonProperty("region")
            List<String> region,

            @JsonProperty("keyword")
            String keyword,

            @JsonProperty("selected_region")
            String selectedRegion
    ) {
    }

    /**
     * 장소 상세 정보
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Document(

            @JsonProperty("id")
            String id,

            @JsonProperty("place_name")
            String placeName,

            @JsonProperty("category_name")
            String categoryName,

            @JsonProperty("category_group_code")
            String categoryGroupCode,

            @JsonProperty("category_group_name")
            String categoryGroupName,

            @JsonProperty("phone")
            String phone,

            @JsonProperty("address_name")
            String addressName,

            @JsonProperty("road_address_name")
            String roadAddressName,

            @JsonProperty("x")
            String x, // longitude (경도)

            @JsonProperty("y")
            String y, // latitude (위도)

            @JsonProperty("place_url")
            String placeUrl,

            @JsonProperty("distance")
            String distance
    ) {

        // 위도를 BigDecimal로 반환
        public BigDecimal getLatitude() {
            return y != null ? new BigDecimal(y) : null;
        }

        // 경도를 BigDecimal로 반환
        public BigDecimal getLongitude() {
            return x != null ? new BigDecimal(x) : null;
        }
    }
}