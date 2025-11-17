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
REPO_AND_TAG="$(echo "${FULL_URI}" | cut -d/ -f2- )"
REPO="$(echo "${REPO_AND_TAG}" | rev | cut -d: -f2- | rev)"
TAG="$(echo "${REPO_AND_TAG}"  | awk -F: '{print $NF}')"

# SSM 코멘트(100자 제한 방어)
COMMENT="Deploy ${REPO}:${TAG}"
if [ ${#COMMENT} -gt 100 ]; then
  COMMENT="${COMMENT:0:100}"
fi

echo "[INFO] FULL_URI=${FULL_URI}"
echo "[INFO] REG_URI=${REG_URI}"
echo "[INFO] EC2_INSTANCE_ID=${EC2_INSTANCE_ID}"
echo "[INFO] COMMENT=${COMMENT}"

# ======== PROMTAIL CONFIG (heredoc 제거 버전) ========
PROMTAIL_CONFIG_ESCAPED=$(printf "%s" "
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
      job: \"oot-dev\"
      environment: \"dev\"
      __path__: /app-logs/*.log
")

# ===== SSM에서 실행할 명령어들 =====
CMDS=(
  "aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${REG_URI}"
  "docker pull ${FULL_URI}"
  "docker network create oot-network || true"
  "echo '[INFO] Fetching Redis configuration from Parameter Store...'"
  "export REDIS_HOST=\$(aws ssm get-parameter --name '/config/dev/redis.host' --query 'Parameter.Value' --output text --region ${AWS_REGION})"
  "export REDIS_PORT=\$(aws ssm get-parameter --name '/config/dev/redis.port' --query 'Parameter.Value' --output text --region ${AWS_REGION})"
  "export REDIS_PASSWORD=\$(aws ssm get-parameter --name '/config/dev/redis.password' --with-decryption --query 'Parameter.Value' --output text --region ${AWS_REGION})"
  "echo \"[INFO] Redis configuration: host=\${REDIS_HOST}, port=\${REDIS_PORT}\""
  "docker stop oot-redis || true"
  "docker rm oot-redis || true"
  "docker run -d --name oot-redis --network oot-network --restart=always -p \${REDIS_PORT}:6379 redis:7-alpine redis-server --requirepass \${REDIS_PASSWORD}"
  "docker stop ${CONTAINER_NAME} || true"
  "docker rm   ${CONTAINER_NAME} || true"
  "mkdir -p /home/ssm-user/app-logs"

  # ===== heredoc 제거 → echo 사용 =====
  "echo \"${PROMTAIL_CONFIG_ESCAPED}\" > /home/ssm-user/promtail-config.yml"

  "docker run -d --name ${CONTAINER_NAME} --network oot-network --restart=always -p ${APP_PORT}:${APP_PORT} \
      -v /home/ssm-user/app-logs:/app-logs \
      -e SPRING_PROFILES_ACTIVE=${SPRING_PROFILE} \
      -e REDIS_HOST=\${REDIS_HOST} \
      -e REDIS_PORT=\${REDIS_PORT} \
      -e REDIS_PASSWORD=\${REDIS_PASSWORD} \
      ${FULL_URI}"

  "docker stop promtail || true"
  "docker rm promtail || true"
  "docker run -d --name promtail --restart=always \
      -v /home/ssm-user/promtail-config.yml:/etc/promtail/config.yml \
      -v /home/ssm-user/app-logs:/app-logs \
      grafana/promtail:latest -config.file=/etc/promtail/config.yml"
)

# Bash 배열 → JSON 배열 변환
COMMANDS_JSON=$(jq -Rn \
  --argjson arr "$(printf '%s\n' "${CMDS[@]}" | jq -R . | jq -s .)" \
  '$arr')

echo "[DEBUG] COMMANDS_JSON=${COMMANDS_JSON}"

# ===== SSM 명령 전송 =====
RESP=$(aws ssm send-command \
  --document-name "AWS-RunShellScript" \
  --comment "${COMMENT}" \
  --targets "Key=instanceIds,Values=${EC2_INSTANCE_ID}" \
  --parameters "{\"commands\": ${COMMANDS_JSON}}" \
  --region "${AWS_REGION}" \
  --output json)

CMD_ID=$(echo "${RESP}" | jq -r '.Command.CommandId')
echo "[INFO] SSM CommandId: ${CMD_ID}"

# ===== SSM 완료 대기 =====
for i in {1..30}; do
  STATUS=$(aws ssm get-command-invocation \
    --command-id "${CMD_ID}" \
    --instance-id "${EC2_INSTANCE_ID}" \
    --query 'Status' \
    --output text \
    --region "${AWS_REGION}") || true

  echo "[INFO] SSM Status: ${STATUS}"

  case "${STATUS}" in
    Success) exit 0 ;;
    Failed|Cancelled|TimedOut)
      echo "[ERROR] SSM failed: ${STATUS}"
      exit 1
      ;;
  esac

  sleep 5
done

echo "[ERROR] SSM command did not complete in time"
exit 1