FROM openjdk:19-jdk-buster

COPY . .

# RUN ./gradlew build
RUN ./gradlew installDist

COPY ./build/install/ ./output/

CMD ["./output/bin/fabbot"]