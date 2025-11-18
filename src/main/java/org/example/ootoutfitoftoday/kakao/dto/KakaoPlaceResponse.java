package org.example.ootoutfitoftoday.kakao.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoPlaceResponse(

        @JsonProperty("meta")
        Meta meta,

        @JsonProperty("documents")
        List<Document> documents
) {
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
        public BigDecimal getLatitude() {
            return y != null ? new BigDecimal(y) : null;
        }

        public BigDecimal getLongitude() {
            return x != null ? new BigDecimal(x) : null;
        }
    }
}