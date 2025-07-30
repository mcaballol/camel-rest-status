FROM registry.access.redhat.com/ubi8/openjdk-17
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} /app-q.jar
ENV JAVA_OPTS=""
ENTRYPOINT exec java $JAVA_OPTS -jar /app-q.jar
