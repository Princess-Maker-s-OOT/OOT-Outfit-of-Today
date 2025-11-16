#!/usr/bin/env bash
set -euo pipefail

# 필수 env: AWS_REGION, ACCOUNT_ID, ECR_REPO, IMAGE_TAG, FRONTEND_PATH
: "${AWS_REGION:?AWS_REGION required}"
: "${ACCOUNT_ID:?ACCOUNT_ID required}"
: "${ECR_REPO:?ECR_REPO required}"
: "${IMAGE_TAG:?IMAGE_TAG required}"
: "${FRONTEND_PATH:?FRONTEND_PATH required}"

# ECR 저장소 이름과 태그는 소문자여야 함
ECR_REPO=${ECR_REPO,,}
IMAGE_TAG=${IMAGE_TAG,,}

REG_URI="${ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
FULL_URI="${REG_URI}/${ECR_REPO}:${IMAGE_TAG}"

echo "[ECR] Login to ${REG_URI}"
aws ecr get-login-password --region "${AWS_REGION}" \
  | docker login --username AWS --password-stdin "${REG_URI}"

echo "[ECR] Copying Dockerfile and .dockerignore to frontend project"
cp "$(dirname "$0")/../Dockerfile" "${FRONTEND_PATH}/"
cp "$(dirname "$0")/../.dockerignore" "${FRONTEND_PATH}/"

echo "[ECR] Build ${ECR_REPO}:${IMAGE_TAG}"
docker build -t "${ECR_REPO}:${IMAGE_TAG}" "${FRONTEND_PATH}"

echo "[ECR] Tag -> ${FULL_URI}"
docker tag "${ECR_REPO}:${IMAGE_TAG}" "${FULL_URI}"

echo "[ECR] Push -> ${FULL_URI}"
docker push "${FULL_URI}"

# GitHub Actions 간 스텝 전달용
if [[ -n "${GITHUB_OUTPUT:-}" ]]; then
  echo "FULL_URI=${FULL_URI}" >> "${GITHUB_OUTPUT}"
else
  echo "FULL_URI=${FULL_URI}"
fi