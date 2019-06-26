package com.example.webfluxdemo.websockets;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class Event {
	String eventId;
    String eventDt;
    String msg;
    
    public Event(String eventId, String eventDt, String msg) {
		this.eventId = eventId;
		this.eventDt = eventDt;
		this.msg = msg;
	}


}
