# kafka_kerberos_demo
First, you need to create principals and key tabs for Kafka_server, kafka_client, zookeeper server and zookeeper client then download kafka and put it in the repo root
## Configuration
### Kafka server
#### kafka_server_jaas.conf
```
KafkaServer {
    com.sun.security.auth.module.Krb5LoginModule required debug=true
    useKeyTab=true
    storeKey=true
    useTicketCache=false
    keyTab="kafka.keytab"
    principal="kafka/kclient.madar.com@MADAR.COM";
};
Client {
    com.sun.security.auth.module.Krb5LoginModule required debug=true
    useKeyTab=true
    storeKey=true
    useTicketCache=false
    keyTab="zkclient.keytab"
    principal="zkclient@MADAR.COM";
};

```
#### server.properties
```
group.initial.rebalance.delay.ms=0
listeners=SASL_PLAINTEXT://kclient.madar.com:9092
security.inter.broker.protocol=SASL_PLAINTEXT
sasl.mechanism.inter.broker.protocol=GSSAPI
sasl.enabled.mechanism=GSSAPI
sasl.kerberos.service.name=kafka
```
#### kafka_client_jaas.conf
```
KafkaClient {
      	com.sun.security.auth.module.Krb5LoginModule required
        useKeyTab=true
        storeKey=true
        keyTab="jane.keytab"
        principal="jane@EXAMPLE.COM";
};
```
### Zookeeper configuration
#### zookeeper_jaas.conf
```
Server {
       com.sun.security.auth.module.Krb5LoginModule required
       useKeyTab=true
       keyTab="/etc/zookeeper.keytab"
       storeKey=true
       useTicketCache=false
       principal="zookeeper/localhost@MADAR.COM";
};
```
### Producer and consumer properties
#### input.properties
```
bootstrap.servers=kclient.madar.com:9092
client.id=test_client
group.id=test_consumer
topic_name=test101
```
## Running
### start the zookeeper server
```
./run_zookeeper.sh
```
### start the kafka server
```
./run_zookeeper.sh
```
### Create the kafka topic
```
bin/kafka-topics.sh --zookeeper localhost:2181 --create --topic test101 --partitions 1 --replication-factor 1
```
### Start the producer app
```
./start_producer.sh input.properties kafka_client_jaas.conf
```
### Start the consumer app
```
./start_consumer.sh input.properties kafka_client_jaas.conf
```
