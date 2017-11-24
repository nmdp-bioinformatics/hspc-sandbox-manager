#!/usr/bin/env bash

. prepare-build.sh

docker image build --tag $IMAGE_NAME .
