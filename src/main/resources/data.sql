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

-- [2] 최상위 카테고리 (남성/여성/아동)
INSERT INTO categories (name, parent_id, created_at, updated_at, is_deleted)
SELECT '남성', NULL, NOW(), NOW(), FALSE
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = '남성' AND parent_id IS NULL);
INSERT INTO categories (name, parent_id, created_at, updated_at, is_deleted)
SELECT '여성', NULL, NOW(), NOW(), FALSE
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = '여성' AND parent_id IS NULL);
INSERT INTO categories (name, parent_id, created_at, updated_at, is_deleted)
SELECT '아동', NULL, NOW(), NOW(), FALSE
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = '아동' AND parent_id IS NULL);

-- [3] 남성 1단계 하위 카테고리
INSERT INTO categories (name, parent_id, created_at, updated_at, is_deleted)
SELECT sub.name, parent.id, NOW(), NOW(), FALSE
FROM (SELECT '아우터' AS name
      UNION
      SELECT '상의'
      UNION
      SELECT '하의'
      UNION
      SELECT '신발'
      UNION
      SELECT '가방'
      UNION
      SELECT '액세서리'
      UNION
      SELECT '이너웨어'
      UNION
      SELECT '라이프웨어') sub
         JOIN categories parent ON parent.name = '남성' AND parent.parent_id IS NULL
WHERE NOT EXISTS (SELECT 1
                  FROM categories
                  WHERE name = sub.name AND parent_id = parent.id);

-- [4] 남성 - 아우터 하위 카테고리
INSERT INTO categories (name, parent_id, created_at, updated_at, is_deleted)
SELECT sub.name, parent.id, NOW(), NOW(), FALSE
FROM (SELECT '자켓' AS name
      UNION
      SELECT '코트'
      UNION
      SELECT '패딩'
      UNION
      SELECT '가디건'
      UNION
      SELECT '블루종'
      UNION
      SELECT '후리스'
      UNION
      SELECT '후드집업') sub
         JOIN categories parent
              ON parent.name = '아우터'
                  AND parent.parent_id = (SELECT id FROM categories WHERE name = '남성' AND parent_id IS NULL)
WHERE NOT EXISTS (SELECT 1
                  FROM categories
                  WHERE name = sub.name AND parent_id = parent.id);

-- [5] 남성 - 상의 하위 카테고리
INSERT INTO categories (name, parent_id, created_at, updated_at, is_deleted)
SELECT sub.name, parent.id, NOW(), NOW(), FALSE
FROM (SELECT '반팔 티셔츠' AS name
      UNION
      SELECT '긴팔 티셔츠'
      UNION
      SELECT '셔츠'
      UNION
      SELECT '맨투맨'
      UNION
      SELECT '니트'
      UNION
      SELECT '후드티'
      UNION
      SELECT '민소매') sub
         JOIN categories parent
              ON parent.name = '상의'
                  AND parent.parent_id = (SELECT id FROM categories WHERE name = '남성' AND parent_id IS NULL)
WHERE NOT EXISTS (SELECT 1
                  FROM categories
                  WHERE name = sub.name AND parent_id = parent.id);

-- [6] 남성 - 하의 하위 카테고리
INSERT INTO categories (name, parent_id, created_at, updated_at, is_deleted)
SELECT sub.name, parent.id, NOW(), NOW(), FALSE
FROM (SELECT '청바지' AS name
      UNION
      SELECT '슬랙스'
      UNION
      SELECT '조거팬츠'
      UNION
      SELECT '반바지'
      UNION
      SELECT '트레이닝팬츠') sub
         JOIN categories parent
              ON parent.name = '하의'
                  AND parent.parent_id = (SELECT id FROM categories WHERE name = '남성' AND parent_id IS NULL)
WHERE NOT EXISTS (SELECT 1
                  FROM categories
                  WHERE name = sub.name AND parent_id = parent.id);

