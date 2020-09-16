#!/bin/bash

export KAFKA_OPTS="-Djava.security.auth.login.config=kafka_server_jaas.conf"
kafka/bin/kafka-server-start.sh server.properties

