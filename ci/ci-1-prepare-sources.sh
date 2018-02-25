#!/usr/bin/env bash

set -x

echo "dynamically fix the JavaScript references to bypass cache on new deployments"
sed -E -i -e "s/.js\?r=[0-9.]+(-SNAPSHOT|-latest)?/.js\?r=${PROJECT_VERSION}/g" ../src/index.html
if ! [ -s ../src/index.html ]
then
  echo "src/index.html is empty!"
  exit 1
else
  cat src/index.html
fi

echo "dynamically configuring the services.js"
sed -i -e "s/replacethiswithcurrentenvironment/${ACTIVE_ENV}/g" ../src/static/js/services.js

echo "active_env:"
cat ../src/static/js/services.js | grep "var active_env ="

echo "finished prepare_build.sh"
