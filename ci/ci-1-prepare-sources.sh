#!/usr/bin/env bash

jq ".family = \"$PROJECT_FULL_NAME\"" ${TEMPLATE_FILE} > tmp.json && mv tmp.json ${TEMPLATE_FILE}
jq ".containerDefinitions[0].name = \"$PROJECT_FULL_NAME\"" ${TEMPLATE_FILE} > tmp.json && mv tmp.json ${TEMPLATE_FILE}
jq ".containerDefinitions[0].image = \"$DOCKER_IMAGE_COORDINATES\"" ${TEMPLATE_FILE} > tmp.json && mv tmp.json ${TEMPLATE_FILE}
jq ".containerDefinitions[0].portMappings[0].containerPort=(${PROJECT_PORT} | tonumber)" ${TEMPLATE_FILE} > tmp.json && mv tmp.json ${TEMPLATE_FILE}
jq ".containerDefinitions[0].logConfiguration.options.\"awslogs-group\" = \"/ecs/$PROJECT_FULL_NAME\"" ${TEMPLATE_FILE} > tmp.json && mv tmp.json ${TEMPLATE_FILE}
jq "(.containerDefinitions[0].environment[] | select(.name == \"JASYPT_ENCRYPTOR_PASSWORD\") | .value) |= \""${ENC_PW_TEST}"\"" ${TEMPLATE_FILE} > tmp.json && mv tmp.json ${TEMPLATE_FILE}
jq "(.containerDefinitions[0].environment[] | select(.name == \"SPRING_PROFILES_ACTIVE\") | .value) |= \"${SPRING_PROFILES_ACTIVE}\"" ${TEMPLATE_FILE} > tmp.json && mv tmp.json ${TEMPLATE_FILE}
jq "(.containerDefinitions[0].environment[] | select(.name == \"TARGET_ENV\") | .value) |= \"${TARGET_ENV}\"" ${TEMPLATE_FILE} > tmp.json && mv tmp.json ${TEMPLATE_FILE}
jq ".containerDefinitions[0].memoryReservation=(${AWS_CONTAINER_MEMORY_RESERVE} | tonumber)" ${TEMPLATE_FILE} > tmp.json && mv tmp.json ${TEMPLATE_FILE}

cat ${TEMPLATE_FILE}

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
sed -i -e "s/replacethiswithcurrentenvironment/${TARGET_ENV}/g" ../src/static/js/services.js

echo "active_env:"
cat ../src/static/js/services.js | grep "var active_env ="

echo "finished prepare_build.sh"
