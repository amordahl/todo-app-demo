FROM sbtscala/scala-sbt:eclipse-temurin-21.0.8_9_1.11.6_3.7.3 AS builder
WORKDIR /app
COPY . .
RUN ["sbt", "stage"]

FROM eclipse-temurin:21 AS todoapp
WORKDIR /app
COPY --from=builder /app/target/universal/stage /app/
CMD ["/app/bin/todoapp"]