-- [7] 남성 - 신발 하위 카테고리
INSERT INTO categories (name, parent_id, created_at, updated_at, is_deleted)
SELECT sub.name, parent.id, NOW(), NOW(), FALSE
FROM (SELECT '스니커즈' AS name
      UNION
      SELECT '부츠'
      UNION
      SELECT '샌들'
      UNION
      SELECT '로퍼'
      UNION
      SELECT '구두') sub
         JOIN categories parent
              ON parent.name = '신발'
                  AND parent.parent_id = (SELECT id FROM categories WHERE name = '남성' AND parent_id IS NULL)
WHERE NOT EXISTS (SELECT 1
                  FROM categories
                  WHERE name = sub.name AND parent_id = parent.id);

-- [8] 남성 - 가방 하위 카테고리
INSERT INTO categories (name, parent_id, created_at, updated_at, is_deleted)
SELECT sub.name, parent.id, NOW(), NOW(), FALSE
FROM (SELECT '백팩' AS name
      UNION
      SELECT '크로스백'
      UNION
      SELECT '클러치백'
      UNION
      SELECT '토트백') sub
         JOIN categories parent
              ON parent.name = '가방'
                  AND parent.parent_id = (SELECT id FROM categories WHERE name = '남성' AND parent_id IS NULL)
WHERE NOT EXISTS (SELECT 1
                  FROM categories
                  WHERE name = sub.name AND parent_id = parent.id);

-- [9] 남성 - 액세서리 하위 카테고리
INSERT INTO categories (name, parent_id, created_at, updated_at, is_deleted)
SELECT sub.name, parent.id, NOW(), NOW(), FALSE
FROM (SELECT '모자' AS name
      UNION
      SELECT '벨트'
      UNION
      SELECT '시계'
      UNION
      SELECT '팔찌'
      UNION
      SELECT '선글라스'
      UNION
      SELECT '머플러/스카프') sub
         JOIN categories parent
              ON parent.name = '액세서리'
                  AND parent.parent_id = (SELECT id FROM categories WHERE name = '남성' AND parent_id IS NULL)
WHERE NOT EXISTS (SELECT 1
                  FROM categories
                  WHERE name = sub.name AND parent_id = parent.id);

-- [10] 남성 - 이너웨어 하위 카테고리
INSERT INTO categories (name, parent_id, created_at, updated_at, is_deleted)
SELECT sub.name, parent.id, NOW(), NOW(), FALSE
FROM (SELECT '러닝/탱크탑' AS name
      UNION
      SELECT '드로즈/트렁크') sub
         JOIN categories parent
              ON parent.name = '이너웨어'
                  AND parent.parent_id = (SELECT id FROM categories WHERE name = '남성' AND parent_id IS NULL)
WHERE NOT EXISTS (SELECT 1
                  FROM categories
                  WHERE name = sub.name AND parent_id = parent.id);

-- [11] 남성 - 라이프웨어 하위 카테고리
INSERT INTO categories (name, parent_id, created_at, updated_at, is_deleted)
SELECT sub.name, parent.id, NOW(), NOW(), FALSE
FROM (SELECT '홈웨어' AS name
      UNION
      SELECT '스포츠웨어') sub
         JOIN categories parent
              ON parent.name = '라이프웨어'
                  AND parent.parent_id = (SELECT id FROM categories WHERE name = '남성' AND parent_id IS NULL)
WHERE NOT EXISTS (SELECT 1
                  FROM categories
                  WHERE name = sub.name AND parent_id = parent.id);

-- [12] 여성 - 상위 카테고리
INSERT INTO categories (name, parent_id, created_at, updated_at, is_deleted)
SELECT sub.name, parent.id, NOW(), NOW(), FALSE
FROM (SELECT '아우터' AS name
      UNION
      SELECT '상의'
      UNION
      SELECT '하의'
      UNION
      SELECT '신발'
      UNION
      SELECT '가방'
      UNION
      SELECT '액세서리'
      UNION
      SELECT '원피스'
      UNION
      SELECT '이너웨어') sub
         JOIN categories parent
              ON parent.name = '여성'
                  AND parent.parent_id IS NULL
