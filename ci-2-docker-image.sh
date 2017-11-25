#!/usr/bin/env bash

set -e

[[ -z "$DOCKER_ID" ]] && { echo "Error: DOCKER_ID is not provided"; exit 1; } || echo "DOCKER_ID: $DOCKER_ID"
[[ -z "$DOCKER_PASSWORD" ]] && { echo "Error: DOCKER_PASSWORD is not provided"; exit 1; } || echo "DOCKER_PASSWORD: $DOCKER_PASSWORD"
[[ -z "$IMAGE_NAME" ]] && { echo "Error: IMAGE_NAME is not provided"; exit 1; } || echo "IMAGE_NAME: $IMAGE_NAME"

echo "starting ci-2-docker-image.sh..."

echo "docker login..."
docker login -u $DOCKER_ID -p $DOCKER_PASSWORD

echo "docker build..."
echo "IMAGE_NAME: $IMAGE_NAME"
docker build -t "$IMAGE_NAME" .

echo "docker push..."
docker push "$IMAGE_NAME"

echo "finished ci-2-docker-image.sh"