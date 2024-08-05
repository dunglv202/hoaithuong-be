FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /usr/sqa/backend
COPY pom.xml pom.xml
COPY ./src ./src
RUN --mount=type=cache,target=/root/.m2 mvn clean package

FROM eclipse-temurin:17-jdk-alpine
WORKDIR /usr/sqa/backend
COPY --from=build /usr/sqa/backend/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
EXPOSE 8080