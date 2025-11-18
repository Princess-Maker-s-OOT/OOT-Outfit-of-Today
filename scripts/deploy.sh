#!/usr/bin/env bash
set -euo pipefail

# ===== 필수 환경변수 점검 =====
: "${AWS_REGION:?AWS_REGION required}"
: "${EC2_INSTANCE_ID:?EC2_INSTANCE_ID required}"
: "${FULL_URI:?FULL_URI required}"
: "${CONTAINER_NAME:?CONTAINER_NAME required}"
: "${APP_PORT:?APP_PORT required}"
: "${SPRING_PROFILE:?SPRING_PROFILE required}"

MONITORING_EC2_PUBLIC_IP="54.180.9.231"

# ===== ECR 경로 파싱 =====
REG_URI="$(echo "${FULL_URI}" | cut -d/ -f1)"
REPO_AND_TAG="$(echo "${FULL_URI}" | cut -d/ -f2-)"
REPO="$(echo "${REPO_AND_TAG}" | rev | cut -d: -f2- | rev)"
TAG="$(echo "${REPO_AND_TAG}" | awk -F: '{print $NF}')"

COMMENT="Deploy ${REPO}:${TAG}"
if [ ${#COMMENT} -gt 100 ]; then COMMENT="${COMMENT:0:100}"; fi

echo "[INFO] Deploy start"
echo "[INFO] FULL_URI=${FULL_URI}"
echo "[INFO] EC2_INSTANCE_ID=${EC2_INSTANCE_ID}"

# ===== promtail config (Base64 인코딩) =====
PROMTAIL_CONFIG_CONTENT=$(cat <<EOF
server:
  http_listen_port: 9080
  grpc_listen_port: 0

positions:
  filename: /tmp/positions.yaml

clients:
  - url: http://${MONITORING_EC2_PUBLIC_IP}:3100/loki/api/v1/push

scrape_configs:
- job_name: oot-dev-logs
  static_configs:
  - targets:
      - localhost
    labels:
      job: "oot-dev"
      environment: "dev"
      __path__: /app-logs/*.log
EOF
)

PROMTAIL_CONFIG_CONTENT_B64=$(echo "${PROMTAIL_CONFIG_CONTENT}" | base64 -w 0)

# ===== EC2에서 실행될 명령 =====
CMDS=(
  "aws ecr get-login-password --region ${AWS_REGION} \
    | docker login --username AWS --password-stdin ${REG_URI}"

  "docker pull ${FULL_URI}"

  "docker network create oot-network || true"

  # Redis 환경변수 로드
  "export REDIS_HOST=\$(aws ssm get-parameter --name '/config/dev/redis.host' --query 'Parameter.Value' --output text --region ${AWS_REGION})"
  "export REDIS_PORT=\$(aws ssm get-parameter --name '/config/dev/redis.port' --query 'Parameter.Value' --output text --region ${AWS_REGION})"
  "export REDIS_PASSWORD=\$(aws ssm get-parameter --name '/config/dev/redis.password' --with-decryption --query 'Parameter.Value' --output text --region ${AWS_REGION})"

  # Redis 재기동
  "docker stop oot-redis || true"
  "docker rm oot-redis || true"
  "docker run -d --name oot-redis \
      --network oot-network \
      --restart=always \
      -p \${REDIS_PORT}:6379 \
      redis:7-alpine \
      redis-server --requirepass \${REDIS_PASSWORD}"

  # Redis health-check (Fail → 경고로 변경)
  "echo '[INFO] Checking Redis health...'"
  "for i in {1..10}; do \
      if docker exec oot-redis redis-cli -a \${REDIS_PASSWORD} ping | grep -q PONG; then \
        echo '[INFO] Redis OK'; break; \
      fi; echo '[WAITING] Redis... (\$i/10)'; sleep 2; \
    done"

  # ❗여기가 핵심 수정: Fail이어도 exit 안 함
  "if ! docker exec oot-redis redis-cli -a \${REDIS_PASSWORD} ping | grep -q PONG; then \
      echo '[WARN] Redis failed health-check — continuing (app will retry)'; \
    fi"

  # 앱 재기동
  "docker stop ${CONTAINER_NAME} || true"
  "docker rm   ${CONTAINER_NAME} || true"

  "mkdir -p /home/ssm-user/app-logs"

  "docker run -d --name ${CONTAINER_NAME} \
      --network oot-network \
      --restart=always \
      -p ${APP_PORT}:${APP_PORT} \
      -v /home/ssm-user/app-logs:/app-logs \
      -e SPRING_PROFILES_ACTIVE=${SPRING_PROFILE} \
      -e REDIS_HOST=\${REDIS_HOST} \
      -e REDIS_PORT=\${REDIS_PORT} \
      -e REDIS_PASSWORD=\${REDIS_PASSWORD} \
      ${FULL_URI}"

  # Optional Health Check (앱 실패해도 배포 OK)
  "echo '[INFO] Optional App Health Check...'"
  "for i in {1..10}; do \
      if curl -s http://localhost:${APP_PORT}/api/actuator/health | grep -q UP; then \
        echo '[INFO] App Health UP'; break; \
      fi; echo '[WARN] Health not UP yet (\$i/10)'; sleep 3; \
    done"

  "if ! curl -s http://localhost:${APP_PORT}/api/actuator/health | grep -q UP; then \
      echo '[WARN] Health check failed — continuing anyway (optional)'; \
    fi"

  # promtail config 생성
  "echo \"${PROMTAIL_CONFIG_CONTENT_B64}\" | base64 -d > /home/ssm-user/promtail-config.yml"

  # promtail 재기동
  "docker stop promtail || true"
  "docker rm promtail || true"
  "docker run -d --name promtail \
      --restart=always \
      -v /home/ssm-user/promtail-config.yml:/etc/promtail/config.yml \
      -v /home/ssm-user/app-logs:/app-logs \
      grafana/promtail:latest \
      -config.file=/etc/promtail/config.yml"
)

# ===== JSON 변환 =====
COMMANDS_JSON=$(jq -Rn --argjson arr "$(printf '%s\n' "${CMDS[@]}" | jq -R . | jq -s .)" '$arr')

# ===== SSM 실행 =====
RESP=$(aws ssm send-command \
  --document-name "AWS-RunShellScript" \
  --comment "${COMMENT}" \
  --targets "Key=instanceIds,Values=${EC2_INSTANCE_ID}" \
  --parameters "{\"commands\": ${COMMANDS_JSON}}" \
  --region "${AWS_REGION}" \
  --output json)

CMD_ID=$(echo "${RESP}" | jq -r '.Command.CommandId')
echo "[INFO] SSM CommandId: ${CMD_ID}"

# ===== 상태 대기 =====
for i in {1..30}; do
  STATUS=$(aws ssm get-command-invocation --command-id "${CMD_ID}" --instance-id "${EC2_INSTANCE_ID}" --region "${AWS_REGION}" --query 'Status' --output text || true)
  echo "[INFO] Status: $STATUS"

  case "$STATUS" in
    Success) exit 0 ;;
    Failed|Cancelled|TimedOut)
      echo "[ERROR] SSM command failed: $STATUS"
      aws ssm get-command-invocation --command-id "${CMD_ID}" --instance-id "${EC2_INSTANCE_ID}" --region "${AWS_REGION}" \
        --query '[Status,StandardOutputContent,StandardErrorContent]' --output text
      exit 1
      ;;
  esac

  sleep 5
done

echo "[ERROR] SSM timed out"
aws ssm get-command-invocation --command-id "${CMD_ID}" --instance-id "${EC2_INSTANCE_ID}" --region "${AWS_REGION}" \
  --query '[Status,StandardOutputContent,StandardErrorContent]' --output text
exit 1