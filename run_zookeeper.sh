#!/bin/bash
export KAFKA_OPTS="-Djava.security.auth.login.config=zookeeper_jaas.conf"
kafka/bin/zookeeper-server-start.sh zookeeper.properties

