FROM maven:3.8.3-openjdk-17 AS build

RUN mkdir /project
COPY . ./project
WORKDIR /project

# AWS
ARG aws_region
ENV AWS_REGION=$aws_region

ARG aws_access_key_id
ENV AWS_ACCESS_KEY_ID=$aws_access_key_id

ARG aws_secret_access_key
ENV AWS_SECRET_ACCESS_KEY=$aws_secret_access_key

ARG bucket_name
ENV BUCKET_NAME=$bucket_name

ARG aws_region
ENV AWS_REGION=$aws_region

# SENTRY
ARG sentry_enabled
ENV SENTRY_ENABLED=$sentry_enabled

ARG sentry_dsn
ENV SENTRY_DSN=$sentry_dsn

ARG sentry_auth_token
ENV SENTRY_AUTH_TOKEN=$sentry_auth_token

ARG cors_origins
ENV CORS_ORIGINS=$cors_origins

ARG app_profile
ENV APP_PROFILE=$app_profile

# JWT
ARG jwt_secret
ENV JWT_SECRET=$jwt_secret

ARG jwt_expiration
ENV JWT_EXPIRATION=$jwt_expiration

ARG jwt_refresh_expiration
ENV JWT_REFRESH_EXPIRATION=$jwt_refresh_expiration

# SWAGGER
ARG prod_url
ENV PROD_URL=$prod_url

RUN mvn clean package

FROM eclipse-temurin:17-jdk-focal

RUN mkdir /app

RUN addgroup --gid 1001 --system minegroup
RUN adduser --system mine --uid 1001

COPY --from=build /project/target/minebox-0.0.1-SNAPSHOT.jar /app/minebox.jar

WORKDIR /app

RUN chown -R mine:minegroup /app

CMD java $JAVA_OPTS -jar minebox.jar
