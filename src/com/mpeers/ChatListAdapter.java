package com.mpeers;

import java.util.ArrayList;
import java.util.HashMap;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.mpeers.data.UserManager;
import com.mpeers.data.entities.Chat;
import com.mpeers.data.entities.User;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ChatListAdapter extends ArrayAdapter<Chat> {
	private final Context context;
	private final ArrayList<Chat> values;	

	public ChatListAdapter(Context context, ArrayList<Chat> chats) {
		super(context, R.layout.chat_list_item, chats);
		this.context = context;
		this.values = chats;		
	}	

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.chat_list_item, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.name);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);		

		Chat currentChat = values.get(position);		
		if(currentChat.anonymousChat){
			textView.setText(currentChat.anonymousUser.name);
		} else {
			for(User user : currentChat.users){
				if(user.id != UserManager.getInstance(context).getCurrentUser().id){
					textView.setText(user.displayName);
					UrlImageViewHelper.setUrlDrawable(imageView, user.picture);
					break;
				}    			
			}
		} 

		return rowView;
	}
} 