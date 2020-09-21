package kerberos_example;

import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

/**
 * @author madar
 *
 */
public class KafkaConsumerKerberos {

	String topicName = null;
	KafkaConsumer<String, String> kafkaConsumer = null;

	/**
	 * Load propery file
	 * 
	 * @param args
	 */
	public void start(String args[]) {
		if (args.length == 0) {
			System.out.println("Error pass propery file as an arg.");
			System.exit(1);
		}
		String inputPropertiesFile = args[0];
		Properties props = new Properties();
		try {
			System.out.println("Loading properties file " + inputPropertiesFile);
			props.load(new FileReader(new File(inputPropertiesFile)));
			setup(props);
			startConsumer();
			kafkaConsumer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Setup broker properties
	 * 
	 * @param props
	 */
	public void setup(Properties props) {
		props.put("key.deserializer", StringDeserializer.class.getName());
		props.put("value.deserializer", StringDeserializer.class.getName());
		props.put("enable.auto.commit", "true");
		props.put("auto.commit.interval.ms", "1000");
		props.put("sessio.timeout.ms", "30000");
		// Kerberos props
		if (props.get("topic_name") != null) {
			topicName = (String) props.get("topic_name");
			System.out.println("Consuming from topic=" + topicName);
		}
		kafkaConsumer = new KafkaConsumer<String, String>(props);
		kafkaConsumer.subscribe(Arrays.asList(topicName));
	}

	/**
	 * Start the Kafka consumer
	 */
	public void startConsumer() {
		System.out.println("Listening on topic=" + topicName + ", kafkaConsumer=" + kafkaConsumer);
		while (true) {
			ConsumerRecords<String, String> records = kafkaConsumer.poll(100);
			for (ConsumerRecord<String, String> record : records) {
				String key = record.key();
				String value = record.value();

				System.out.println((key != null ? "key=" + key + ", " : "") + "value=" + value);
			}
		}

	}

	/**
	 * Main
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		KafkaConsumerKerberos consumerKerberos = new KafkaConsumerKerberos();
		consumerKerberos.start(args);
	}
}
