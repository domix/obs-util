#!/usr/bin/env bash

docker build -f Dockerfile_build .
exit_code=$?

docker rmi -f $(docker images -q --filter label=stage=builder)

exit "$exit_code"