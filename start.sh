#!/bin/bash
# Startup script for PaaS deployment (Railway, Render, etc.)

echo "Building project with Maven..."
mvn clean package -DskipTests

echo "Starting Tomcat embedded server..."
# Use the PORT environment variable if provided by the platform
PORT=${PORT:-8080}
java -jar target/attendances-project-exec.war --server.port=$PORT
