#!/bin/bash
if [ -v $1 ] || [ "$1" == "build" ]; then
	mvn package -Dmaven.test.skip=true
	chmod 744 target/trx-v1-jar-with-dependencies.jar
	docker-compose up --build
elif [ "$1" == "tests" ]; then
	mvn clean test -Dtest=TransactionsTests.java
else
	echo "[USAGE]: ./run.sh [<tests>/<build>]"
fi

