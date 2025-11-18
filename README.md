# OOT (Outfit of Today)
![img.png](img.png)

## 1. 프로젝트 개요

OOT는 **의류 기반 중고 거래 플랫폼**과 **디지털 옷장 관리 서비스**를 결합하여, 옷의 활용도를 높이고 의류 순환을 통한 환경 보호에 기여합니다.

- 회원 대상: 옷장 관리, 중고 거래, 추천 기능
- 비회원 대상: 공개 옷장 및 중고 거래글 조회
- 옷 기부 및 판매 추천, 소셜 로그인·멀티 디바이스 관리 지원
- 직거래 중심, 독립적 거래(옷장 없이도 거래 가능)

## 기간

- 2025년 10월 13일 ~ 2025년 11월 18일

---

## 2. 기술 스택

**Language/Backend**

- Java 17
- Spring Boot, Spring Data JPA, QueryDSL
- Spring Batch
- Gradle, Lombok

**Frontend**

- Next.js 15.3.3 (App Router)
- TypeScript
- Tailwind CSS
- shadcn/ui 
- Recharts

**Security**

- JWT, Spring Security, OAuth 2.0

**Test/문서화**

- Swagger, Postman

**IDE**

- IntelliJ IDEA, VS Code

**협업/설계**

- Github, Slack, Notion, ERDCloud, Figma, Zep, draw.io

**외부 API**

- Google(OAuth 2.0), Kakao Maps, Toss Payments

**실시간 통신**

- WebSocket

**클라우드**

- AWS EC2, S3, VPC, RDS

**배포/인프라**

- Git Actions, Docker

**모니터링**

- Prometheus, Grafana, Loki, K6, Promtail, Redis Insight

**DB**

- MySQL 8.0, Redis

---

## 3. 아키텍처/ERD

- **전체 구조**: SRP(Single Responsibility Principle) 단일 책임 원칙 구조, React 프론트엔드, AWS EC2 배포
- **API 명세·와이어프레임**: Swagger 문서, Figma/ERDCloud로 설계
- **API 명세서**
- 
- **와이어프레임**
- 
- **ERD**
![img_1.png](img_1.png)
- **주요 엔티티**: User, Closet, Clothes, WearRecord, SalePost, Category, Chat, DonationCenter(의류 기부처) 등
- **DB**: MySQL(관계 및 GIS 포인트 저장), Redis(캐시 및 통계 집계)
- **배치 작업**: Spring Batch, 매일 새벽 2시 미착용 옷 자동 감지 및 추천 생성, 매일 자정(00시 00분) 사용자 대시보드 최신화

---

## 4. 주요기능/명세

- **회원/비회원 권한 관리, 소셜 로그인(구글)·멀티 디바이스 지원, 관리자 시드/권한 분리**
- **디지털 옷장 생성, 옷 등록 및 착용 기록, 1년 미사용 옷 자동 추천(기부/판매)**
- **상품 판매 및 관리: 상태·카테고리·키워드 검색, 거래 안정성(상태 제어)**
- **판매자/구매자 1:1 채팅(WebSocket)**
- **위치 기반 의류 기부처 실시간 탐색(KakaoMap API·MySQL GIS 연계)**
- **카테고리 계층구조, BFS로 탐색, 순환참조 방지**
- **대시보드: 일/주/월별 통계, Redis 인메모리 캐싱, 빠른 응답, 성능 최적화**
- **거래·결제, TossPayments 연동 에스크로 처리**

---
## 5. 디렉토리 구조

### 백엔드 (Spring Boot)

---

## 6. 실행법

## 개발환경 구축

1. **백엔드**
    - Java 17 설치, Gradle로 의존성 설치
    - `docker-compose.yml`로 MySQL/Redis 컨테이너 실행
    - `application.yml`에 환경정보 세팅
    - IDE로 실행
2. **프론트엔드**
    - Node.js LTS 설치
    - 의존성 설치: `npm install`
    - 개발 서버 실행: `npm run dev`
3. **배포**
    - AWS EC2, GitHub Actions, Docker 기반 배포 스크립트 사용
    - RDS, S3 외부 연결(API Key 필요)

---

## 7. 개발 프로세스

### 개발 워크플로우

1. **이슈 생성**: GitHub Issues에서 기능/버그 이슈 작성
2. **브랜치 생성**: `feat/도메인 - 이슈 번호` 형식으로 브랜치 생성
3. **개발 및 커밋**:
   - 커밋 메시지 규칙: `[타입] 제목` (예: `[Feat] 옷장 조회 API 구현`)
   - 타입: `Feat`, `Fix`, `Refactor`, `Docs`, `Test`, `Chore`
4. **Pull Request**:
   - PR 템플릿에 따라 작성
   - 최소 1명 이상의 코드 리뷰 필수
5. **코드 리뷰 및 머지**: 리뷰 승인 후 `develop` 브랜치로 머지
6. **배포**:
   - `develop` → `staging` (테스트 환경)
   - `main` → `production` (운영 환경)
   - GitHub Actions를 통한 자동 배포

### 브랜치 전략

- `main`: 운영 환경 배포 브랜치
- `develop`: 개발 환경 통합 브랜치
- `feature/*`: 기능 개발 브랜치
- `fix/*`: 버그 수정 브랜치
- `hotfix/*`: 긴급 수정 브랜치

### 협업 도구

- **코드 관리**: GitHub (PR, Issue, Projects)
- **커뮤니케이션**: Slack (알림 연동, 일일 스탠드업)
- **문서화**: Notion (회의록, 기술 스펙, API 명세)
- **디자인**: Figma (와이어프레임, UI/UX)
- **일정 관리**: GitHub Projects (칸반 보드)

### 코드 리뷰 체크리스트

- [ ] 코드가 프로젝트 코딩 컨벤션을 따르는가?
- [ ] 불필요한 주석이나 콘솔 로그가 제거되었는가?
- [ ] 테스트 코드가 작성되었는가?
- [ ] API 문서(Swagger)가 업데이트되었는가?
- [ ] 성능이나 보안 이슈가 없는가?

### CI/CD 파이프라인

**백엔드**
1. GitHub Actions 트리거 (push/PR to develop/main)
2. Gradle 빌드 및 테스트 실행
3. Docker 이미지 빌드
4. AWS ECR에 푸시
5. EC2 인스턴스에 배포
6. Health check 및 Slack 알림

**프론트엔드**
1. GitHub Actions 트리거
2. npm 빌드 및 테스트
3. Docker 이미지 빌드 또는 정적 파일 생성
4. S3/CloudFront 배포 또는 EC2 배포
5. 배포 완료 알림

---

## 8. 이슈/트러블슈팅

- 괜찮은 이슈 한두개

---

## 9. 팀원/라이선스

## 팀원 (GitHub 기준)

- [**zerone1202**](https://github.com/zerone1202)
- [**cjn404**](https://github.com/cjn404)
- [**Byeongsu-cmd**](https://github.com/Byeongsu-cmd)
- [**AllSungho**](https://github.com/AllSungho)
- [**assokk**](https://github.com/assokk)

## 라이선스

- MIT License

---

**추가 세부 트러블 슈팅 및 이슈는 Wiki 참조**