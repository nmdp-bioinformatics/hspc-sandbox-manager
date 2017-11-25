#!/usr/bin/env bash

echo "testing ci scripts..."

echo "pinning to the 'test' environment..."
sed -i -e 's/replacethiswithcurrentenvironment/test/g' ./src/static/js/services.js

echo "BITBUCKET_BRANCH: $BITBUCKET_BRANCH"
export BITBUCKET_BRANCH="develop"

echo "TARGET_AWS_CLUSTER: $TARGET_AWS_CLUSTER"
export TARGET_AWS_CLUSTER="hspc-test"

echo "TARGET_AWS_SERVICE: $TARGET_AWS_SERVICE"
export TARGET_AWS_SERVICE="hspc-sandbox-manager-service"

. ci-all.sh
