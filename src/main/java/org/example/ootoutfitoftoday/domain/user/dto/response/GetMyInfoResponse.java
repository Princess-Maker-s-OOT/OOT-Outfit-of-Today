package org.example.ootoutfitoftoday.domain.user.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.util.Location;
import org.example.ootoutfitoftoday.common.util.PointFormatAndParse;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.enums.UserRole;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.ALWAYS)
@Getter
@Builder
@RequiredArgsConstructor
public class GetMyInfoResponse {

    private final String imageUrl;
    private final String loginId;
    private final String email;
    private final String nickname;
    private final String username;
    private final String phoneNumber;
    private final UserRole role;
    private final String tradeAddress;
    private final BigDecimal tradeLatitude;
    private final BigDecimal tradeLongitude;

    public static GetMyInfoResponse from(User user) {
        Location location = PointFormatAndParse.parse(user.getTradeLocation());

        return GetMyInfoResponse.builder()
                .imageUrl(user.getImageUrl())
                .loginId(user.getLoginId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .username(user.getUsername())
                .phoneNumber(user.getPhoneNumber())
                .tradeAddress(user.getTradeAddress())
                .tradeLatitude(location.latitude())
                .tradeLongitude(location.longitude())
                .role(user.getRole())
                .build();
    }
}