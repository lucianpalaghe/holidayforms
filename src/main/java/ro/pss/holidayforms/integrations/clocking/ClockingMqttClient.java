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
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import ro.pss.holidayforms.domain.ClockingRecord;
import ro.pss.holidayforms.domain.User;
import ro.pss.holidayforms.domain.repo.ClockingRecordRepository;
import ro.pss.holidayforms.domain.repo.UserRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Configuration
@Slf4j
public class ClockingMqttClient {
	@Autowired
	private ClockingRecordRepository clockingRepo;
	@Autowired
	private UserRepository userRepo;

	@Bean
	public MqttPahoClientFactory mqttClientFactory() {
		DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
		MqttConnectOptions options = new MqttConnectOptions();
		options.setServerURIs(new String[]{"tcp://soldier.cloudmqtt.com:14024"});
		options.setUserName("asukbczm");
		options.setPassword("EAR5krLsBzGz".toCharArray());
		options.setCleanSession(false);
		factory.setConnectionOptions(options);
		return factory;
	}

	@Bean
	public MessageProducer inbound(MqttPahoClientFactory factory) {
		MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter("springClient", factory, "/clocking");
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
			log.info(String.format("MQTT message received: %s. Trying to create clocking record...", message));
			createClocking(message);
		};
	}

	private void createClocking(Message message) {
		String uid = message.getPayload().toString();
		Optional<User> userOptional = userRepo.findByClockingCardId(uid);
		userOptional.ifPresentOrElse(u -> {
			ClockingRecord r = new ClockingRecord();
			r.setEmployee(u);
			r.setDateTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(message.getHeaders().getTimestamp()), ZoneId.systemDefault()));
			clockingRepo.save(r);
			log.info(String.format("Created clocking for %s", u.getEmail()));
		}, () -> log.info(String.format("User not found for UID: %s. Clocking not created!", uid)));
	}

	@Bean
	@ServiceActivator(inputChannel = "mqttOutboundChannel")
	public MessageHandler mqttOutbound(MqttPahoClientFactory factory) {
		MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler("testClient", factory);
		messageHandler.setAsync(true);
		messageHandler.setDefaultTopic("/config");
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
