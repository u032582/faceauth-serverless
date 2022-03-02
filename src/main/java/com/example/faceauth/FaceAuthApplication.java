package com.example.faceauth;

import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.function.context.FunctionRegistration;
import org.springframework.cloud.function.context.FunctionType;
import org.springframework.cloud.function.context.MessageRoutingCallback;
import org.springframework.cloud.function.context.config.RoutingFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.GenericApplicationContext;

import com.example.faceauth.entity.Face;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class FaceAuthApplication {
	@Autowired
	private Blogic blogic;
	@Autowired
	private RoutingFunction routingFunction;

	public static void main(String[] args) {
		SpringApplication.run(FaceAuthApplication.class, args);
	}

	@Bean
	public Function<Face, Face> getface() {
		return value -> blogic.getFace(value);
	}

	@Bean
	public Function<Face, Face> echo() {
		return input -> input;
	}

	@Bean
	public Function<Face, Face> registface() {
		return value -> blogic.registFace(value);
	}

	@Bean
	public Function<Face, BlogicResponse> login() {
		return value -> blogic.login(value);
	}

	@Bean
	public MessageRoutingCallback messageRouter() {
		return message -> {
			String funcname = message.getHeaders().get("spring.cloud.function.definition").toString();
			log.info("routing function: {}", funcname);
			return funcname;
		};
	}

	public void initialize(GenericApplicationContext context) {
		context.registerBean("getface", FunctionRegistration.class,
				() -> new FunctionRegistration<Function<Face, Face>>(getface())
						.type(FunctionType.from(Face.class).to(Face.class).getType()));
		context.registerBean("echo", FunctionRegistration.class,
				() -> new FunctionRegistration<Function<Face, Face>>(echo())
						.type(FunctionType.from(Face.class).to(Face.class).getType()));
		context.registerBean(MessageRoutingCallback.class, this::messageRouter);
	}
}
