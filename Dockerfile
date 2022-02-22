FROM openjdk:19-jdk-buster

COPY gradle/ gradle/
COPY gradlew gradlew
COPY settings.gradle.kts settings.gradle.kts
COPY build.gradle.kts build.gradle.kts
COPY gradle.properties gradle.properties
COPY src/ src/

# RUN ./gradlew build
RUN ./gradlew installDist

COPY ./build/install/fabbot/ .

CMD ["./bin/fabbot"]