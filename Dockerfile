# Build stage
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# Copy gradle files
COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle

# Download dependencies
RUN gradle dependencies --no-daemon || true

# Copy source code
COPY src src

# Build application
RUN gradle bootJar --no-daemon -x test

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Create non-root user
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

# Copy jar from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Set JVM options
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseContainerSupport"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]