FROM openjdk:8-jre-alpine
WORKDIR /src
ADD crossdock-java/build/libs/crossdock-0.0.1-SNAPSHOT.jar app.jar
CMD ["/usr/bin/java", "-cp", "app.jar", "works.crossdock.CrossdockClient"]
EXPOSE 8080-8082
