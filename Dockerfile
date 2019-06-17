FROM maven:3.5-jdk-8-alpine AS builder

COPY pom.xml /app/pom.xml
RUN mvn -f /app/pom.xml dependency:go-offline

COPY src /app/src
RUN mvn test -f /app/pom.xml
RUN mvn -f /app/pom.xml package -Dmaven.test.skip=true

FROM openjdk:8-jre-alpine

COPY --from=builder /app/target/*-fat.jar ./app.jar
EXPOSE 8087
CMD ["java", "-jar", "app.jar"]
