#!/bin/bash
# Startup script for PaaS deployment (Railway, Render, etc.)

echo "Building project with Maven..."
mvn clean package -DskipTests

echo "Starting Jetty server..."
# Use the PORT environment variable if provided by the platform
PORT=${PORT:-8080}
java -jar target/dependency/jetty-runner.jar --port $PORT target/attendances-project.war
