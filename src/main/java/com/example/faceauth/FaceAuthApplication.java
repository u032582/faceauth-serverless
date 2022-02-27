package com.example.faceauth;

import java.util.function.Function;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;

import com.example.faceauth.entity.Face;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class FaceAuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(FaceAuthApplication.class, args);
	}

	@Bean
	public Function<Message<Face>, Face> message() {
		return value -> _message(value);
	}

	@Bean
	public Function<Message<?>, String> apigateway() {
		return event -> {
			log.info("pathParameters= {}", event.getHeaders().get("pathParameters"));
			return event.getPayload().toString();
		};
	}

	public Face _message(Message<Face> value) {
		log.info("AAAAAAAAAAAAAAAAAAAA {}", value.getPayload());
		return value.getPayload();
	}

//	@Override
//	public void initialize(GenericApplicationContext context) {
//		context.registerBean("uppercase", FunctionRegistration.class, () -> new FunctionRegistration<>(this.uppercase())
//				.type(FunctionType.from(String.class).to(String.class)));
//		context.registerBean("event", FunctionRegistration.class, () -> new FunctionRegistration<>(this.event())
//				.type(FunctionType.from(APIGatewayProxyRequestEvent.class).to(String.class)));
//	}

}
