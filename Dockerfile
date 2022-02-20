FROM openjdk:19-jdk-buster

COPY . .

RUN ./gradlew build
RUN ./gradlew installDist
RUN unzip ./build/distributions/fabbot-1.0-SNAPSHOT.zip

COPY ./build/distributions/fabbot-1.0-SNAPSHOT ./output

CMD ["./output/bin/fabbot"]