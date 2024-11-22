# Step 1: Build Stage - Install Maven and use Java 21
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Install Maven
RUN apk add --no-cache maven

COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Step 2: Runtime Stage - Using Java 21 for the runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080

# Set default config location to /config
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=classpath:/application.properties,/config/application.properties"]