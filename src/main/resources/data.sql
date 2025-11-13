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

-- 카테고리 초기 데이터 주입 (비파괴적 & 자동 참조형)

SET FOREIGN_KEY_CHECKS = 0;

-- 최상위 카테고리
INSERT INTO categories (name, parent_id, created_at, updated_at, is_deleted)
SELECT '남성', NULL, NOW(), NOW(), false
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = '남성' AND parent_id IS NULL);

INSERT INTO categories (name, parent_id, created_at, updated_at, is_deleted)
SELECT '여성', NULL, NOW(), NOW(), false
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = '여성' AND parent_id IS NULL);

INSERT INTO categories (name, parent_id, created_at, updated_at, is_deleted)
SELECT '아동', NULL, NOW(), NOW(), false
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = '아동' AND parent_id IS NULL);

-- 남성 하위 카테고리
INSERT INTO categories (name, parent_id, created_at, updated_at, is_deleted)
SELECT '아우터', (SELECT id FROM categories WHERE name = '남성' AND parent_id IS NULL), NOW(), NOW(), false
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = '아우터' AND parent_id = (SELECT id FROM categories WHERE name = '남성' AND parent_id IS NULL));

INSERT INTO categories (name, parent_id, created_at, updated_at, is_deleted)
SELECT '상의', (SELECT id FROM categories WHERE name = '남성' AND parent_id IS NULL), NOW(), NOW(), false
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = '상의' AND parent_id = (SELECT id FROM categories WHERE name = '남성' AND parent_id IS NULL));

INSERT INTO categories (name, parent_id, created_at, updated_at, is_deleted)
SELECT '하의', (SELECT id FROM categories WHERE name = '남성' AND parent_id IS NULL), NOW(), NOW(), false
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = '하의' AND parent_id = (SELECT id FROM categories WHERE name = '남성' AND parent_id IS NULL));

INSERT INTO categories (name, parent_id, created_at, updated_at, is_deleted)
SELECT '신발', (SELECT id FROM categories WHERE name = '남성' AND parent_id IS NULL), NOW(), NOW(), false
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = '신발' AND parent_id = (SELECT id FROM categories WHERE name = '남성' AND parent_id IS NULL));

INSERT INTO categories (name, parent_id, created_at, updated_at, is_deleted)
SELECT '가방', (SELECT id FROM categories WHERE name = '남성' AND parent_id IS NULL), NOW(), NOW(), false
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = '가방' AND parent_id = (SELECT id FROM categories WHERE name = '남성' AND parent_id IS NULL));

INSERT INTO categories (name, parent_id, created_at, updated_at, is_deleted)
SELECT '액세서리', (SELECT id FROM categories WHERE name = '남성' AND parent_id IS NULL), NOW(), NOW(), false
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = '액세서리' AND parent_id = (SELECT id FROM categories WHERE name = '남성' AND parent_id IS NULL));

INSERT INTO categories (name, parent_id, created_at, updated_at, is_deleted)
SELECT '이너웨어', (SELECT id FROM categories WHERE name = '남성' AND parent_id IS NULL), NOW(), NOW(), false
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = '이너웨어' AND parent_id = (SELECT id FROM categories WHERE name = '남성' AND parent_id IS NULL));

INSERT INTO categories (name, parent_id, created_at, updated_at, is_deleted)
SELECT '라이프웨어', (SELECT id FROM categories WHERE name = '남성' AND parent_id IS NULL), NOW(), NOW(), false
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = '라이프웨어' AND parent_id = (SELECT id FROM categories WHERE name = '남성' AND parent_id IS NULL));

-- 남성 - 아우터 세부
INSERT INTO categories (name, parent_id, created_at, updated_at, is_deleted)
SELECT sub.name, parent.id, NOW(), NOW(), false
FROM (SELECT '자켓' AS name UNION SELECT '코트' UNION SELECT '패딩' UNION SELECT '가디건' UNION SELECT '블루종' UNION SELECT '후리스' UNION SELECT '후드집업') sub
JOIN categories parent ON parent.name = '아우터' AND parent.parent_id = (SELECT id FROM categories WHERE name = '남성' AND parent_id IS NULL)
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = sub.name AND parent_id = parent.id);

-- 여성 하위 카테고리
INSERT INTO categories (name, parent_id, created_at, updated_at, is_deleted)
SELECT sub.name, parent.id, NOW(), NOW(), false
FROM (
    SELECT '아우터' AS name UNION SELECT '상의' UNION SELECT '하의' UNION SELECT '신발' UNION SELECT '가방'
    UNION SELECT '액세서리' UNION SELECT '원피스' UNION SELECT '이너웨어'
) sub
JOIN categories parent ON parent.name = '여성' AND parent.parent_id IS NULL
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = sub.name AND parent_id = parent.id);

-- 여성 - 아우터 세부
INSERT INTO categories (name, parent_id, created_at, updated_at, is_deleted)
SELECT sub.name, parent.id, NOW(), NOW(), false
FROM (
    SELECT '코트' AS name UNION SELECT '재킷' UNION SELECT '가디건' UNION SELECT '블루종' UNION SELECT '패딩'
) sub
JOIN categories parent ON parent.name = '아우터' AND parent.parent_id = (SELECT id FROM categories WHERE name = '여성' AND parent_id IS NULL)
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = sub.name AND parent_id = parent.id);

-- 아동 하위 카테고리 및 세부
INSERT INTO categories (name, parent_id, created_at, updated_at, is_deleted)
SELECT sub.name, parent.id, NOW(), NOW(), false
FROM (
    SELECT '아우터' AS name UNION SELECT '상의' UNION SELECT '하의' UNION SELECT '신발' UNION SELECT '액세서리'
) sub
JOIN categories parent ON parent.name = '아동' AND parent.parent_id IS NULL
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = sub.name AND parent_id = parent.id);

-- 아동 세부 예시 (상하신)
INSERT INTO categories (name, parent_id, created_at, updated_at, is_deleted)
SELECT sub.name, parent.id, NOW(), NOW(), false
FROM (SELECT '티셔츠' AS name UNION SELECT '바지' UNION SELECT '스니커즈') sub
JOIN categories parent ON parent.name IN ('상의', '하의', '신발') AND parent.parent_id = (SELECT id FROM categories WHERE name = '아동' AND parent_id IS NULL)
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = sub.name AND parent_id = parent.id);

SET FOREIGN_KEY_CHECKS = 1;
