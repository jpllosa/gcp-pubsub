package com.blogspot.jpllosa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.annotation.MessagingGateway;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.integration.outbound.PubSubMessageHandler;

import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.MessageHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;
import com.google.cloud.spring.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import org.springframework.beans.factory.annotation.Qualifier;

@SpringBootApplication
@RestController
public class GcpPubsubApplication {

	public static void main(String[] args) {
		SpringApplication.run(GcpPubsubApplication.class, args);
	}

	// Send
	@MessagingGateway(defaultRequestChannel = "gcpPubsubOutputChannel")
	public interface GcpPubsubOutboundGateway {
		void sendToGcpPubsub(String text);
	}

	@Bean
	@ServiceActivator(inputChannel = "gcpPubsubOutputChannel")
	public MessageHandler messageSender(PubSubTemplate pubsubTemplate) {
		return new PubSubMessageHandler(pubsubTemplate, "gcp-pubsub-example");
	}

	@Autowired
	private GcpPubsubOutboundGateway messagingGateway;

	@PostMapping("/postMessage")
	public RedirectView postMessage(@RequestParam("message") String message) {
		this.messagingGateway.sendToGcpPubsub(message);
		return new RedirectView("/");
	}

	// Receive
	@Bean
	public MessageChannel gcpPubsubInputChannel() {
		return new DirectChannel();
	}

	@Bean
	public PubSubInboundChannelAdapter messageChannelAdapter(
			@Qualifier("gcpPubsubInputChannel") MessageChannel inputChannel,
			PubSubTemplate pubSubTemplate) {
		PubSubInboundChannelAdapter adapter =
				new PubSubInboundChannelAdapter(pubSubTemplate, "gcp-pubsub-example-sub");
		adapter.setOutputChannel(inputChannel);

		return adapter;
	}

	@ServiceActivator(inputChannel = "gcpPubsubInputChannel")
	public void messageReceiver(String message) {
		System.out.println("Message received: " + message);
	}


}
