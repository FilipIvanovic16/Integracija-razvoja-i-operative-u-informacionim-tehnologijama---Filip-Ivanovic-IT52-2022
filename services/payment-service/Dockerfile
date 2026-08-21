FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml .
COPY shared/pom.xml shared/pom.xml
COPY services/auth-service/pom.xml services/auth-service/pom.xml
COPY services/catalog-service/pom.xml services/catalog-service/pom.xml
COPY services/order-service/pom.xml services/order-service/pom.xml
COPY services/payment-service/pom.xml services/payment-service/pom.xml
COPY services/api-gateway/pom.xml services/api-gateway/pom.xml
COPY services/notification-service/pom.xml services/notification-service/pom.xml

COPY shared/src shared/src
COPY services/payment-service/src services/payment-service/src

RUN mvn -B -q -pl services/payment-service -am package -DskipTests

FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S chronoshop && adduser -S chronoshop -G chronoshop
WORKDIR /app
COPY --from=build /workspace/services/payment-service/target/payment-service-*.jar app.jar
USER chronoshop:chronoshop

EXPOSE 8084
HEALTHCHECK --interval=30s --timeout=3s --start-period=45s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8084/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=60", "-jar", "app.jar"]
