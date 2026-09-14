# --- Этап 1: Сборка приложения ---
# Заменили maven:3.9.9-... на стабильный тег maven:3-...
FROM maven:3-eclipse-temurin-25-alpine AS builder
WORKDIR /build

# Кэшируем зависимости Maven
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копируем исходный код и собираем JAR
COPY src ./src
RUN mvn clean package -DskipTests

# --- Этап 2: Запуск приложения ---
FROM eclipse-temurin:25-jdk-alpine
WORKDIR /app

# Устанавливаем Maven для запуска тестов
RUN apk add --no-cache maven

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Копируем артефакт и исходный код для тестов
COPY --from=builder /build/target/nirgi-java-0.0.1-SNAPSHOT.jar app.jar
COPY --from=builder /build/pom.xml ./pom.xml
COPY --from=builder /build/src ./src

ENV JAVA_OPTS="-XX:+UseG1GC -XX:+UseContainerSupport -Duser.timezone=Europe/Tallinn"
ENV TZ=Europe/Tallinn
ENV SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.mariadb.jdbc.Driver

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]