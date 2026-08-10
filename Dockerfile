FROM eclipse-temurin:17-jre
ENV TZ=Asia/Seoul
COPY build/libs/*SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "/app.jar"]
