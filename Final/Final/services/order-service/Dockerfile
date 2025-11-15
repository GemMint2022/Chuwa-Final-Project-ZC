FROM openjdk:17-ea-alpine

RUN apk update && apk add curl && rm -rf /var/cache/apk/*

WORKDIR /app

COPY target/order-service-1.0.0.jar app.jar

EXPOSE 8083

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8083/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]