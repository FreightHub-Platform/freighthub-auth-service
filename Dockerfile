FROM openjdk:22-jdk-oracle

WORKDIR /app

COPY target/auth_service-0.0.1-SNAPSHOT.jar /app/auth_service-0.0.1.jar

COPY application.properties /app/application.properties

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/auth_service-0.0.1.jar" , "--spring.config.location=file:/app/application.properties"]
