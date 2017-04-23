FROM anapsix/alpine-java
WORKDIR /home
COPY target/trx-v1-jar-with-dependencies.jar trx-v1-jar-with-dependencies.jar
CMD ["java","-jar","trx-v1-jar-with-dependencies.jar"]
