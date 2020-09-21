package kerberos_example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

public class KafkaProducerKerberos {

	String topicName = "test_topic";
	KafkaProducer<String, String> producer = null;

	/**
	 * Load properties file
	 * 
	 * @param args
	 */
	public void start(String[] args) {
		// Load property files
		if (args.length == 0) {
			System.out.println("Error! pass the properties file");
			System.exit(1);
		}

		String inputPropertiesFIle = args[0];
		Properties props = new Properties();
		try {
			System.out.println("Loading proprties file" + inputPropertiesFIle);
			props.load(new FileReader(new File(inputPropertiesFIle)));
			setup(props);
			publish();
			producer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Setup Kafka producer properties
	 * 
	 * @param props
	 */
	public void setup(Properties props) {
		props.put("key.serializer", StringSerializer.class.getName());
		props.put("value.serializer", StringSerializer.class.getName());
		// props.put("sasl.kerberos.service.name", "kafka");
		if (props.get("topic_name") != null) {
			topicName = (String) props.get("topic_name");
			System.out.println("publishing to topic" + topicName);
		}
		producer = new KafkaProducer<String, String>(props);
	}

	/**
	 * Start the Kafka publisher
	 */
	public void publish() {
		System.out.println("Type message to send and press Enter,  Type \"exit\" to exit");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			try {

				System.out.println("Enter: ");
				String message = br.readLine();
				if (message.equalsIgnoreCase("exit")) {
					System.out.println("Out!");
				}
				System.out.println("Sending message " + message);
				RecordMetadata metadata = producer.send(new ProducerRecord<String, String>(topicName, message)).get();
				if (metadata == null) {
					System.out.println("Error sending message");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Main
	 * @param args
	 */
	public static void main(String[] args) {
		KafkaProducerKerberos producer = new KafkaProducerKerberos();
		producer.start(args);
	}
}
