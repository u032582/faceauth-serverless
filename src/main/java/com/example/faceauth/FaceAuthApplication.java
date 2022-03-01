package com.example.faceauth;

import java.util.List;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.function.context.MessageRoutingCallback;
import org.springframework.context.annotation.Bean;

import com.example.faceauth.entity.Account;
import com.example.faceauth.entity.Face;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class FaceAuthApplication {
	@Autowired
	private Blogic blogic;

	public static void main(String[] args) {
		SpringApplication.run(FaceAuthApplication.class, args);
	}

	@Bean
	public Function<Account, List<Face>> getfaces() {
		return value -> blogic.getFace(value);
	}

	@Bean
	public Function<Face, Face> registface() {
		return value -> blogic.registFace(value);
	}

	@Bean
	public Function<Face, Boolean> login() {
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
}
