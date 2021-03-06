package ro.pss.holidayforms.integrations.clocking;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import ro.pss.holidayforms.service.ClockingService;

@Configuration
@Slf4j
public class ClockingMqttClient {
	@Autowired
	ClockingService service;

	private DefaultMqttPahoClientFactory factory;

	@Bean
	public MqttPahoClientFactory mqttClientFactory() {
		if (factory == null) {
			factory = new DefaultMqttPahoClientFactory();
			MqttConnectOptions options = new MqttConnectOptions();
			options.setServerURIs(new String[]{"tcp://soldier.cloudmqtt.com:14024"});
			options.setUserName("asukbczm");
			options.setPassword("EAR5krLsBzGz".toCharArray());
			options.setCleanSession(false);
			factory.setConnectionOptions(options);
		}
		return factory;
	}

	@Bean
	public MessageProducer inbound() {
		MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter("springClient", mqttClientFactory(),
																							  "clocking");
		adapter.setCompletionTimeout(5000);
		adapter.setConverter(new DefaultPahoMessageConverter());
		adapter.setQos(1);
		adapter.setOutputChannel(mqttInputChannel());
		return adapter;
	}

	@Bean
	@ServiceActivator(inputChannel = "mqttInputChannel")
	public MessageHandler handler() {
		return message -> {
			try {
				log.info(String.format("Receiving MQTT payload: %s", message.getPayload().toString()));
				String[] payloadParts = message.getPayload().toString().split(";");
				String uidPart = payloadParts[0];
				Long timestampPart = Long.parseLong(payloadParts[1]);
				service.addClocking(uidPart, timestampPart);
			} catch (Exception e) {
				log.error(String.format("Error interpreting MQTT payload: %s", message.getPayload().toString()));
			}
		};
	}

	@Bean
	@ServiceActivator(inputChannel = "mqttOutboundChannel")
	public MessageHandler mqttOutbound() {
		MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler("springClient", mqttClientFactory());
		messageHandler.setAsync(true);
		messageHandler.setDefaultTopic("config");
		return messageHandler;
	}

	@Bean
	public MessageChannel mqttInputChannel() {
		return new DirectChannel();
	}

	@Bean
	public MessageChannel mqttOutboundChannel() {
		return new DirectChannel();
	}

	@MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
	public interface MQTTGateway {
		void sendToMqtt(String data);
	}
}