# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
# ⭐ Download dependencies trước — tận dụng Docker cache
#    Lần sau build sẽ nhanh hơn nhiều nếu pom.xml không đổi
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run
# ⭐ Dùng image nhỏ hơn để chạy — không cần Maven nữa
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Render dùng PORT env variable
EXPOSE 8080

ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]