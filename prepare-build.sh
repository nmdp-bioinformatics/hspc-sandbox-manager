#!/usr/bin/env bash

set -e

echo 'starting prepare_build.sh...'

echo 'capture build definitions...'

export PROJECT_REPO="nexus.hspconsortium.org:18083/hspc"
echo "PROJECT_REPO: $PROJECT_REPO"

export PROJECT_NAME=$(cat package.json | jq --raw-output '.name')
echo "PROJECT_NAME: $PROJECT_NAME"

export PROJECT_VERSION=$(cat package.json | jq --raw-output '.version')
echo "PROJECT_VERSION: $PROJECT_VERSION"

export PROJECT_PORT=$(cat package.json | jq --raw-output '.config.port')
echo "PROJECT_PORT: $PROJECT_PORT"

export IMAGE_NAME=$PROJECT_REPO/$PROJECT_NAME:$PROJECT_VERSION
echo "IMAGE_NAME: $IMAGE_NAME"

echo 'dynamically fix the JavaScript references to bypass cache on new deployments'
cat src/index.html | sed -E -e "s/.js\?r=[0-9.]+/.js\?r=$PROJECT_VERSION/g" 2>&1 | tee src/index.html

echo 'dynamically fix the container-definitions_prod.json'
cat container-definitions_prod.json | jq --arg container_name $PROJECT_NAME '.[0].name=$container_name' 2>&1 | tee container-definitions_prod.json
cat container-definitions_prod.json | jq --arg image_name $IMAGE_NAME '.[0].image=$image_name' 2>&1 | tee container-definitions_prod.json
cat container-definitions_prod.json | jq --arg container_port $PROJECT_PORT '.[0].portMappings[0].containerPort=($container_port | tonumber)' 2>&1 | tee container-definitions_prod.json

if ! [ -s container-definitions_prod.json ]
then
  echo "container-definitions_prod.json is empty!"
  exit 1
end
fi

echo 'dynamically fix the container-definitions_test.json'
cat container-definitions_test.json | jq --arg container_name $PROJECT_NAME '.[0].name=$container_name' 2>&1 | tee container-definitions_test.json
cat container-definitions_test.json | jq --arg image_name $IMAGE_NAME '.[0].image=$image_name' 2>&1 | tee container-definitions_test.json
cat container-definitions_test.json | jq --arg container_port $PROJECT_PORT '.[0].portMappings[0].containerPort=($container_port | tonumber)' 2>&1 | tee container-definitions_test.json

if ! [ -s container-definitions_test.json ]
then
  echo "container-definitions_test.json is empty!"
  exit 1
end
fi

echo 'finished prepare_build.sh'
