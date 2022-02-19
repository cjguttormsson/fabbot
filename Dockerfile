FROM openjdk:19-jdk

COPY . .

RUN ./gradlew build

CMD ["./gradlew", "run"]