package org.example.ootoutfitoftoday.domain.salepost.util;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public class NativeQuerySortUtil {

    private static final String DEFAULT_SORT_COLUMN = "created_at";
    private static final String DEFAULT_DIRECTION = "DESC";

    // 💡 2. 허용된 DB 컬럼 목록 정의 (SQL 인젝션 방지 핵심)
    private static final List<String> ALLOWED_SORT_COLUMNS = List.of("id", "title", "price", DEFAULT_SORT_COLUMN); // "created_at" 포함

    /**
     * Pageable과 기본 SQL을 받아 ORDER BY 절이 추가된 최종 SQL을 반환합니다.
     *
     * @param baseSql  ORDER BY 절이 없는 기본 SQL 문자열
     * @param pageable Spring Data JPA의 Pageable 객체
     * @return ORDER BY 절이 추가된 최종 SQL 문자열
     */
    public static String buildOrderClause(
            String baseSql,
            Pageable pageable) {
        Sort sort = pageable.getSort();

        // --- 1. 정렬 기준이 없는 경우 (기본값 적용) ---
        if (sort.isUnsorted() || sort.isEmpty()) {
            return String.format("%s ORDER BY s.%s %s", baseSql, DEFAULT_SORT_COLUMN, DEFAULT_DIRECTION);
        }

        // --- 2. 정렬 기준이 있는 경우 ---

        // 첫 번째 정렬 기준만 사용합니다.
        Sort.Order order = sort.stream().findFirst().get();

        String property = order.getProperty();
        String direction = order.getDirection().name(); // String으로 DESC 또는 ASC

        // 자바 필드명(camelCase)을 DB 컬럼명(snake_case)으로 변환
        String dbColumn = convertToSnakeCase(property);

        // SQL 인젝션 방지: 허용된 컬럼이 아니면 기본값으로 대체
        if (!ALLOWED_SORT_COLUMNS.contains(dbColumn)) {
            dbColumn = DEFAULT_SORT_COLUMN;
            direction = DEFAULT_DIRECTION; // 방향도 기본값으로 되돌림
        }

        // 최종 SQL 문자열 조립
        return String.format("%s ORDER BY s.%s %s", baseSql, dbColumn, direction);
    }

    // 💡 Helper: CamelCase를 Snake_Case로 변환하는 간단한 로직 (실제로는 더 복잡할 수 있음)
    private static String convertToSnakeCase(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}
