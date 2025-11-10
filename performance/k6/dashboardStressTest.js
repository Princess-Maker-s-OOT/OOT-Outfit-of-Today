import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { uuidv4, randomString, randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

const BASE_URL = __ENV.BASE_URL || 'http://host.docker.internal:8080';
const USER_COUNT = parseInt(__ENV.USER_COUNT, 10) || 1000;

export const options = {
    setupTimeout: '20m', // (예시) setup이 최대 20분까지 대기 가능하게 세팅
    scenarios: {
        user_stress: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '2m', target: 200 },
                { duration: '2m', target: 500 },
                { duration: '2m', target: 800 },
                { duration: '2m', target: 1000 },
                { duration: '1m', target: 0 }
            ],
            exec: 'userScenario',
        },
        admin_stress: {
            executor: 'constant-vus',
            vus: 5,
            duration: '9m',
            exec: 'adminScenario',
            startTime: '10s',
        },
    },
    thresholds: {
        'http_req_failed{role:user}': ['rate<0.10'],
        'http_req_duration{role:user}': ['p(95)<5000'],
        'http_req_failed{role:admin}': ['rate<0.05'],
        'http_req_duration{role:admin}': ['p(95)<2000'],
    },
};

function parseAccessToken(res) {
    try {
        const data = res.json('data');
        return data && data.accessToken ? data.accessToken : null;
    } catch (e) {
        return null;
    }
}

export function setup() {
    const users = [];
    const reqCount = USER_COUNT;
    let batchTry = 0;
    const BATCH_SIZE = 20;
    const SLEEP_SEC = 0.5;
    while (users.length < reqCount && batchTry < 100) {
        batchTry++;
        const batchReqs = [];
        const batchMeta = [];
        for (let i = 0; i < Math.min(reqCount - users.length, BATCH_SIZE); i++) {
            const idx = users.length + i + 1;
            const loginId = `u${idx}${randomString(4, "abcdefghijklmnopqrstuvwxyz")}`;
            const nickname = `test${idx}${randomString(2, "abcdefghijklmnopqrstuvwxyz")}`;
            const email = `${loginId}@example.com`;
            const username = randomString(randomIntBetween(2, 10), "abcdefghijklmnopqrstuvwxyz0123456789");
            const password = 'Ab12!@zz';
            const phoneNumber = "010" + randomString(8, "0123456789");
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
        sleep(SLEEP_SEC);
        const loginReqs = [];
        const loginMetas = [];
        for (let i = 0; i < signupResults.length; i++) {
            if (signupResults[i].status !== 201) continue;
            const {loginId, password, nickname} = batchMeta[i];
            const loginPayload = JSON.stringify({ loginId, password, deviceId: uuidv4(), deviceName: `K6VU${loginId}` });
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
        sleep(SLEEP_SEC);
    }
    if (users.length < reqCount)
        throw new Error(`[setup] Could not create ${reqCount} users: only ${users.length} created`);
    return { users };
}

export function userScenario(data) {
    const n = data.users.length;
    const idx = ((__VU - 1) % n);
    const user = data.users[idx];
    if (!user) {
        console.error(`[VU:${__VU}] user undefined! idx=${idx}, users.length=${n}`);
        return;
    }
    const token = user.token;
    group('사용자 대시보드 - 통계 API', () => {
        [
            { url: `/api/v1/dashboards/users/overview`, api: 'summary' },
            { url: `/api/v1/dashboards/users/statistics`, api: 'wearStats' }
        ].forEach(({ url, api }) => {
            let res = http.get(`${BASE_URL}${url}`, {
                headers: { 'Authorization': `${token}` },
                tags: { role: 'user', api }
            });
            check(res, {'status 200': r => r.status === 200 });
        });
        sleep(0.5);
    });
}

function adminLogin() {
    const loginPayload = JSON.stringify({
        loginId: 'admin',
        password: 'admin00!',
        deviceId: uuidv4(),
        deviceName: "K6 Admin Runner"
    });
    const loginRes = http.post(`${BASE_URL}/api/v1/auth/login`, loginPayload, { headers: { 'Content-Type': 'application/json' } });
    if (loginRes.status !== 200) {
        return null;
    }
    const token = parseAccessToken(loginRes);
    if (!token) {
        return null;
    }
    return token;
}

export function adminScenario() {
    const token = adminLogin();
    if (!token) return;
    group('관리자 대시보드 - 통계 API', () => {
        [
            { url: '/api/admin/v1/dashboards/users/statistics', api: 'userStats' },
            { url: '/api/admin/v1/dashboards/clothes/statistics', api: 'clothesStats' },
            { url: '/api/admin/v1/dashboards/sale-posts/statistics', api: 'salesStats' },
            { url: '/api/admin/v1/dashboards/popular', api: 'topCatStats' },
        ].forEach(({ url, api }) => {
            let res = http.get(`${BASE_URL}${url}`, {
                headers: { 'Authorization': `${token}` },
                tags: { role: 'admin', api }
            });
            check(res, { 'status 200': r => r.status === 200 });
        });
        sleep(1); // 관리자도 부하를 조금 완화
    });
}
