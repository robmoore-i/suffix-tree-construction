FROM openjdk:11
ARG JAR_FILE=build/libs/LSystems-1.0.jar
COPY ${JAR_FILE} lsystems.jar
ENTRYPOINT ["java","-jar","/lsystems.jar"]
