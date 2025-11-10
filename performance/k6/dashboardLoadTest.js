import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { uuidv4, randomString, randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

const BASE_URL = __ENV.BASE_URL || 'http://host.docker.internal:8080';

export const options = {
    setupTimeout: '10m',
    scenarios: {
        admin_load: {
            executor: 'constant-vus',
            vus: 1,
            duration: '2m',
            exec: 'adminScenario',
            startTime: '0s',  // 관리자가 0초에 즉시 시작
        },
        user_load: {
            executor: 'constant-vus',
            vus: 1000,
            duration: '2m',
            exec: 'userScenario',
            startTime: '10s',  // 사용자는 10초 후 시작
        },
    },
    thresholds: {
        'http_req_failed{role:admin}': ['rate<0.02'],
        'http_req_failed{role:user}': ['rate<0.05'],
        'http_req_duration{role:admin}': ['p(95)<800'],
        'http_req_duration{role:user}': ['p(95)<1300'],
    },
};

function parseAccessToken(res) {
    try {
        const data = res.json('data');
        return data && data.accessToken ? data.accessToken : null;
    } catch {
        return null;
    }
}

// 관리자 로그인 (재시도 포함)
function adminLoginWithRetry(maxRetries = 5) {
    let lastError = null;

    for (let attempt = 0; attempt < maxRetries; attempt++) {
        try {
            const loginPayload = JSON.stringify({
                loginId: 'admin',
                password: 'admin00!',
                deviceId: uuidv4(),
                deviceName: `K6 Admin Runner`,
            });

            const loginRes = http.post(`${BASE_URL}/api/v1/auth/login`, loginPayload, {
                headers: { 'Content-Type': 'application/json' },
                timeout: '10s'
            });

            if (loginRes.status === 200) {
                const token = parseAccessToken(loginRes);
                if (token) {
                    return token;
                }
            }

            lastError = `Login failed with status ${loginRes.status}`;
            sleep(1);
        } catch (e) {
            lastError = String(e);
            sleep(1);
        }
    }

    console.error(`Admin login failed after ${maxRetries} attempts: ${lastError}`);
    return null;
}

// 관리자 API 호출 (재시도 및 토큰 갱신)
function adminApiCallWithRetry(url, adminToken, maxRetries = 5) {
    let token = adminToken;
    let lastError = null;

    for (let attempt = 0; attempt < maxRetries; attempt++) {
        // 토큰이 없으면 재로그인
        if (!token) {
            token = adminLoginWithRetry(2);
            if (!token) {
                lastError = 'No token available';
                sleep(1);
                continue;
            }
        }

        try {
            const res = http.get(`${BASE_URL}${url}`, {
                headers: { 'Authorization': token },
                tags: { role: 'admin' },
                timeout: '15s'
            });

            // 성공
            if (res.status === 200) {
                return res;
            }

            // 401 토큰 만료: 재로그인
            if (res.status === 401) {
                token = adminLoginWithRetry(2);
                if (!token) {
                    return res;
                }
                sleep(0.5);
                continue;
            }

            // 기타 실패: 재시도
            lastError = `Status ${res.status}`;
            sleep(0.5);

        } catch (e) {
            lastError = String(e);
            sleep(0.5);
        }
    }

    // 최종 실패
    return { status: 0, body: lastError };
}

export function setup() {
    const users = [];
    const reqCount = 1000;
    const batchSize = 50;
    let batchTry = 0;

    console.log('[setup] 1000명의 사용자 생성 시작...');

    while (users.length < reqCount && batchTry < 30) {
        batchTry++;
        const batchReqs = [];
        const batchMeta = [];
        for (let i = 0; i < Math.min(reqCount - users.length, batchSize); i++) {
            const idx = users.length + i + 1;
            const loginId = `u${idx}${randomString(4, 'abcdefghijklmnopqrstuvwxyz')}`;
            const nickname = `test${idx}${randomString(2, 'abcdefghijklmnopqrstuvwxyz')}`;
            const email = `${loginId}@example.com`;
            const username = randomString(randomIntBetween(2, 10), 'abcdefghijklmnopqrstuvwxyz0123456789');
            const password = 'Ab12!@zz';
            const phoneNumber = '010' + randomString(8, '0123456789');
            const signupPayload = JSON.stringify({ loginId, email, nickname, username, password, phoneNumber });
            batchReqs.push([
                'POST',
                `${BASE_URL}/api/v1/auth/signup`,
                signupPayload,
                { headers: { 'Content-Type': 'application/json' } }
            ]);
            batchMeta.push({ loginId, nickname, password });
        }
        const signupResults = http.batch(batchReqs);

        const loginReqs = [];
        const loginMetas = [];
        for (let i = 0; i < signupResults.length; i++) {
            if (signupResults[i].status !== 201) continue;
            const { loginId, password, nickname } = batchMeta[i];
            const loginPayload = JSON.stringify({
                loginId,
                password,
                deviceId: uuidv4(),
                deviceName: `K6VU${loginId}`
            });
            loginReqs.push([
                'POST',
                `${BASE_URL}/api/v1/auth/login`,
                loginPayload,
                { headers: { 'Content-Type': 'application/json' } }
            ]);
            loginMetas.push({ loginId, nickname });
        }
        const loginResults = http.batch(loginReqs);
        for (let i = 0; i < loginResults.length; i++) {
            if (loginResults[i].status !== 200) continue;
            const token = parseAccessToken(loginResults[i]);
            if (!token) continue;
            const { loginId, nickname } = loginMetas[i];
            users.push({ loginId, nickname, token });
        }
    }

    if (users.length < reqCount) {
        throw new Error(`[setup] Could not create ${reqCount} users: only ${users.length} created`);
    }

    console.log(`[setup] 사용자 생성 완료: ${users.length}명`);

    // 관리자 토큰 초기 발급
    console.log('[setup] 관리자 로그인 시작...');
    const adminToken = adminLoginWithRetry(5);
    if (!adminToken) {
        throw new Error('[setup] Admin login failed');
    }

    console.log('[setup] 관리자 로그인 성공');
    return { users, adminToken };
}

export function adminScenario(data) {
    const adminToken = data.adminToken;

    group('관리자 대시보드 - 통계 API', () => {
        [
            { url: '/api/admin/v1/dashboards/users/statistics', api: 'userStats' },
            { url: '/api/admin/v1/dashboards/clothes/statistics', api: 'clothesStats' },
            { url: '/api/admin/v1/dashboards/sale-posts/statistics', api: 'salesStats' },
            { url: '/api/admin/v1/dashboards/popular', api: 'topCatStats' },
        ].forEach(({ url, api }) => {
            const res = adminApiCallWithRetry(url, adminToken, 5);
            if (res.status !== 200) {
                console.log(`[admin] ${api} 실패: ${res.status}`);
            }
            check(res, { 'status is 200': r => r.status === 200 });
        });
        sleep(1);
    });
}

export function userScenario(data) {
    const n = data.users.length;
    const idx = ((__VU - 1) % n);
    const user = data.users[idx];
    if (!user) return;

    const token = user.token;
    group('사용자 대시보드 - 통계 API', () => {
        [
            { url: `/api/v1/dashboards/users/overview`, api: 'summary' },
            { url: `/api/v1/dashboards/users/statistics`, api: 'wearStats' }
        ].forEach(({ url, api }) => {
            const res = http.get(`${BASE_URL}${url}`, {
                headers: { 'Authorization': token },
                tags: { role: 'user', api },
                timeout: '10s'
            });
            if (res.status !== 200) {
                console.log(`[user] ${api} 실패: ${res.status}`);
            }
            check(res, { 'status is 200': r => r.status === 200 });
        });
        sleep(1);
    });
}
