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

    // SRID 4326: WGS84 좌표계
    private static final int SRID = 4326;
    private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), SRID);
    // 기본 검색 키워드들
    private static final List<String> DEFAULT_KEYWORDS = List.of(
            "의류기부",
            "헌옷수거함",
            "아름다운가게",
            "의류수거함"
    );
    private final KakaoMapClient kakaoMapClient;
    private final DonationCenterCommandService donationCenterCommandService;

    /**
     * 주변 기부처 검색
     *
     * @param latitude  사용자 위치의 위도
     * @param longitude 사용자 위치의 경도
     * @param radius    검색 반경 (미터, 기본값: 5000)
     * @param keyword   검색 키워드 (선택사항, 없으면 기본 키워드로 검색)
     * @return 거리순으로 정렬된 기부처 목록
     */
    @Override
    @Transactional
    public List<DonationCenterSearchResponse> searchNearbyDonationCenters(
            Double latitude,
            Double longitude,
            Integer radius,
            String keyword
    ) {

        // 좌표 유효성 검증
        validateCoordinates(latitude, longitude);

        // 기본값 설정
        final Integer searchRadius = (radius == null || radius <= 0) ? 5000 : radius;  // 기본 5km

        log.info("기부처 검색 시작: latitude={}, longitude={}, radius={}, keyword={}",
                latitude, longitude, searchRadius, keyword);

        // 키워드가 지정된 경우 해당 키워드로만 검색, 아니면 기본 키워드들로 검색
        List<String> searchKeywords = (keyword != null && !keyword.isBlank())
                ? List.of(keyword)
                : DEFAULT_KEYWORDS;

        // 각 키워드로 검색하여 결과를 합침 (중복 제거)
        List<DonationCenterSearchResponse> allResults = searchKeywords.stream()
                .flatMap(kw -> searchByKeyword(kw, latitude, longitude, searchRadius).stream())
                .distinct()  // 중복 제거 (kakaoPlaceId 기준으로 중복이 발생할 수 있음)
                .sorted(Comparator.comparing(
                        DonationCenterSearchResponse::distance,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .collect(Collectors.toList());

        log.info("기부처 검색 완료: 총 {}개 발견", allResults.size());

        return allResults;
    }

    //특정 키워드로 기부처 검색
    private List<DonationCenterSearchResponse> searchByKeyword(
            String keyword,
            Double latitude,
            Double longitude,
            Integer radius
    ) {

        // 카카오맵 API 호출
        KakaoPlaceResponse response = kakaoMapClient.searchByKeyword(
                keyword,
                String.valueOf(longitude),  // 카카오맵 API는 x에 경도
                String.valueOf(latitude),   // y에 위도
                radius,
                null,  // page
                15     // size: 최대 15개
        );

        if (response == null || response.documents() == null || response.documents().isEmpty()) {
            log.debug("검색 결과 없음: keyword={}", keyword);
            return List.of();
        }

        // 응답받은 각 장소를 처리하고 DB에 저장
        return response.documents().stream()
                .map(this::processDonationCenter)
                .collect(Collectors.toList());
    }

    private DonationCenterSearchResponse processDonationCenter(KakaoPlaceResponse.Document document) {

        // Point 객체 생성
        Point location = createPoint(
                Double.parseDouble(document.x()),  // 경도
                Double.parseDouble(document.y())   // 위도
        );

        // CommandService를 통해 기부처 생성 또는 조회 (Command 책임 분리)
        DonationCenter center = donationCenterCommandService.createOrGet(
                document.id(),
                document.placeName(),
                document.roadAddressName() != null && !document.roadAddressName().isBlank()
                        ? document.roadAddressName()
                        : document.addressName(),
                document.phone() != null && !document.phone().isBlank()
                        ? document.phone()
                        : null,
                location,
                document.categoryName()
        );

        // 거리 정보 포함하여 응답 DTO 생성
        Integer distance = document.distance() != null && !document.distance().isBlank()
                ? Integer.parseInt(document.distance())
                : null;

        return DonationCenterSearchResponse.fromWithDistance(center, distance);
    }

    // 좌표 유효성 검증
    private void validateCoordinates(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            throw new DonationException(DonationErrorCode.INVALID_COORDINATES);
        }

        // 위도는 -90 ~ 90, 경도는 -180 ~ 180
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            throw new DonationException(DonationErrorCode.INVALID_COORDINATES);
        }
    }

    // Point 객체 생성
    private Point createPoint(double longitude, double latitude) {

        Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        point.setSRID(SRID);

        return point;
    }
}
