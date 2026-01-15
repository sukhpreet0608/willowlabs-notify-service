# Stage 1: Build the application
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copy the pom.xml and maven wrapper to cache dependencies
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline

# Copy the source code and build the jar
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Create the runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Add a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# --- ADDED CODE TO FIX DATABASE PERMISSIONS ---
RUN mkdir /data && chown spring:spring /data
# -----------------------------------------------

USER spring:spring

# Copy the built jar from the build stage (Maven puts it in 'target')
COPY --from=build /app/target/*.jar app.jar

# Expose the API port
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]