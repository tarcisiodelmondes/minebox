FROM maven:3.8.3-openjdk-17 AS build

RUN mkdir /project
COPY . ./project
WORKDIR /project

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk-focal

RUN mkdir /app

RUN addgroup --gid 1001 --system minegroup
RUN adduser --system mine --uid 1001

COPY --from=build /project/target/minebox-0.0.1-SNAPSHOT.jar /app/minebox.jar

WORKDIR /app

RUN chown -R mine:minegroup /app

CMD java $JAVA_OPTS -jar minebox.jar
