#!/bin/bash
echo "현재 실행 중인 서버 상태 확인 중..."

LOCAL_PID=$(pgrep -f 'spring.profiles.active=local')
BATCH_PID=$(pgrep -f 'spring.profiles.active=batch')

if [ -n "$LOCAL_PID" ]; then
  echo "Local 서버 실행 중 (PID: $LOCAL_PID)"
else
  echo "Local 서버가 꺼져 있습니다."
fi

if [ -n "$BATCH_PID" ]; then
  echo "Batch 서버 실행 중 (PID: $BATCH_PID)"
else
  echo "Batch 서버가 꺼져 있습니다."
fi