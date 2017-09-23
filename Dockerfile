FROM openjdk:8-jdk-alpine
ADD target/hspc-sandbox-manager-*.war app.war
ADD target/dependency/jetty-runner.jar jetty-runner.jar
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar app.war" ]