package org.example.ootoutfitoftoday.common.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpRequestUtil {

    /**
     * 클라이언트 실제 IP 추출
     * X-Forwarded-For, X-Real-IP 헤더 우선 확인(프록시/로드밸런서 대응)
     */
    public static String getClientIp(HttpServletRequest request) {

        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty()) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        // X-Forwarded-For는 "client, proxy1, proxy2" 형식일 수 있음
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}
