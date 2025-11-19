package org.example.ootoutfitoftoday.domain.salepost.util;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public class NativeQuerySortUtil {

    private static final String DEFAULT_SORT_COLUMN = "created_at";
    private static final String DEFAULT_DIRECTION = "DESC";

    private static final List<String> ALLOWED_SORT_COLUMNS = List.of("id", "title", "price", DEFAULT_SORT_COLUMN);

    public static String buildOrderClause(
            String baseSql,
            Pageable pageable) {
        Sort sort = pageable.getSort();

        if (sort.isUnsorted() || sort.isEmpty()) {
            return String.format("%s ORDER BY s.%s %s", baseSql, DEFAULT_SORT_COLUMN, DEFAULT_DIRECTION);
        }

        Sort.Order order = sort.stream().findFirst().get();

        String property = order.getProperty();
        String direction = order.getDirection().name();

        String dbColumn = convertToSnakeCase(property);

        if (!ALLOWED_SORT_COLUMNS.contains(dbColumn)) {
            dbColumn = DEFAULT_SORT_COLUMN;
            direction = DEFAULT_DIRECTION;
        }

        return String.format("%s ORDER BY s.%s %s", baseSql, dbColumn, direction);
    }

    private static String convertToSnakeCase(String camelCase) {

        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}