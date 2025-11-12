package org.example.ootoutfitoftoday.domain.salepost.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.io.Serializable;
import java.util.List;

/**
 * Slice를 Redis 캐시에 저장하기 위한 직렬화 가능한 DTO
 * SliceImpl은 기본 생성자가 없어 Jackson 역직렬화 불가능하므로 별도 DTO 필요
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CachedSliceResponse<T> implements Serializable {

    private List<T> content;
    private boolean hasNext;
    private int pageNumber;
    private int pageSize;

    /**
     * Slice를 캐시 가능한 DTO로 변환
     */
    public static <T> CachedSliceResponse<T> from(Slice<T> slice) {
        return new CachedSliceResponse<>(
                slice.getContent(),
                slice.hasNext(),
                slice.getNumber(),
                slice.getSize()
        );
    }

    /**
     * 캐시된 DTO를 다시 Slice로 변환
     */
    public Slice<T> toSlice() {
        return new SliceImpl<>(content, org.springframework.data.domain.PageRequest.of(pageNumber, pageSize), hasNext);
    }
}
