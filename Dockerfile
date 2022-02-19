FROM gradle:latest

COPY . .

RUN ./gradlew build

COPY . .

CMD ["./gradlew", "run"]