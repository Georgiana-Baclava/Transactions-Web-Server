#!/bin/bash
mvn package
chmod 744 target/trx-v1-jar-with-dependencies.jar
docker-compose up --build
