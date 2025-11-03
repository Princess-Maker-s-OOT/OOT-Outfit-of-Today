#!/usr/bin/env bash
set -euo pipefail

echo "OOT-Outfit-of-Today 서버 실행 시작..."

# ===== logs 폴더 자동 생성 =====
mkdir -p logs
echo "logs 디렉토리 확인 완료."

# ===== 기존 포트 점유 프로세스 종료 =====
echo "기존 포트 점유 프로세스 종료 중..."
kill -9 $(lsof -ti :8080) 2>/dev/null || true
kill -9 $(lsof -ti :8081) 2>/dev/null || true

# ===== 기존 프로세스 종료 =====
echo "기존 서버 프로세스 종료 중..."
pkill -f "spring.profiles.active=local" || true
pkill -f "spring.profiles.active=batch" || true
sleep 2

# ===== Local 서버 실행 =====
echo "Local 서버 실행 중..."
nohup ./gradlew bootRun --args="--spring.profiles.active=local" > logs/local-server.log 2>&1 &

# ===== Batch 서버 실행 =====
echo "Batch 서버 실행 중..."
nohup ./gradlew bootRun --args="--spring.profiles.active=batch" > logs/batch-server.log 2>&1 &

sleep 8
echo "서버가 백그라운드에서 실행 중입니다."

# ===== 상태 확인 =====
LOCAL_PID=$(pgrep -f "spring.profiles.active=local" | head -n 1 || true)
BATCH_PID=$(pgrep -f "spring.profiles.active=batch" | head -n 1 || true)

if [ -n "$LOCAL_PID" ]; then
  echo "Local 서버 실행 중 (PID: $LOCAL_PID)"
else
  echo "Local 서버 실행 실패! 로그 확인 필요:"
  tail -n 20 logs/local-server.log || echo "(로그 없음)"
fi

if [ -n "$BATCH_PID" ]; then
  echo "Batch 서버 실행 중 (PID: $BATCH_PID)"
else
  echo "Batch 서버 실행 실패! 로그 확인 필요:"
  tail -n 20 logs/batch-server.log || echo "(로그 없음)"
fi

echo "로그 파일 경로:"
echo " - Local: logs/local-server.log"
echo " - Batch: logs/batch-server.log"