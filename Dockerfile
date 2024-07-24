# Stage 1: Build the application
FROM gradle:jdk21 as builder

# Copy the build script and source code
COPY --chown=gradle:gradle . /home/gradle/src

# Set the working directory
WORKDIR /home/gradle/src

# Build the application
RUN gradle build --no-daemon

# Stage 2: Run the application
FROM openjdk:21-jdk

# Copy the JAR file from the previous stage
COPY --from=builder /home/gradle/src/build/libs/*.jar /app/application.jar

# Copy the .env file from the previous stage
COPY --from=builder /home/gradle/src/.env /app/.env

# Set the working directory
WORKDIR /app

# Run the application
CMD ["java", "-jar", "application.jar"]
