import random
import time
import string
import json
from locust import HttpUser, task, between, events, TaskSet
from locust.runners import MasterRunner
from prometheus_client import Counter

# ==========================================================
# 1. Prometheus 메트릭 및 이벤트 핸들러
# ==========================================================

SUCCESSFUL_LOGINS = Counter(
    'locust_successful_logins_total',
    'Count of successful login attempts'
)

@events.test_start.add_listener
def on_test_start(environment, **kwargs):
    if environment.runner and isinstance(environment.runner, MasterRunner):
        print("Master process detected. Starting Locust test.")
    else:
        print("Worker or Standalone process detected. Starting Locust test.")

@events.spawning_complete.add_listener
def on_spawning_complete(**kwargs):
    print("==================================================")
    print("✅ All users have spawned. Test is fully operational.")
    print("==================================================")

@events.quitting.add_listener
def on_quitting(environment, **kwargs):
    print("Locust test is stopping.")


# ==========================================================
# 2. 유효 데이터 생성 함수
# ==========================================================

def generate_random_string(length):
    return ''.join(random.choice(string.ascii_letters + string.digits) for _ in range(length))

def generate_user_data():
    """DB Unique 제약 조건과 DTO Validation을 모두 충족하는 유일한 데이터를 생성"""
    timestamp = str(int(time.time() * 1000000))
    unique_suffix = str(random.randint(1000, 9000))

    login_id = f"user_{timestamp[-4:]}_{unique_suffix}"
    email = f"user_{timestamp}{unique_suffix}@testload.com"
    nickname = f"Tester{unique_suffix}"
    username = "LoadTesterName"
    password = "123456A!"
    random_8_digits = str(random.randint(10000000, 99999999))
    phone_number = f"010{random_8_digits}"

    return {
        "loginId": login_id,
        "email": email,
        "nickname": nickname,
        "username": username,
        "password": password,
        "phoneNumber": phone_number,

        "login_credentials": {
            "loginId": login_id,
            "password": password
        }
    }


# ==========================================================
# 3. 순차적 인증 시퀀스 (회원가입 → 로그인)
# ==========================================================

class AuthSequence(TaskSet):
    """회원가입 -> 로그인 순서를 보장"""

    @task
    def signup_task(self):
        signup_endpoint = "/api/v1/auth/signup"
        payload = self.user.signup_payload

        with self.client.post(signup_endpoint,
                              json=payload,
                              name="[1] POST /signup",
                              catch_response=True) as response:
            if response.status_code in [200, 201]:
                response.success()
                self.user.is_registered = True
                print(f"✅ Signup Success: {self.user.signup_payload['loginId']}")

            elif response.status_code == 409:
                response.failure("Signup Failed: User already exists (409 Conflict).")
                self.user.is_registered = True  # 이미 존재 → 로그인 시도는 허용

            else:
                response.failure(f"❌ Signup Failed: {response.status_code}")
                self.user.is_registered = False

        # 회원가입 후 바로 로그인 시도로 전환
        self.schedule_task(self.login_task)

    def login_task(self):
        """회원가입 후 로그인"""
        if not self.user.is_registered:
            print("⚠️ Skipping login: user not registered properly")
            return  # 더 이상 중단 신호 안 줌 (runner.quit 제거)

        login_endpoint = "/api/v1/auth/login"
        credentials = self.user.signup_payload['login_credentials']

        with self.client.post(login_endpoint,
                              json=credentials,
                              name="[2] POST /login",
                              catch_response=True) as response:
            if response.status_code == 200:
                try:
                    data = response.json()
                    token = data.get("accessToken")
                    if token:
                        self.user.auth_token = token
                        SUCCESSFUL_LOGINS.inc()
                        print(f"🔑 Login Success: {self.user.signup_payload['loginId']}")
                    else:
                        response.failure("Login success but no token in response.")
                except json.JSONDecodeError:
                    response.failure("Invalid JSON in login response.")
            else:
                response.failure(f"❌ Login Failed: {response.status_code}")

        # 한 유저 시퀀스 완료 → 다음 가상 유저로 넘어감
        self.interrupt(reschedule=False)


# ==========================================================
# 4. 사용자 클래스 (MyUser)
# ==========================================================

class MyUser(HttpUser):
    wait_time = between(1, 2)
    host = "http://host.docker.internal:8080"
    tasks = [AuthSequence]

    signup_payload = None
    is_registered = False
    auth_token = None

    def on_start(self):
        """가상 사용자 시작 시 데이터 생성"""
        self.signup_payload = generate_user_data()
        self.is_registered = False
        self.auth_token = None
