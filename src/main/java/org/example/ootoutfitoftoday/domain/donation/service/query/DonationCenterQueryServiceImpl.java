package org.example.ootoutfitoftoday.domain.donation.service.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.donation.dto.response.DonationCenterSearchResponse;
import org.example.ootoutfitoftoday.domain.donation.entity.DonationCenter;
import org.example.ootoutfitoftoday.domain.donation.exception.DonationErrorCode;
import org.example.ootoutfitoftoday.domain.donation.exception.DonationException;
import org.example.ootoutfitoftoday.domain.donation.service.command.DonationCenterCommandService;
import org.example.ootoutfitoftoday.kakao.client.KakaoMapClient;
import org.example.ootoutfitoftoday.kakao.dto.KakaoPlaceResponse;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DonationCenterQueryServiceImpl implements DonationCenterQueryService {

    private static final int SRID = 4326;
    private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), SRID);
    private static final List<String> DEFAULT_KEYWORDS = List.of(
            "의류기부",
            "헌옷수거함",
            "아름다운가게",
            "의류수거함"
    );
    private final KakaoMapClient kakaoMapClient;
    private final DonationCenterCommandService donationCenterCommandService;

    @Override
    @Transactional(readOnly = true)
    public List<DonationCenterSearchResponse> searchNearbyDonationCenters(
            Double latitude,
            Double longitude,
            Integer radius,
            String keyword
    ) {
        long methodStartTime = System.currentTimeMillis();
        log.debug("searchNearbyDonationCenters 메서드 시작");

        validateCoordinates(latitude, longitude);
        log.debug("좌표 유효성 검증 통과 - 위도: {}, 경도: {}", latitude, longitude);

        final Integer searchRadius = (radius == null || radius <= 0) ? 5000 : radius;
        log.debug("검색 반경 설정 - 입력값: {}, 적용값: {}m", radius, searchRadius);

        log.info("기부처 검색 시작 - 위도: {}, 경도: {}, 반경: {}m, 키워드: {}",
                latitude, longitude, searchRadius, keyword);

        List<String> searchKeywords = (keyword != null && !keyword.isBlank())
                ? List.of(keyword)
                : DEFAULT_KEYWORDS;

        log.debug("검색 키워드 목록: {} (총 {}개)", searchKeywords, searchKeywords.size());

        long searchStartTime = System.currentTimeMillis();
        List<DonationCenterSearchResponse> allResults = searchKeywords.stream()
                .flatMap(kw -> searchByKeyword(kw, latitude, longitude, searchRadius).stream())
                .distinct()
                .sorted(Comparator.comparing(
                        DonationCenterSearchResponse::distance,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .collect(Collectors.toList());

        long searchTime = System.currentTimeMillis() - searchStartTime;
        log.debug("키워드 검색 및 정렬 완료 - 소요시간: {}ms", searchTime);

        long totalTime = System.currentTimeMillis() - methodStartTime;
        log.info("기부처 검색 완료 - 총 {}개 발견, 전체 소요시간: {}ms", allResults.size(), totalTime);

        if (allResults.isEmpty()) {
            log.warn("기부처 검색 결과 없음 - 위도: {}, 경도: {}, 반경: {}m", latitude, longitude, searchRadius);
        }

        return allResults;
    }

    private List<DonationCenterSearchResponse> searchByKeyword(
            String keyword,
            Double latitude,
            Double longitude,
            Integer radius
    ) {
        long apiStartTime = System.currentTimeMillis();
        log.debug("카카오맵 API 호출 시작 - 키워드: {}, 위도: {}, 경도: {}, 반경: {}m",
                keyword, latitude, longitude, radius);

        KakaoPlaceResponse response = kakaoMapClient.searchByKeyword(
                keyword,
                String.valueOf(longitude),
                String.valueOf(latitude),
                radius,
                null,
                15
        );

        long apiTime = System.currentTimeMillis() - apiStartTime;
        log.debug("카카오맵 API 호출 완료 - 키워드: {}, 소요시간: {}ms", keyword, apiTime);

        if (response == null || response.documents() == null || response.documents().isEmpty()) {
            log.debug("검색 결과 없음 - 키워드: {}", keyword);
            return List.of();
        }

        log.debug("카카오맵 API 응답 - 키워드: {}, 결과 건수: {}, 전체 건수: {}",
                keyword, response.documents().size(),
                response.meta() != null ? response.meta().totalCount() : "N/A");

        long processingStartTime = System.currentTimeMillis();
        List<DonationCenterSearchResponse> results = response.documents().stream()
                .map(this::processDonationCenter)
                .collect(Collectors.toList());

        long processingTime = System.currentTimeMillis() - processingStartTime;
        log.debug("기부처 처리 완료 - 키워드: {}, 처리 건수: {}, 소요시간: {}ms",
                keyword, results.size(), processingTime);

        return results;
    }

    private DonationCenterSearchResponse processDonationCenter(KakaoPlaceResponse.Document document) {
        log.trace("기부처 처리 시작 - 장소명: {}, 카카오ID: {}", document.placeName(), document.id());

        double longitude = Double.parseDouble(document.x());
        double latitude = Double.parseDouble(document.y());
        log.trace("좌표 파싱 - 카카오ID: {}, 경도: {}, 위도: {}", document.id(), longitude, latitude);

        Point location = createPoint(longitude, latitude);

        String address = document.roadAddressName() != null && !document.roadAddressName().isBlank()
                ? document.roadAddressName()
                : document.addressName();
        log.trace("주소 결정 - 카카오ID: {}, 도로명: {}, 지번: {}, 선택: {}",
                document.id(), document.roadAddressName(), document.addressName(), address);

        String phoneNumber = document.phone() != null && !document.phone().isBlank()
                ? document.phone()
                : null;

        DonationCenter center = donationCenterCommandService.createOrGet(
                document.id(),
                document.placeName(),
                address,
                phoneNumber,
                location,
                document.categoryName()
        );

        Integer distance = document.distance() != null && !document.distance().isBlank()
                ? Integer.parseInt(document.distance())
                : null;

        log.trace("기부처 처리 완료 - 기부처ID: {}, 카카오ID: {}, 장소명: {}, 거리: {}m",
                center.getId(), document.id(), document.placeName(), distance);

        return DonationCenterSearchResponse.fromWithDistance(center, distance);
    }

    private void validateCoordinates(Double latitude, Double longitude) {
        log.trace("좌표 유효성 검증 시작 - 위도: {}, 경도: {}", latitude, longitude);

        if (latitude == null || longitude == null) {
            log.warn("좌표값 누락 - 위도: {}, 경도: {}", latitude, longitude);
            throw new DonationException(DonationErrorCode.INVALID_COORDINATES);
        }

        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            log.warn("잘못된 좌표 범위 - 위도: {} (유효범위: -90~90), 경도: {} (유효범위: -180~180)",
                    latitude, longitude);
            throw new DonationException(DonationErrorCode.INVALID_COORDINATES);
        }

        boolean isInKorea = latitude >= 33.0 && latitude <= 43.0
                && longitude >= 124.0 && longitude <= 132.0;
        if (!isInKorea) {
            log.warn("대한민국 범위 외 좌표 - 위도: {}, 경도: {} (대한민국: 위도 33~43, 경도 124~132)",
                    latitude, longitude);
        }

        log.trace("좌표 유효성 검증 완료");
    }

    private Point createPoint(double longitude, double latitude) {
        log.trace("Point 객체 생성 - 경도: {}, 위도: {}, SRID: {}", longitude, latitude, SRID);

        Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        point.setSRID(SRID);

        log.trace("Point 객체 생성 완료 - WKT: {}", point.toText());

        return point;
    }
}