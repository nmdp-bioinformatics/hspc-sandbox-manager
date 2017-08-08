#!/usr/bin/env bash

java \
  -Xms256M \
  -Xmx512M \
  -Xdebug \
  -Xrunjdwp:server=y,transport=dt_socket,address=8000,suspend=n \
  -jar target/hspc-sandbox-manager*.war
