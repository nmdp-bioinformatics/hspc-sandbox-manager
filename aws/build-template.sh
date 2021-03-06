#!/usr/bin/env bash

echo "running $0..."

if [ $# -eq 6 ]; then
    echo "usage: $0 {project-name} {docker-image-coordinates} {project-port} {aws-container-memory-reserve}";
    exit 1;
fi

TEMPLATE_FILE="../aws/task-definition.json"

set -x

echo "dynamically fix ${TEMPLATE_FILE}"
jq ".family=\"${1}\"" ${TEMPLATE_FILE} > tmp.json && mv tmp.json ${TEMPLATE_FILE}
jq ".containerDefinitions[0].name=\"${1}\"" ${TEMPLATE_FILE} > tmp.json && mv tmp.json ${TEMPLATE_FILE}
jq ".containerDefinitions[0].image=\"${2}\"" ${TEMPLATE_FILE} > tmp.json && mv tmp.json ${TEMPLATE_FILE}
jq ".containerDefinitions[0].portMappings[0].containerPort=(${3} | tonumber)" ${TEMPLATE_FILE} > tmp.json && mv tmp.json ${TEMPLATE_FILE}
jq ".containerDefinitions[0].memoryReservation=(${4} | tonumber)" ${TEMPLATE_FILE} > tmp.json && mv tmp.json ${TEMPLATE_FILE}
jq ".containerDefinitions[0].logConfiguration.options.\"awslogs-group\"=\"/ecs/${1}\"" ${TEMPLATE_FILE} > tmp.json && mv tmp.json ${TEMPLATE_FILE}

cat ${TEMPLATE_FILE}

echo "finished $0"
