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
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /build/target/nirgi-java-0.0.1-SNAPSHOT.jar app.jar
ENV JAVA_OPTS="-XX:+UseG1GC -XX:+UseContainerSupport -Duser.timezone=Europe/Tallinn"
ENV TZ=Europe/Tallinn

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
