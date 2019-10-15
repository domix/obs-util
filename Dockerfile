#FROM oracle/graalvm-ce:19.2.0.1 as graalvm
#COPY . /home/app/obs-util
#WORKDIR /home/app/obs-util
#RUN gu install native-image
#RUN native-image --no-server -cp build/libs/obs-util-*-all.jar

#FROM frolvlad/alpine-glibc
#EXPOSE 8080
#COPY --from=graalvm /home/app/obs-util .
#ENTRYPOINT ["./obs-util"]

FROM adoptopenjdk:11.0.4_11-jre-openj9-0.15.1

COPY build/libs/obs-util-0.1-all.jar /opt/app/obs-util.jar
COPY run.sh /opt/app/run.sh
RUN chmod +x /opt/app/run.sh
ENTRYPOINT ["/opt/app/run.sh"]