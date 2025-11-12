-- 관리자 계정 초기 데이터 삽입
-- 중복 방지: login_id가 'admin'인 사용자가 없을 때만 삽입
INSERT INTO users (login_id,
                   email,
                   nickname,
                   username,
                   password,
                   phone_number,
                   role,
                   trade_address,
                   trade_location,
                   login_type,
                   created_at,
                   updated_at,
                   is_deleted)
SELECT 'admin',
       'admin@oot.com',
       'admin',
       'admin',
       -- 비밀번호 'admin00!'을 BCrypt로 해싱한 값
       '$2a$12$h/HGvb09H7iScgkacf5KNu4UV60CZCu7CS5l7MEYFPJsKHCfP1Oz.', -- 애플리케이션에서 생성한 해시
       '01000000000',
       'ROLE_ADMIN',
       '서울특별시 중구 세종대로 110',
       ST_GeomFromText('POINT(37.56681294 126.97865509)', 4326),
       'LOGIN_ID',
       NOW(),
       NOW(),
       0                                                               -- is_deleted 기본값
FROM DUAL
WHERE NOT EXISTS (SELECT 1
                  FROM users
                  WHERE login_id = 'admin');

SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM categories;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO categories (id, name, parent_id, created_at, updated_at, is_deleted) VALUES
-- 최상위 카테고리
(1, '남성', NULL, NOW(), NOW(), false),
(2, '여성', NULL, NOW(), NOW(), false),
(3, '아동', NULL, NOW(), NOW(), false),

-- 남성 하위 카테고리
(4, '아우터', 1, NOW(), NOW(), false),
(5, '상의', 1, NOW(), NOW(), false),
(6, '하의', 1, NOW(), NOW(), false),
(7, '신발', 1, NOW(), NOW(), false),
(8, '가방', 1, NOW(), NOW(), false),
(9, '액세서리', 1, NOW(), NOW(), false),
(10, '이너웨어', 1, NOW(), NOW(), false),
(11, '라이프웨어', 1, NOW(), NOW(), false),

-- 남성 - 아우터
(12, '자켓', 4, NOW(), NOW(), false),
(13, '코트', 4, NOW(), NOW(), false),
(14, '패딩', 4, NOW(), NOW(), false),
(15, '가디건', 4, NOW(), NOW(), false),
(16, '블루종', 4, NOW(), NOW(), false),
(17, '후리스', 4, NOW(), NOW(), false),
(18, '후드집업', 4, NOW(), NOW(), false),

-- 남성 - 상의
(19, '반팔 티셔츠', 5, NOW(), NOW(), false),
(20, '긴팔 티셔츠', 5, NOW(), NOW(), false),
(21, '셔츠', 5, NOW(), NOW(), false),
(22, '맨투맨', 5, NOW(), NOW(), false),
(23, '니트', 5, NOW(), NOW(), false),
(24, '후드티', 5, NOW(), NOW(), false),
(25, '민소매', 5, NOW(), NOW(), false),

-- 남성 - 하의
(26, '청바지', 6, NOW(), NOW(), false),
(27, '슬랙스', 6, NOW(), NOW(), false),
(28, '조거팬츠', 6, NOW(), NOW(), false),
(29, '반바지', 6, NOW(), NOW(), false),
(30, '트레이닝팬츠', 6, NOW(), NOW(), false),

-- 남성 - 신발
(31, '스니커즈', 7, NOW(), NOW(), false),
(32, '부츠', 7, NOW(), NOW(), false),
(33, '샌들', 7, NOW(), NOW(), false),
(34, '로퍼', 7, NOW(), NOW(), false),
(35, '구두', 7, NOW(), NOW(), false),

-- 남성 - 가방
(36, '백팩', 8, NOW(), NOW(), false),
(37, '크로스백', 8, NOW(), NOW(), false),
(38, '클러치백', 8, NOW(), NOW(), false),
(39, '토트백', 8, NOW(), NOW(), false),