WHERE NOT EXISTS (SELECT 1
                  FROM categories
                  WHERE name = sub.name AND parent_id = parent.id);

-- [13] 여성 - 아우터 하위 카테고리
INSERT INTO categories (name, parent_id, created_at, updated_at, is_deleted)
SELECT sub.name, parent.id, NOW(), NOW(), FALSE
FROM (SELECT '코트' AS name
      UNION
      SELECT '재킷'
      UNION
      SELECT '가디건'
      UNION
      SELECT '블루종'
      UNION
      SELECT '패딩') sub
         JOIN categories parent
              ON parent.name = '아우터'
                  AND parent.parent_id = (SELECT id FROM categories WHERE name = '여성' AND parent_id IS NULL)
WHERE NOT EXISTS (SELECT 1
                  FROM categories
                  WHERE name = sub.name AND parent_id = parent.id);

-- [14] 여성 - 상의 하위 카테고리
INSERT INTO categories (name, parent_id, created_at, updated_at, is_deleted)
SELECT sub.name, parent.id, NOW(), NOW(), FALSE
FROM (SELECT '블라우스' AS name
      UNION
      SELECT '니트'
      UNION
      SELECT '티셔츠'
      UNION
      SELECT '후드티') sub
         JOIN categories parent
              ON parent.name = '상의'
                  AND parent.parent_id = (SELECT id FROM categories WHERE name = '여성' AND parent_id IS NULL)
WHERE NOT EXISTS (SELECT 1
                  FROM categories
                  WHERE name = sub.name AND parent_id = parent.id);

-- [15] 여성 - 하의 하위 카테고리
INSERT INTO categories (name, parent_id, created_at, updated_at, is_deleted)
SELECT sub.name, parent.id, NOW(), NOW(), FALSE
FROM (SELECT '스커트' AS name
      UNION
      SELECT '팬츠'
      UNION
      SELECT '반바지'
      UNION
      SELECT '슬랙스') sub
         JOIN categories parent
              ON parent.name = '하의'
                  AND parent.parent_id = (SELECT id FROM categories WHERE name = '여성' AND parent_id IS NULL)
WHERE NOT EXISTS (SELECT 1
                  FROM categories
                  WHERE name = sub.name AND parent_id = parent.id);

-- [16] 여성 - 신발 하위 카테고리
INSERT INTO categories (name, parent_id, created_at, updated_at, is_deleted)
SELECT sub.name, parent.id, NOW(), NOW(), FALSE
FROM (SELECT '플랫슈즈' AS name
      UNION
      SELECT '힐'
      UNION
      SELECT '부츠'
      UNION
      SELECT '스니커즈'
      UNION
      SELECT '로퍼') sub
         JOIN categories parent
              ON parent.name = '신발'
                  AND parent.parent_id = (SELECT id FROM categories WHERE name = '여성' AND parent_id IS NULL)
WHERE NOT EXISTS (SELECT 1
                  FROM categories
                  WHERE name = sub.name AND parent_id = parent.id);

-- [17] 여성 - 가방 하위 카테고리
INSERT INTO categories (name, parent_id, created_at, updated_at, is_deleted)
SELECT sub.name, parent.id, NOW(), NOW(), FALSE
FROM (SELECT '크로스백' AS name
      UNION
      SELECT '숄더백'
      UNION
      SELECT '토트백'
      UNION
      SELECT '미니백') sub
         JOIN categories parent
              ON parent.name = '가방'
                  AND parent.parent_id = (SELECT id FROM categories WHERE name = '여성' AND parent_id IS NULL)
WHERE NOT EXISTS (SELECT 1
                  FROM categories
                  WHERE name = sub.name AND parent_id = parent.id);

