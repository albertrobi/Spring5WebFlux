package com.example.webfluxdemo.websockets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.qos.logback.classic.Logger;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

import static java.time.LocalDateTime.now;
import static java.util.UUID.randomUUID;

@Component("ReactiveWebSocketHandler")
public class ReactiveWebSocketHandler implements WebSocketHandler {

    private static final ObjectMapper json = new ObjectMapper();

    private String message = "";
    
    private Flux<String> eventFlux = Flux.generate(sink -> {
	    Event event = new Event(randomUUID().toString(), now().toString(), message);
	    try {
	        sink.next(json.writeValueAsString(event));
	    } catch (JsonProcessingException e) {
	        sink.error(e);
	    }
    });
    
	private Flux<String> intervalFlux = Flux.interval(Duration.ofMillis(800L))
			.zipWith(eventFlux, (time, event) -> event);
	

    @Override
    public Mono<Void> handle(WebSocketSession webSocketSession) {
    	
    	webSocketSession.receive()
 	            .map(WebSocketMessage::getPayloadAsText).subscribe(msg -> { this.message = msg;});
    	 
        return webSocketSession
                .send( intervalFlux.map(webSocketSession::textMessage));
                		
    }
}
