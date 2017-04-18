FROM openjdk:8-jre-alpine
WORKDIR /src
ADD yarpc-crossdock/works.build/libs/yarpc-crossdock-jar-with-dependencies.jar app.jar
CMD ["/usr/bin/java", "-cp", "app.jar", "io.yarpc.crossdock.CrossdockTestClient"]
EXPOSE 8080-8082
