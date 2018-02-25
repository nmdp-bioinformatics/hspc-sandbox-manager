#!/usr/bin/env bash

set -x

# set values provided at build time
export ACTIVE_ENV=test
export PROJECT_NAME=sandbox-manager
export TARGET_AWS_CLUSTER=hspc-test

. ci-0-set-properties.sh

. ci-1-prepare-sources.sh
#. ci-2-docker-image.sh
. ci-3-aws-task-update.sh
. ci-4-aws-service-update.sh
