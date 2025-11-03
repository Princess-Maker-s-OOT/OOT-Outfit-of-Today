#!/bin/bash
echo "실행 중인 OOT 서버 종료 중..."

# local 서버 종료
LOCAL_PID=$(ps aux | grep 'spring.profiles.active=local' | grep -v grep | awk '{print $2}')
if [ -n "$LOCAL_PID" ]; then
  kill -9 $LOCAL_PID
  echo "Local 서버 종료 (PID: $LOCAL_PID)"
else
  echo "Local 서버가 실행 중이지 않습니다."
fi

# batch 서버 종료
BATCH_PID=$(ps aux | grep 'spring.profiles.active=batch' | grep -v grep | awk '{print $2}')
if [ -n "$BATCH_PID" ]; then
  kill -9 $BATCH_PID
  echo "Batch 서버 종료 (PID: $BATCH_PID)"
else
  echo "Batch 서버가 실행 중이지 않습니다."
fi