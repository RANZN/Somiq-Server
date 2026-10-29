# Production runtime image for Zoho Catalyst AppSail / Docker
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the fat JAR built by Gradle
COPY build/libs/Somiq-server-all.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
