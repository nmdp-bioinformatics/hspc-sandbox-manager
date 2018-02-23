#!/usr/bin/env bash

set -e

[[ -z "$1" ]] && { echo "usage: $0 {project_version} {test|prod}"; exit 1; }
[[ -z "$2" ]] && { echo "usage: $0 {project_version} {test|prod}"; exit 1; }

echo "starting prepare_build.sh..."
echo "PROJECT_VERSION: $1"
echo "CURRENT_ENV: $2"

echo "dynamically fix the JavaScript references to bypass cache on new deployments"
sed -E -i -e "s/.js\?r=[0-9.]+(-SNAPSHOT|-latest)?/.js\?r=$1/g" src/index.html
if ! [ -s src/index.html ]
then
  echo "src/index.html is empty!"
  exit 1
else
  cat src/index.html
fi

#echo "dynamically configuring the services.js"
#sed -i -e "s/replacethiswithcurrentenvironment/$2/g" src/static/js/services.js
#
#echo "active_env:"
#cat ./src/static/js/services.js | grep "var active_env ="

echo "finished prepare_build.sh"
