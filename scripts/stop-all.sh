#!/bin/bash
echo "실행 중인 OOT 서버 종료 중..."

# local 서버 종료
LOCAL_PIDS=$(pgrep -f 'spring.profiles.active=local')
if [ -n "$LOCAL_PIDS" ]; then
  kill -9 $LOCAL_PIDS
  echo "Local 서버 종료 (PID: $LOCAL_PIDS)"
else
  echo "Local 서버가 실행 중이지 않습니다."
fi
# batch 서버 종료
BATCH_PIDS=$(pgrep -f 'spring.profiles.active=batch')
if [ -n "$BATCH_PIDS" ]; then
  kill -9 $BATCH_PIDS
  echo "Batch 서버 종료 (PID: $BATCH_PIDS)"
else
  echo "Batch 서버가 실행 중이지 않습니다."
fi