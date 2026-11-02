# Production runtime image for Zoho Catalyst AppSail / Docker
FROM eclipse-temurin:25-jre-noble

WORKDIR /app

# Copy the fat JAR built by Gradle
COPY build/libs/Somiq-server-all.jar app.jar

# Alias app-all.jar in case startup commands reference it
RUN ln -sf app.jar app-all.jar

# Make java resolvable even if the runtime overrides PATH (e.g. a Catalyst startup command)
RUN ln -sf /opt/java/openjdk/bin/java /usr/bin/java

# Catalyst passes the port in X_ZOHO_CATALYST_LISTEN_PORT (default 9000); application.yaml reads it
EXPOSE 9000

# TieredStopAtLevel=1 and SerialGC cut JVM startup time; native access silences Netty warnings
CMD ["/opt/java/openjdk/bin/java", "-XX:TieredStopAtLevel=1", "-XX:+UseSerialGC", "--enable-native-access=ALL-UNNAMED", "-jar", "/app/app.jar"]
