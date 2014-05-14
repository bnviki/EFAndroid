package com.mpeers.service;

import com.mpeers.data.ChatMessageTable;
import com.mpeers.data.entities.ChatMessage;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

public class ChatMessageWriter extends IntentService {

	public ChatMessageWriter() {
		super("ChatMessageWriter");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		Bundle values = intent.getExtras();
		String room = values.getString("CHAT_MSG_ROOM");
		String from = values.getString("CHAT_MSG_FROM");
		String message = values.getString("CHAT_MSG_MESSAGE");		
		ChatMessage msg = new ChatMessage(room, from, message);
		ChatMessageTable.getInstance(getApplicationContext()).saveMessage(msg);
	}

}
