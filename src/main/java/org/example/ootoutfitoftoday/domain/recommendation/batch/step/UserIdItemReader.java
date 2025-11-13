package org.example.ootoutfitoftoday.domain.recommendation.batch.step;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.user.repository.UserRepository;
import org.springframework.batch.item.ItemReader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Iterator;

/**
 * 활성 사용자 ID를 페이징 방식으로 읽어오는 ItemReader
 * 페이징 방식으로 대용량 데이터를 효율적으로 처리합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserIdItemReader implements ItemReader<Long> {

    private static final int PAGE_SIZE = 100;
    private final UserRepository userRepository;
    private int currentPage = 0;
    private Iterator<Long> currentIterator;
    private boolean hasMorePages = true;

    @Override
    public Long read() {
        // 현재 페이지의 데이터를 모두 읽었거나 처음 실행인 경우
        if (currentIterator == null || !currentIterator.hasNext()) {
            if (!hasMorePages) {

                return null; // 모든 데이터 읽기 완료
            }

            // 다음 페이지 로드
            Pageable pageable = PageRequest.of(currentPage, PAGE_SIZE);
            Page<Long> page = userRepository.findAllActiveUserIds(pageable);

            if (page.isEmpty()) {
                log.debug("No more users to process");

                return null;
            }

            currentIterator = page.getContent().iterator();
            hasMorePages = page.hasNext();
            currentPage++;

            log.debug("Loaded page {} with {} users", currentPage, page.getNumberOfElements());
        }

        return currentIterator.hasNext() ? currentIterator.next() : null;
    }
}