# ════════════════════════════════════════════════════════════════════════════
# JasperReports Service - Multi-Stage Dockerfile
# ════════════════════════════════════════════════════════════════════════════
# Uses Java 17 (JasperReports compatibility) in an isolated Docker environment
# while the main project uses Java 25.
# ════════════════════════════════════════════════════════════════════════════

# ────────────────────────────────────────────────────────────────────────────
# Stage 1: Build Stage
# ────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder

# Accept proxy configuration as build arguments
ARG HTTP_PROXY
ARG HTTPS_PROXY
ARG NO_PROXY

# Set proxy environment variables if provided
ENV HTTP_PROXY=${HTTP_PROXY}
ENV HTTPS_PROXY=${HTTPS_PROXY}
ENV NO_PROXY=${NO_PROXY}

WORKDIR /build

# Workaround: Corporate proxy modifies SSL certificates, causing APK failures
# Solution: Use HTTP mirrors instead of HTTPS for Alpine repositories
RUN echo "http://dl-cdn.alpinelinux.org/alpine/v3.22/main" > /etc/apk/repositories && \
    echo "http://dl-cdn.alpinelinux.org/alpine/v3.22/community" >> /etc/apk/repositories

# Install Maven
RUN apk add --no-cache maven

# Copy BOM and parent POMs for dependency resolution
COPY bom/pom.xml /build/bom/pom.xml
COPY root/pom.xml /build/root/pom.xml
COPY root/lib/pom.xml /build/root/lib/pom.xml
COPY root/lib/office/pom.xml /build/root/lib/office/pom.xml
COPY root/lib/office/word/pom.xml /build/root/lib/office/word/pom.xml
COPY root/lib/office/word/jasperreports/pom.xml /build/root/lib/office/word/jasperreports/pom.xml

# Copy sandbox model (dependency)
COPY root/sandbox/pom.xml /build/root/sandbox/pom.xml
COPY root/sandbox/office/pom.xml /build/root/sandbox/office/pom.xml
COPY root/sandbox/office/microsoft/pom.xml /build/root/sandbox/office/microsoft/pom.xml
COPY root/sandbox/office/microsoft/word/pom.xml /build/root/sandbox/office/microsoft/word/pom.xml
COPY root/sandbox/office/microsoft/word/jasperreports/pom.xml /build/root/sandbox/office/microsoft/word/jasperreports/pom.xml
COPY root/sandbox/office/microsoft/word/jasperreports/model /build/root/sandbox/office/microsoft/word/jasperreports/model

# Build the model first (install to local Maven repo)
# Force Java 17 compatibility even though parent uses Java 25

# Step 1: Install all parent POMs in the hierarchy
WORKDIR /build/root/sandbox
RUN mvn install -N -DskipTests -q \
    -Djava.version=17 \
    -Dmaven.compiler.release=17 \
    -Dmaven.compiler.source=17 \
    -Dmaven.compiler.target=17

WORKDIR /build/root/sandbox/office
RUN mvn install -N -DskipTests -q \
    -Djava.version=17 \
    -Dmaven.compiler.release=17 \
    -Dmaven.compiler.source=17 \
    -Dmaven.compiler.target=17

WORKDIR /build/root/sandbox/office/microsoft
RUN mvn install -N -DskipTests -q \
    -Djava.version=17 \
    -Dmaven.compiler.release=17 \
    -Dmaven.compiler.source=17 \
    -Dmaven.compiler.target=17

WORKDIR /build/root/sandbox/office/microsoft/word
RUN mvn install -N -DskipTests -q \
    -Djava.version=17 \
    -Dmaven.compiler.release=17 \
    -Dmaven.compiler.source=17 \
    -Dmaven.compiler.target=17

WORKDIR /build/root/sandbox/office/microsoft/word/jasperreports
RUN mvn install -N -DskipTests -q \
    -Djava.version=17 \
    -Dmaven.compiler.release=17 \
    -Dmaven.compiler.source=17 \
    -Dmaven.compiler.target=17

# Step 2: Build and install model module
WORKDIR /build/root/sandbox/office/microsoft/word/jasperreports/model
RUN mvn clean install -DskipTests -q \
    -Djava.version=17 \
    -Dmaven.compiler.release=17 \
    -Dmaven.compiler.source=17 \
    -Dmaven.compiler.target=17

# Copy server module
COPY root/lib/office/word/jasperreports/server /build/root/lib/office/word/jasperreports/server

# Build the server (this will also build the model dependency)
WORKDIR /build/root/lib/office/word/jasperreports/server
RUN mvn clean package -DskipTests -q \
    -Djava.version=17 \
    -Dmaven.compiler.release=17 \
    -Dmaven.compiler.source=17 \
    -Dmaven.compiler.target=17

# ────────────────────────────────────────────────────────────────────────────
# Stage 2: Runtime Stage
# ────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

# Accept proxy configuration as build arguments
ARG HTTP_PROXY
ARG HTTPS_PROXY
ARG NO_PROXY

# Set proxy environment variables if provided
ENV HTTP_PROXY=${HTTP_PROXY}
ENV HTTPS_PROXY=${HTTPS_PROXY}
ENV NO_PROXY=${NO_PROXY}

WORKDIR /app

# Workaround: Corporate proxy modifies SSL certificates, causing APK failures
# Solution: Use HTTP mirrors instead of HTTPS for Alpine repositories
RUN echo "http://dl-cdn.alpinelinux.org/alpine/v3.22/main" > /etc/apk/repositories && \
    echo "http://dl-cdn.alpinelinux.org/alpine/v3.22/community" >> /etc/apk/repositories

# Install wget for health checks
RUN apk add --no-cache wget

# Copy the fat JAR from build stage
COPY --from=builder /build/root/lib/office/word/jasperreports/server/target/jasperreports-server.jar /app/jasperreports-server.jar

# Copy templates directory
COPY root/sandbox/office/microsoft/word/jasperreports/templates /app/templates

# Create output directory
RUN mkdir -p /app/output && chmod 777 /app/output

# Expose port
EXPOSE 8090

# Health check
HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=40s \
    CMD wget --quiet --tries=1 --spider http://localhost:8090/health || exit 1

# Run the server
ENV PORT=8090
ENTRYPOINT ["java", "-jar", "/app/jasperreports-server.jar"]
