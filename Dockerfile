# ---- Build stage: compile the jar inside the image ----
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Copy the Maven wrapper and pom first so dependency downloads are cached
# as a layer and only re-run when pom.xml changes.
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
RUN ./mvnw -q dependency:go-offline

COPY src src
RUN ./mvnw -q package -DskipTests

# ---- Run stage: slim image with only the JRE and the jar ----
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
