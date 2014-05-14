package com.mpeers.data.entities;

public class ChatMessage {
	public String room;	
	public String message;
	public String from;
	
	public ChatMessage(String room, String from, String msg){
		this.room = room;	
		this.message = msg;
		this.from = from;		
	}
}
