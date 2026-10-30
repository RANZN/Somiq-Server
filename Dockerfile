# Production runtime image for Zoho Catalyst AppSail / Docker
FROM eclipse-temurin:25-jre-noble

WORKDIR /app

# Copy the fat JAR built by Gradle
COPY build/libs/Somiq-server-all.jar app.jar

# Alias app-all.jar in case startup commands reference it
RUN ln -sf app.jar app-all.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
