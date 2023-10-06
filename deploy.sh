#!/bin/bash

start=$(date +"%s")

ssh -p ${SERVER_PORT} ${SERVER_USER}@${SERVER_HOST} -i key.txt -t -t -o StrictHostKeyChecking=no <<'ENDSSH'
docker pull tarcisiodelmondes/minebox:latest

CONTAINER_NAME=minebox
if [ -n "$(docker ps -qa -f name=$CONTAINER_NAME)" ]; then
    if [ -n "$(docker ps -q -f name=$CONTAINER_NAME)" ]; then
        echo "Container is running -> stopping it..."
        docker stop $CONTAINER_NAME;
    fi
fi

docker run -d --rm --network=host \
  -e AWS_REGION \
  -e AWS_ACCESS_KEY_ID \
  -e AWS_SECRET_ACCESS_KEY \
  -e BUCKET_NAME \
  -e AWS_REGION \
  -e SENTRY_AUTH_TOKEN \
  -e SENTRY_DSN \
  -e SENTRY_ENABLED \
  -e APP_PROFILE=prod \
  -e POSTGRES_PASSWORD \
  -e POSTGRES_USER \
  -e POSTGRES_DB \
  -e DATABASE_URL \
  -e JWT_SECRET \
  -e JWT_EXPIRATION \
  -e JWT_REFRESH_EXPIRATION \
  -e CORS_ORIGINS \
  --name $CONTAINER_NAME tarcisiodelmondes/minebox:latest

exit
ENDSSH

if [ $? -eq 0 ]; then
	exit 0
else
	exit 1
fi

end=$(data +"%s")

diff=$(($end - $start))

echo "Deployed in: ${diff}s"
