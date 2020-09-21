# kafka_kerberos_demo
First, you need to create principals and key tabs for Kafka_server, kafka_client, zookeeper server and zookeeper client then download kafka and put it in the repo root
## Configuration
### Setup TLS keys
#### 1 Run Vault
------------
* Download [Vault](https://www.vaultproject.io/downloads.html), and startup an instance in dev mode:

```
./vault server -dev
```

* In another shell, run these Vault commands:

```
set VAULT_ADDR=http://127.0.0.1:8200
```

... and then:

```
./vault auth enable userpass
./vault policy-write writers writers.hcl
./vault write auth/userpass/users/vault_user password=vault_pass policies=writers
./vault secrets enable pki -path=pki/
./vault secrets tune -max-lease-ttl=87600h pki
./vault write pki/root/generate/internal common_name=myvault.com ttl=87600h
./vault write pki/roles/kafka-broker allowed_domains="madar.com" allow_subdomains="true" max_ttl="72h"
./vault write pki/roles/kafka-consumer allowed_domains="madar.com" allow_subdomains="true" max_ttl="72h"
./vault write pki/roles/kafka-producer allowed_domains="madar.com" allow_subdomains="true" max_ttl="72h"
./vault write pki/issue/kafka-broker common_name=kclient.madar.com
```

The `writers.hcl` file is located in the root of this repo.

#### 2 Create a Root CA
-------------------

* That last Vault command will write to the console three blocks of text in PEM format.  Copy those three blocks into 
  text files named `certificate.pem`, `issuing_ca.pem`, and `private_key.pem`.

* Convert the PEM-formatted private key and certificate chain issued by Vault into a PCKS#12 keystore:

```
openssl pkcs12 -export -out keystore.pkcs12 -in certificate.pem -inkey private_key.pem
```

* Convert the PCKS#12 keystore into a Java keystore:

```
keytool -importkeystore -srckeystore keystore.pkcs12 -srcstoretype PKCS12 -destkeystore keystore.jks -deststorepass <password from previous step>
```

* Convert the PEM-formatted CA certificate into binary DER format:

```
openssl x509 -in issuing_ca.pem -out ca.der -outform der
```

* Convert the DER file into a Java truststore:

```
keytool.exe -importcert -alias CARoot -file ca.der -keystore truststore.jks
```
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
        keyTab="kafka_client.keytab"
        useTicketCache=false
        serviceName=kafka
        principal="kafka_client/kclient.madar.com@MADAR.COM";
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
