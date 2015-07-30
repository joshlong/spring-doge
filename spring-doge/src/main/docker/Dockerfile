FROM java:8
MAINTAINER Patrick Chanezon <patrick@chanezon.com>
VOLUME /tmp
ADD spring-doge.jar app.jar
RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java",  "-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
