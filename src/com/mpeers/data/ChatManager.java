package com.mpeers.data;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.mpeers.ChatListChangeListner;
import com.mpeers.onUserLoginListner;
import com.mpeers.connection.NewChatMessageListner;
import com.mpeers.connection.XmppConnection;
import com.mpeers.data.entities.Chat;
import com.mpeers.data.entities.ChatMessage;
import com.mpeers.data.entities.User;
import com.mpeers.utils.RandomString;
import com.mpeers.utils.RestClient;
import com.mpeers.utils.RestResponse;
import com.mpeers.utils.RestClient.RequestMethod;

public class ChatManager implements onUserLoginListner, NewChatMessageListner{
	private ArrayList<Chat> currentChats;
	private RestClient restClient;
	private Context context;
	private ChatTable chatTable;
	private ArrayList<ChatListChangeListner> chatListChangeListners;
	
	private static ChatManager instance;
	
	public static ChatManager getInstance(Context context){
		if(instance == null){
			instance = new ChatManager(context.getApplicationContext());
			UserManager.getInstance(context).addLoginListner(instance);
		}
		return instance;
	}
	
	private ChatManager(Context context){
		this.context = context;
		currentChats = new ArrayList<Chat>();
		restClient = RestClient.getInstance();
		chatTable = ChatTable.getInstance(context);
		chatListChangeListners = new ArrayList<ChatListChangeListner>();	
	}
	
	public ArrayList<Chat> getAllChatsFromDb(){
		currentChats = chatTable.getAllChats();
		/*String nickname = UserManager.getInstance(context).getCurrentUser().userName;
		for(Chat chat : currentChats){			
			XmppConnection.getInstance(context).joinRoom(chat.room, nickname);
		}*/
		dispatchChatListChange();
		return currentChats;
	}
	
	public void addChatToList(Chat chat){		
		if(chatTable.saveChat(chat)){
			currentChats.add(chat);
			String nickname = UserManager.getInstance(context).getCurrentUser().userName;
			nickname = nickname + "###" + (new RandomString(10).nextString());
			XmppConnection.getInstance(context).joinRoom(chat.room, nickname);			
		}
	}
	
	public void addChatListChangeListner(ChatListChangeListner listner){
		chatListChangeListners.add(listner);
	}
	
	public void dispatchChatListChange(){
		for(ChatListChangeListner listner : chatListChangeListners)
			listner.onChatListChangeListner();
	}
	
	public ArrayList<Chat> refreshChatList(){
		User currentUser = UserManager.getInstance(context).getCurrentUser();
		if(currentUser != null){			
	    	RestResponse res = restClient.execute("/users/" + currentUser.id + "/chats", 
	    			RequestMethod.GET, null, null);   	
	    	 
	    	if(res.responseCode == 200){
	    		chatTable.clear();
	    		currentChats.clear();
	    		try{
	    			JSONArray jArray = new JSONArray(res.response);
	    			for(int i=0; i < jArray.length(); i++){
	    				Chat chat = new Chat(jArray.getJSONObject(i));
	    				addChatToList(chat);
	    			}
	    		} catch (Exception e){	    			
	    		}
	    	}
		}
		dispatchChatListChange();
		return currentChats;
	}
	
	public Chat getChatById(String id){
		for(Chat chat : currentChats){
			if(chat.id.equals(id))
				return chat;
		}
		return null;
	}
	
	public ArrayList<Chat> getCurrentChats(){
		return currentChats;
	}

	@Override
	public void onUserLogin(User user) {		
		refreshChatList();
	}

	@Override
	public void onNewChatMessage(ChatMessage newMsg) {
		// TODO Auto-generated method stub
		
	}
}
