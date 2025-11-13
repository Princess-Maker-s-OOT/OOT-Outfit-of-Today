package org.example.ootoutfitoftoday.common.config;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Batch 전역 설정
 * Spring Batch의 기본 인프라를 활성화합니다:
 * - JobRepository: Job 실행 메타데이터 관리
 * - JobLauncher: Job 실행 엔진
 * - JobExplorer: Job 실행 이력 조회
 * - JobRegistry: Job 등록 및 관리
 * <p>
 * 메타데이터 테이블 (자동 생성됨):
 * - BATCH_JOB_INSTANCE: Job의 논리적 실행 단위
 * - BATCH_JOB_EXECUTION: Job의 물리적 실행 이력
 * - BATCH_JOB_EXECUTION_PARAMS: Job 실행 파라미터
 * - BATCH_STEP_EXECUTION: Step 실행 이력
 * - BATCH_JOB_EXECUTION_CONTEXT: Job 실행 컨텍스트 (상태 저장)
 * - BATCH_STEP_EXECUTION_CONTEXT: Step 실행 컨텍스트
 * <p>
 * Spring Batch 5.x에서는 DefaultBatchConfiguration을 상속하면
 * JobRepository, TransactionManager 등이 자동 구성됨
 * 커스텀 설정이 필요 없으면 이 클래스만으로 충분
 */
@Configuration
@EnableBatchProcessing
public class BatchConfig {
}