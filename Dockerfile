FROM oracle/graalvm-ce:19.2.0.1 as graalvm
COPY . /home/app/obs-util
WORKDIR /home/app/obs-util
RUN gu install native-image
RUN native-image --no-server -cp build/libs/obs-util-*-all.jar

FROM frolvlad/alpine-glibc
EXPOSE 8080
COPY --from=graalvm /home/app/obs-util .
ENTRYPOINT ["./obs-util"]
