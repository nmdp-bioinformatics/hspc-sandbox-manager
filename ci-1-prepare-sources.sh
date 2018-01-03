#!/usr/bin/env bash

set -e

echo "starting prepare_build.sh..."
echo "PROJECT_VERSION: $PROJECT_VERSION"
echo "PROJECT_NAME: $PROJECT_NAME"
echo "CURRENT_ENV: $CURRENT_ENV"

echo "dynamically fix the JavaScript references to bypass cache on new deployments"
sed -E -i -e "s/.js\?r=[0-9.]+(-SNAPSHOT|-latest)?/.js\?r=$PROJECT_VERSION/g" src/index.html
if ! [ -s src/index.html ]
then
  echo "src/index.html is empty!"
  exit 1
else
  cat src/index.html
fi

echo "dynamically fix the container-definitions_prod.json"
cat container-definitions_prod.json.jq | jq --arg container_name $PROJECT_NAME '.[0].name=$container_name' | jq --arg image_name $IMAGE_NAME '.[0].image=$image_name' | jq --arg container_port $PROJECT_PORT '.[0].portMappings[0].containerPort=($container_port | tonumber)' | tee container-definitions_prod.json

if ! [ -s container-definitions_prod.json ]
then
  echo "container-definitions_prod.json is empty!"
  exit 1
fi

echo "dynamically fix the container-definitions_test.json"
cat container-definitions_test.json.jq | jq --arg container_name $PROJECT_NAME '.[0].name=$container_name' | jq --arg image_name $IMAGE_NAME '.[0].image=$image_name' | jq --arg container_port $PROJECT_PORT '.[0].portMappings[0].containerPort=($container_port | tonumber)' | tee container-definitions_test.json

if ! [ -s container-definitions_test.json ]
then
  echo "container-definitions_test.json is empty!"
  exit 1
fi

echo "dynamically configuring the services.js"
sed -i -e "s/replacethiswithcurrentenvironment/$CURRENT_ENV/g" src/static/js/services.js

echo "active_env:"
cat ./src/static/js/services.js | grep "var active_env ="

echo "finished prepare_build.sh"
