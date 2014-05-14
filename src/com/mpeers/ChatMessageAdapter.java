package com.mpeers;

import java.util.ArrayList;

import com.mpeers.data.UserManager;
import com.mpeers.data.entities.Chat;
import com.mpeers.data.entities.ChatMessage;
import com.mpeers.data.entities.User;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ChatMessageAdapter extends ArrayAdapter<ChatMessage>{
	private Context context;
	private ArrayList<ChatMessage> values;
	private Chat chat;
	
	public ChatMessageAdapter(Context context, ArrayList<ChatMessage> messages, Chat chat) {
		super(context, R.layout.chat_message, messages);
		this.context = context;
		this.values = messages;
		this.chat = chat;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.chat_message, parent, false);
		TextView titleView = (TextView) rowView.findViewById(R.id.title);
		TextView messageView = (TextView) rowView.findViewById(R.id.message);
		

		ChatMessage currentMsg = values.get(position);	
		if(currentMsg.from.equals("anonymous"))
			titleView.setText(chat.anonymousUser.name);
		else
			titleView.setText(currentMsg.from);
		messageView.setText(currentMsg.message);
		
		return rowView;
	}

}
