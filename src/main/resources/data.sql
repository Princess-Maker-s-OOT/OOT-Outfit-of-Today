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
       -- 비밀번호 'admin00!'을 BCrypt로 해싱한 값 (아래 주석 참고)
       '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cyhQQNte8vUXq2gE6WFQfPPK5xk1G',
       '01000000000',
       'ROLE_ADMIN',
       '서울특별시 중구 세종대로 110',
       ST_GeomFromText('POINT(37.56681294 126.97865509)', 4326),
       'LOGIN_ID',
       NOW(),
       NOW(),
       0 -- is_deleted 기본값
FROM DUAL
WHERE NOT EXISTS (SELECT 1
                  FROM users
                  WHERE login_id = 'admin');