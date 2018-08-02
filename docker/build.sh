#!/usr/bin/env bash

tag="hspconsortium/sandbox-manager:latest"
if [ $# -gt 0 ]; then
  tag=$1
fi

target_env="local"
if [ $# -gt 1 ]; then
  target_env=$2
fi

# files must be in a folder or subfolder
cd ..

docker \
  build -t $tag \
  --build-arg TARGET_ENV=$target_env \
  .

cd docker
