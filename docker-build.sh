#!/usr/bin/env bash

REPO="domix/obs-util"
TAG=$(grep "version" gradle.properties|cut -d'=' -f2)
VERSION=$(echo "$TAG" | tr '[:upper:]' '[:lower:]')


echo $VERSION

docker build -t ${REPO}:${VERSION} -t ${REPO}:latest . && \
   git release $VERSION && \
   docker push ${REPO}:${VERSION} && \
   docker push ${REPO}:latest && \
   echo "Done"

exit_code=$?

docker rmi -f $(docker images -q --filter label=stage=builder)

exit "$exit_code"