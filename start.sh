#!/bin/bash

# Create logs directory if it doesn't exist
mkdir -p logs

# Set JVM options for Render free tier (512MB RAM limit)
export JAVA_OPTS="-Xmx400m -Xms200m -server -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:+UseContainerSupport"

# Start the application with production profile
java $JAVA_OPTS -Dspring.profiles.active=prod -jar build/libs/chamggae.jar 