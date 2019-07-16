package com.example.webfluxdemo;

import java.net.URI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;

import reactor.core.publisher.Mono;

@SpringBootApplication
@EnableReactiveMongoRepositories
public class WebfluxDemoApplication {

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(WebfluxDemoApplication.class, args);
		
		 WebSocketClient client = new ReactorNettyWebSocketClient();
	        client.execute(
	          URI.create("ws://localhost:8080/event-emitter"), 
	          session -> session.send(
	            Mono.just(session.textMessage("event-spring-reactive-client-websocket")))
	            .thenMany(session.receive()
	              .map(WebSocketMessage::getPayloadAsText)
	              .log())
	            	.then());      	
		
	}
}
