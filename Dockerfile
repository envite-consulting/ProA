FROM registry.access.redhat.com/ubi8/openjdk-21:1.20
COPY backend/target/*.jar /app/proa-run.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/proa-run.jar"]