-- 남성 - 액세서리
(40, '모자', 9, NOW(), NOW(), false),
(41, '벨트', 9, NOW(), NOW(), false),
(42, '시계', 9, NOW(), NOW(), false),
(43, '팔찌', 9, NOW(), NOW(), false),
(44, '선글라스', 9, NOW(), NOW(), false),
(45, '머플러/스카프', 9, NOW(), NOW(), false),

-- 남성 - 이너웨어
(46, '러닝/탱크탑', 10, NOW(), NOW(), false),
(47, '드로즈/트렁크', 10, NOW(), NOW(), false),

-- 남성 - 라이프웨어
(48, '홈웨어', 11, NOW(), NOW(), false),
(49, '스포츠웨어', 11, NOW(), NOW(), false),

-- 여성 하위 카테고리
(50, '아우터', 2, NOW(), NOW(), false),
(51, '상의', 2, NOW(), NOW(), false),
(52, '하의', 2, NOW(), NOW(), false),
(53, '신발', 2, NOW(), NOW(), false),
(54, '가방', 2, NOW(), NOW(), false),
(55, '액세서리', 2, NOW(), NOW(), false),
(56, '원피스', 2, NOW(), NOW(), false),
(57, '이너웨어', 2, NOW(), NOW(), false),

-- 여성 - 아우터
(58, '코트', 50, NOW(), NOW(), false),
(59, '재킷', 50, NOW(), NOW(), false),
(60, '가디건', 50, NOW(), NOW(), false),
(61, '블루종', 50, NOW(), NOW(), false),
(62, '패딩', 50, NOW(), NOW(), false),

-- 여성 - 상의
(63, '블라우스', 51, NOW(), NOW(), false),
(64, '니트', 51, NOW(), NOW(), false),
(65, '티셔츠', 51, NOW(), NOW(), false),
(66, '후드티', 51, NOW(), NOW(), false),

-- 여성 - 하의
(67, '스커트', 52, NOW(), NOW(), false),
(68, '팬츠', 52, NOW(), NOW(), false),
(69, '반바지', 52, NOW(), NOW(), false),
(70, '슬랙스', 52, NOW(), NOW(), false),

-- 여성 - 신발
(71, '플랫슈즈', 53, NOW(), NOW(), false),
(72, '힐', 53, NOW(), NOW(), false),
(73, '부츠', 53, NOW(), NOW(), false),
(74, '스니커즈', 53, NOW(), NOW(), false),
(75, '로퍼', 53, NOW(), NOW(), false),

-- 여성 - 가방
(76, '크로스백', 54, NOW(), NOW(), false),
(77, '숄더백', 54, NOW(), NOW(), false),
(78, '토트백', 54, NOW(), NOW(), false),
(79, '미니백', 54, NOW(), NOW(), false),


-- 여성 - 액세서리
(80, '목걸이', 55, NOW(), NOW(), false),
(81, '귀걸이', 55, NOW(), NOW(), false),
(82, '팔찌', 55, NOW(), NOW(), false),
(83, '반지', 55, NOW(), NOW(), false),
(84, '헤어악세서리', 55, NOW(), NOW(), false),
(85, '스카프', 55, NOW(), NOW(), false),

-- 여성 - 원피스
(86, '캐주얼원피스', 56, NOW(), NOW(), false),
(87, '롱원피스', 56, NOW(), NOW(), false),
(88, '미니원피스', 56, NOW(), NOW(), false),

-- 여성 - 이너웨어
(89, '브라탑', 57, NOW(), NOW(), false),
(90, '팬티', 57, NOW(), NOW(), false),
(91, '스타킹', 57, NOW(), NOW(), false),

-- 아동 하위 카테고리
(92, '아우터', 3, NOW(), NOW(), false),
(93, '상의', 3, NOW(), NOW(), false),
(94, '하의', 3, NOW(), NOW(), false),
(95, '신발', 3, NOW(), NOW(), false),
(96, '액세서리', 3, NOW(), NOW(), false),

-- 아동 - 상의/하의/신발
(97, '티셔츠', 93, NOW(), NOW(), false),
(98, '바지', 94, NOW(), NOW(), false),
(99, '스니커즈', 95, NOW(), NOW(), false);