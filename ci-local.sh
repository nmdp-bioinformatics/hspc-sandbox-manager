#!/usr/bin/env bash

echo "testing ci scripts..."

# don't push the build image
export DOCKER_PUSH=false
echo "DOCKER_PUSH: $DOCKER_PUSH"

# fix on "local" to prevent a deployment to AWS
echo "BITBUCKET_BRANCH: $BITBUCKET_BRANCH"
export BITBUCKET_BRANCH="local"

# fix on "develop" to prevent a deployment to AWS
#export BITBUCKET_BRANCH="develop"
#echo "TARGET_AWS_CLUSTER: $TARGET_AWS_CLUSTER"
#export TARGET_AWS_CLUSTER="hspc-test"
#echo "TARGET_AWS_SERVICE: $TARGET_AWS_SERVICE"
#export TARGET_AWS_SERVICE="hspc-sandbox-manager-service"

. ci-all.sh
