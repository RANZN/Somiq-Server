# Production runtime image for Zoho Catalyst AppSail / Docker
FROM eclipse-temurin:25-jre-noble

WORKDIR /app

# Copy the fat JAR built by Gradle
COPY build/libs/Somiq-server-all.jar app.jar

# Alias app-all.jar in case startup commands reference it
RUN ln -sf app.jar app-all.jar

# Make java resolvable even if the runtime overrides PATH (e.g. a Catalyst startup command)
RUN ln -sf /opt/java/openjdk/bin/java /usr/bin/java

EXPOSE 8080

ENTRYPOINT ["/opt/java/openjdk/bin/java", "-jar", "/app/app.jar"]
