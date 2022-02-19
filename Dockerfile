FROM openjdk:19-jdk

COPY . .

RUN ./gradlew test

CMD ["./gradlew", "run"]