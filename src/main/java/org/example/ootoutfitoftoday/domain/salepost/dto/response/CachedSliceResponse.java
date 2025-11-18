package org.example.ootoutfitoftoday.domain.salepost.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.io.Serializable;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CachedSliceResponse<T> implements Serializable {

    private List<T> content;
    private boolean hasNext;
    private int pageNumber;
    private int pageSize;

    public static <T> CachedSliceResponse<T> from(Slice<T> slice) {

        return new CachedSliceResponse<>(
                slice.getContent(),
                slice.hasNext(),
                slice.getNumber(),
                slice.getSize()
        );
    }

    public Slice<T> toSlice() {

        return new SliceImpl<>(content, org.springframework.data.domain.PageRequest.of(pageNumber, pageSize), hasNext);
    }
}