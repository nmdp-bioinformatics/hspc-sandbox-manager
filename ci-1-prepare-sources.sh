#!/usr/bin/env bash

set -e

echo "starting prepare_build.sh..."

echo "dynamically fix the JavaScript references to bypass cache on new deployments"
cat src/index.html | sed -E -e "s/.js\?r=[0-9.]+/.js\?r=$PROJECT_VERSION/g" 2>&1 | tee src/index.html
if ! [ -s src/index.html ]
then
  echo "src/index.html is empty!"
  exit 1
end
fi

echo "dynamically fix the container-definitions_prod.json"
cat container-definitions_prod.json.jq | jq --arg container_name $PROJECT_NAME '.[0].name=$container_name' | jq --arg image_name $IMAGE_NAME '.[0].image=$image_name' | jq --arg container_port $PROJECT_PORT '.[0].portMappings[0].containerPort=($container_port | tonumber)' | tee container-definitions_prod.json

if ! [ -s container-definitions_prod.json ]
then
  echo "container-definitions_prod.json is empty!"
  exit 1
end
fi

echo "dynamically fix the container-definitions_test.json"
cat container-definitions_test.json.jq | jq --arg container_name $PROJECT_NAME '.[0].name=$container_name' | jq --arg image_name $IMAGE_NAME '.[0].image=$image_name' | jq --arg container_port $PROJECT_PORT '.[0].portMappings[0].containerPort=($container_port | tonumber)' | tee container-definitions_test.json

if ! [ -s container-definitions_test.json ]
then
  echo "container-definitions_test.json is empty!"
  exit 1
end
fi

echo "finished prepare_build.sh"
