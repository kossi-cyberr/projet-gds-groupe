# --- Étape 1 : Build ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# --- Étape 2 : Image finale (légère) ---
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar /tmp/
RUN find /tmp -maxdepth 1 -name "*.jar" ! -name "*.original" -exec mv {} /app/app.jar \;
EXPOSE 8089
ENTRYPOINT ["java", "-jar", "app.jar"]
