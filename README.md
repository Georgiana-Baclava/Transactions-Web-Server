# Transactions-Web-Server

This is a simple web server implemented in Java and it expose an API used for adding and analysing transactions.

The data are received in JSON format and saved into MongoDB.

## IMPLEMENTATION

It spports the following operations:

* POST to http://127.0.0.1:5000/transactions/ with a JSON payload of the form: {“sender”:
sender_id(integer), “receiver”: receiver_id(integer), “timestamp”: ts(integer), “sum”:
x(integer)}. This adds a new transaction into the database.

* GET to http://127.0.0.1:5000/transactions/?user=XXXX&day=YYYY&threshold=ZZZZ
Given a user, a day and a threshold, returns a with list all transactions in which the user was involved
in the given day.

* GET to http://127.0.0.1:5000/balance/?user=XXXX&since=YYYY&until=ZZZZ Given a
timeframe and an user, obtain the user's balance in the given timeframe. At the beginning of
the timeframe the user has balance 0.


In run.sh, if the test parameter is set, the tests are runned via mvn clean test command.
If the build parameter is set or no parameter is set at all, the executable jar is created and the server is built and runned via Dockerfile and docker-composite file.

In order to run the web server on any platform, I've created 2 Docker containers, one for the web server and the other for the database and linked them with a docker-compose file.

In the Dockerfile, I use anapsix/alpine-java as image for the web server container, set working directory to /home, copy the runnable jar here and start the web server with the command: java -jar trx-v1-jar-with-dependencies.jar


## PREREQUISITES

* install maven

* add maven to your path

* install docker

* for Linux OS, install also docker-compose

## RUN

To run the tests, use the following command:

./run.sh tests

In order to run the web server, use one of this commands:

 ./run.sh
 
 ./run.sh build
