# build stage
FROM gradle:7.1-jdk11 AS build

WORKDIR /project

COPY src ./src
COPY *.gradle ./
COPY gradle.properties ./

RUN gradle shadowJar

# execution stage
FROM openjdk:11

WORKDIR /app

COPY --from=build /project/build/libs/scooternav-s-*-all.jar ./application.jar
COPY src/main/resources ./resources

CMD ["java", "-jar", "application.jar"]
