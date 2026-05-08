# =============================================
# HA Cluster Demo — Multi-stage Dockerfile
# =============================================

# Stage 1: Build with Maven
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /build

# Copy pom.xml and download dependencies first (cache layer)
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests -q

# Stage 2: Minimal runtime image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Add a non-root user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Copy the built jar
COPY --from=builder /build/target/ha-cluster-demo-1.0.0.jar app.jar

# Change ownership
RUN chown appuser:appuser app.jar

USER appuser

# Health check using curl (install in base image) or wget
HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=3 \
    CMD wget -qO- http://localhost:8080/api/health || exit 1

EXPOSE 8080

ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-jar", "app.jar"]
