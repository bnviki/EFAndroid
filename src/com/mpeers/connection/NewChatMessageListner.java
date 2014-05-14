package com.mpeers.connection;

import com.mpeers.data.entities.ChatMessage;


public interface NewChatMessageListner {
	public void onNewChatMessage(ChatMessage newMsg);
}
