#!/usr/bin/env bash

set -e

[[ -z "$IMAGE_NAME" ]] && { echo "Error: IMAGE_NAME is not provided"; exit 1; } || echo "IMAGE_NAME: $IMAGE_NAME"
[[ -z "$DOCKER_PUSH" ]] && { echo "Warning: DOCKER_PUSH is not provided, defaulting to true..."; DOCKER_PUSH=true; } || echo "DOCKER_PUSH: $DOCKER_PUSH"

echo "starting ci-2-docker-image.sh..."

echo "docker build..."
echo "IMAGE_NAME: $IMAGE_NAME"
docker build -t "$IMAGE_NAME" .

if [ $DOCKER_PUSH = "true" ]
then
    [[ -z "$NEXUS_USR" ]] && { echo "Error: NEXUS_USR is not provided"; exit 1; }
    [[ -z "$NEXUS_PWD" ]] && { echo "Error: NEXUS_PWD is not provided"; exit 1; }
    echo "docker login..."
    docker login -u $NEXUS_USR -p $NEXUS_PWD nexus.hspconsortium.org:18083

    echo "docker push..."
    docker push $IMAGE_NAME
else
    echo "docker push skipped"
fi

echo "finished ci-2-docker-image.sh"