FROM gradle:latest as build

# This is the default, but there's no way to get it dynamically.
# It's set explicitly here to ensure the final COPY command is correct.
WORKDIR /home/gradle

COPY src/ src/
COPY cards.db settings.gradle.kts build.gradle.kts gradle.properties ./

RUN gradle build --no-daemon
RUN gradle installDist --no-daemon

FROM openjdk:11-jre-slim

COPY --from=build /home/gradle/build/install/fabbot/ .
COPY cards.db cards.db

CMD ["./bin/fabbot"]