#!/usr/bin/env bash

echo "testing ci scripts..."

echo "capture build definitions..."

# push the build image
export DOCKER_PUSH=true; echo "DOCKER_PUSH: $DOCKER_PUSH"

export PROJECT_REPO="nexus.hspconsortium.org:18083/hspc"; echo "PROJECT_REPO: $PROJECT_REPO"

export PROJECT_NAME=$(cat package.json | jq --raw-output '.name'); echo "PROJECT_NAME: $PROJECT_NAME"

export PROJECT_VERSION=$(cat package.json | jq --raw-output '.version'); echo "PROJECT_VERSION: $PROJECT_VERSION"

export PROJECT_PORT=$(cat package.json | jq --raw-output '.config.port'); echo "PROJECT_PORT: $PROJECT_PORT"

export FAMILY_NAME=$PROJECT_NAME

export IMAGE_NAME=$PROJECT_REPO/$PROJECT_NAME:$PROJECT_VERSION
echo "IMAGE_NAME: $IMAGE_NAME"

export CURRENT_ENV=local
echo "CURRENT_ENV: $CURRENT_ENV"

. ci-1-prepare-sources.sh
. ci-2-docker-image.sh
#. ci-3-aws-update.sh
