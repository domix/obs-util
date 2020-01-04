#FROM oracle/graalvm-ce:19.2.0.1 as graalvm
#COPY . /home/app/obs-util
#WORKDIR /home/app/obs-util
#RUN gu install native-image
#RUN native-image --no-server -cp build/libs/obs-util-*-all.jar

#FROM frolvlad/alpine-glibc
#EXPOSE 8080
#COPY --from=graalvm /home/app/obs-util .
#ENTRYPOINT ["./obs-util"]

FROM gradle:6.0.1-jdk11 as builder

LABEL stage=builder

WORKDIR /app
COPY . /app
RUN gradle clean build -x test

FROM adoptopenjdk:11.0.5_10-jre-openj9-0.17.0-bionic
COPY --from=builder /app/build/libs/obs-util.jar /opt/app/obs-util.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/opt/app/obs-util.jar"]