-- [18] 여성 - 액세서리 하위 카테고리
INSERT INTO categories (name, parent_id, created_at, updated_at, is_deleted)
SELECT sub.name, parent.id, NOW(), NOW(), FALSE
FROM (SELECT '목걸이' AS name
      UNION
      SELECT '귀걸이'
      UNION
      SELECT '팔찌'
      UNION
      SELECT '반지'
      UNION
      SELECT '헤어악세서리'
      UNION
      SELECT '스카프') sub
         JOIN categories parent
              ON parent.name = '액세서리'
                  AND parent.parent_id = (SELECT id FROM categories WHERE name = '여성' AND parent_id IS NULL)
WHERE NOT EXISTS (SELECT 1
                  FROM categories
                  WHERE name = sub.name AND parent_id = parent.id);

-- [19] 여성 - 원피스 하위 카테고리
INSERT INTO categories (name, parent_id, created_at, updated_at, is_deleted)
SELECT sub.name, parent.id, NOW(), NOW(), FALSE
FROM (SELECT '캐주얼원피스' AS name
      UNION
      SELECT '롱원피스'
      UNION
      SELECT '미니원피스') sub
         JOIN categories parent
              ON parent.name = '원피스'
                  AND parent.parent_id = (SELECT id FROM categories WHERE name = '여성' AND parent_id IS NULL)
WHERE NOT EXISTS (SELECT 1
                  FROM categories
                  WHERE name = sub.name AND parent_id = parent.id);

-- [20] 여성 - 이너웨어 하위 카테고리
INSERT INTO categories (name, parent_id, created_at, updated_at, is_deleted)
SELECT sub.name, parent.id, NOW(), NOW(), FALSE
FROM (SELECT '브라탑' AS name
      UNION
      SELECT '팬티'
      UNION
      SELECT '스타킹') sub
         JOIN categories parent
              ON parent.name = '이너웨어'
                  AND parent.parent_id = (SELECT id FROM categories WHERE name = '여성' AND parent_id IS NULL)
WHERE NOT EXISTS (SELECT 1
                  FROM categories
                  WHERE name = sub.name AND parent_id = parent.id);

-- [21] 아동 - 상위 카테고리
INSERT INTO categories (name, parent_id, created_at, updated_at, is_deleted)
SELECT sub.name, parent.id, NOW(), NOW(), FALSE
FROM (SELECT '아우터' AS name
      UNION
      SELECT '상의'
      UNION
      SELECT '하의'
      UNION
      SELECT '신발'
      UNION
      SELECT '액세서리') sub
         JOIN categories parent
              ON parent.name = '아동'
                  AND parent.parent_id IS NULL
WHERE NOT EXISTS (SELECT 1
                  FROM categories
                  WHERE name = sub.name AND parent_id = parent.id);

-- [22-1] 아동-상의 하위
INSERT INTO categories (name, parent_id, created_at, updated_at, is_deleted)
SELECT '티셔츠', parent.id, NOW(), NOW(), FALSE
FROM categories parent
WHERE parent.name = '상의'
  AND parent.parent_id = (SELECT id FROM categories WHERE name = '아동' AND parent_id IS NULL)
  AND NOT EXISTS (
    SELECT 1 FROM categories WHERE name = '티셔츠' AND parent_id = parent.id
);

-- [22-2] 아동-하의 하위
INSERT INTO categories (name, parent_id, created_at, updated_at, is_deleted)
SELECT '바지', parent.id, NOW(), NOW(), FALSE
FROM categories parent
WHERE parent.name = '하의'
  AND parent.parent_id = (SELECT id FROM categories WHERE name = '아동' AND parent_id IS NULL)
  AND NOT EXISTS (
    SELECT 1 FROM categories WHERE name = '바지' AND parent_id = parent.id
);

-- [22-3] 아동-신발 하위
INSERT INTO categories (name, parent_id, created_at, updated_at, is_deleted)
SELECT '스니커즈', parent.id, NOW(), NOW(), FALSE
FROM categories parent
WHERE parent.name = '신발'
  AND parent.parent_id = (SELECT id FROM categories WHERE name = '아동' AND parent_id IS NULL)
  AND NOT EXISTS (
    SELECT 1 FROM categories WHERE name = '스니커즈' AND parent_id = parent.id
);