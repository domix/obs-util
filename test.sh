#!/usr/bin/env bash

docker build -f Dockerfile_build . && \
  docker rmi -f $(docker images -q --filter label=stage=builder)