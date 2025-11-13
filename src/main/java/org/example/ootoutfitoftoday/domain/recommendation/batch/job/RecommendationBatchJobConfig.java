package org.example.ootoutfitoftoday.domain.recommendation.batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.recommendation.batch.dto.RecommendationBatchResult;
import org.example.ootoutfitoftoday.domain.recommendation.batch.listener.RecommendationJobExecutionListener;
import org.example.ootoutfitoftoday.domain.recommendation.batch.listener.RecommendationStepExecutionListener;
import org.example.ootoutfitoftoday.domain.recommendation.batch.step.RecommendationItemProcessor;
import org.example.ootoutfitoftoday.domain.recommendation.batch.step.RecommendationItemWriter;
import org.example.ootoutfitoftoday.domain.recommendation.batch.step.UserIdItemReader;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 추천 생성 배치 Job 설정
 * <p>
 * Job 구조:
 * - Job: recommendationJob
 * - Step: generateRecommendationsStep
 * - Reader: UserIdItemReader (사용자 ID 페이징 읽기)
 * - Processor: RecommendationItemProcessor (추천 생성)
 * - Writer: RecommendationItemWriter (추천 저장)
 * <p>
 * 주요 기능:
 * - Chunk 단위 처리 (기본 100개)
 * - 재시도 정책: DB 예외 발생 시 최대 3회 재시도
 * - Skip 정책: 개별 사용자 처리 실패 시 건너뛰기 (최대 10개)
 * - 트랜잭션 관리: Chunk 단위 트랜잭션
 * - 메트릭 추적: JobExecutionListener, StepExecutionListener
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class RecommendationBatchJobConfig {

    /**
     * Chunk 크기: 한 번에 처리할 사용자 수
     * 기존 스케줄러의 PAGE_SIZE와 동일하게 설정
     */
    private static final int CHUNK_SIZE = 100;

    /**
     * 재시도 최대 횟수
     */
    private static final int RETRY_LIMIT = 3;

    /**
     * Skip 최대 횟수
     */
    private static final int SKIP_LIMIT = 10;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    // Step 컴포넌트
    private final UserIdItemReader userIdItemReader;
    private final RecommendationItemProcessor recommendationItemProcessor;
    private final RecommendationItemWriter recommendationItemWriter;

    // Listener
    private final RecommendationJobExecutionListener jobExecutionListener;
    private final RecommendationStepExecutionListener stepExecutionListener;

    /**
     * 추천 생성 배치 Job
     */
    @Bean
    public Job recommendationJob() {

        return new JobBuilder("recommendationJob", jobRepository)
                .listener(jobExecutionListener)
                .start(generateRecommendationsStep())
                .build();
    }

    /**
     * 추천 생성 Step
     * Reader -> Processor -> Writer 순서로 Chunk 단위 처리
     */
    @Bean
    public Step generateRecommendationsStep() {

        return new StepBuilder("generateRecommendationsStep", jobRepository)
                .<Long, RecommendationBatchResult>chunk(CHUNK_SIZE, transactionManager)
                .reader(userIdItemReader)
                .processor(recommendationItemProcessor)
                .writer(recommendationItemWriter)
                .listener(stepExecutionListener)

                // 재시도 정책: DB 예외 발생 시 재시도
                .faultTolerant()
                .retry(DataAccessException.class)
                .retryLimit(RETRY_LIMIT)

                // Skip 정책: 개별 사용자 처리 실패 시 건너뛰기
                .skip(Exception.class)
                .skipLimit(SKIP_LIMIT)
                .build();
    }
}