#!/usr/bin/env bash

REPO=$(grep "repo" gradle.properties|cut -d'=' -f2)
TAG=$(grep "version" gradle.properties|cut -d'=' -f2)
VERSION=$(echo "$TAG" | tr '[:upper:]' '[:lower:]')

echo $VERSION

./gradlew clean jibDockerBuild -x test && \
  git release $VERSION && \
  docker tag ${REPO}:${VERSION} docker.pkg.github.com/${REPO}/obs-util:${VERSION} && \
  docker push ${REPO}:${VERSION} && \
  docker push docker.pkg.github.com/${REPO}/obs-util:${VERSION} && \
  echo "Done"
