# Transactions-Web-Server

This is a simple web server that expose an API used for adding and analysing transactions.



## IMPLEMENTATION

It is an HTTP server implemented in Java. The data is received in JSON format and saved into MongoDB.

The server is listening on port 5000.

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


For MongoDB connection, I've created a Singleton. Here, I initialize the MongoClient, the collection and I create the indexes for retrieving the data efficiently. The data is saved in the same format as received from post request.

I used hash indexes for sender and receiver, as in the queries performed, there is no need for sorting based on them. For timestamp and sum I used range indexes in order to perform sorting on the data.

Based on these indexes, all 3 operations have O(long(n)) complexity. Searching only for sender or receiver, is O(1), but the range indexes from timestamp and sum give the final complexity of O(long(n)).

* Post method
I retrieve the request body, convert to JSONObject and insert it into database.

* Get transactions based on a given user, day and threshold
Given the day as timestamp (in seconds), the query search for all transactions for which one of the involved party is the user, the timestamp fits in the day interval given as parameter and the sum is greater than the threshold.

* Get user balance in a specific interval
I've created 2 queries: one for the amount received and the other for the amount sent.
In the first query the user is representing the receiver, and in the last one the user is the transaction's sender.


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

## TODO

Implement multithreaded version
