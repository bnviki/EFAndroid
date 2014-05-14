package com.mpeers.data;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.sax.StartElementListener;

import com.mpeers.onUserLoginListner;
import com.mpeers.connection.NewChatMessageListner;
import com.mpeers.connection.XmppConnection;
import com.mpeers.data.entities.Chat;
import com.mpeers.data.entities.ChatMessage;
import com.mpeers.data.entities.User;
import com.mpeers.service.ChatMessageWriter;

public class ChatMessageManager implements NewChatMessageListner, onUserLoginListner {
	private ArrayList<ChatMessage> messages;
	private static ChatMessageManager instance;
	
	private Context context;
	private ChatMessageTable msgTable;
	
	public static ChatMessageManager getInstance(Context context){
		if(instance == null){
			instance = new ChatMessageManager(context.getApplicationContext());	
			UserManager.getInstance(context).addLoginListner(instance);
		}
		return instance;
	}
	
	private ChatMessageManager(Context context){
		this.context = context;
		messages = new ArrayList<ChatMessage>();
		msgTable = ChatMessageTable.getInstance(context);
		msgTable.clear();
		XmppConnection.getInstance(context).addNewChatMsgListner(this);
	}
	
	public void addMessage(ChatMessage msg){
		if(ChatMessageTable.getInstance(context).saveMessage(msg)){
			messages.add(msg);
		}		
	}	
	
	public ArrayList<ChatMessage> getRoomMessages(String room){		
		if(room != null && !room.equals("")){
			messages = msgTable.getRoomMessages(room);			
		}
		return messages;
	}
	
	public void sendMessage(String room, String msg){
		XmppConnection.getInstance(context).sendMessage(room, msg);
	}
	
	public void addNewMessageListner(NewChatMessageListner listner){
		XmppConnection.getInstance(context).addNewChatMsgListner(listner);
	}

	@Override
	public void onNewChatMessage(ChatMessage newMsg) {
		Intent savemsg = new Intent(context.getApplicationContext(), ChatMessageWriter.class);
		savemsg.putExtra("CHAT_MSG_ROOM", newMsg.room);
		savemsg.putExtra("CHAT_MSG_FROM", newMsg.from);
		savemsg.putExtra("CHAT_MSG_MESSAGE", newMsg.message);
		context.getApplicationContext().startService(savemsg);
		//msgTable.saveMessage(newMsg);
	}

	@Override
	public void onUserLogin(User user) {
		// TODO Auto-generated method stub
		//msgTable.clear();
	}
}
