#!/usr/bin/env bash

VERSION=0.0.1
REPO="domix/obs-util"

docker build -t ${REPO}:${VERSION} -t ${REPO}:latest . && \
   git release $VERSION && \
   docker push ${REPO}:${VERSION} && \
   docker push ${REPO}:latest && \
   echo "Done"

exit_code=$?

docker rmi -f $(docker images -q --filter label=stage=builder)

exit "$exit_code"