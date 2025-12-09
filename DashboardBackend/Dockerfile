# -----------------------------------------------------------------------------
# STAGE 1: Build the Application
# -----------------------------------------------------------------------------
# We use a Maven image with JDK to compile the code
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# 1. Copy only the POM file first
COPY pom.xml .

# 2. Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline

# 3. Copy the actual source code
COPY src ./src

# 4. Build the JAR file (skipping tests to speed up the container build)
RUN mvn clean package -DskipTests

# -----------------------------------------------------------------------------
# STAGE 2: Run the Application
# -----------------------------------------------------------------------------
# We use a JRE (Java Runtime Environment) image which is much smaller than JDK
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 5. Copy only the built JAR from the 'build' stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]