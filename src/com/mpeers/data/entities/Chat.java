package com.mpeers.data.entities;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class Chat {
	public String id;
	public ArrayList<User> users;
	public String room;
	public boolean anonymousChat;
	public AnonymousUser anonymousUser;
	
	public static class AnonymousUser {
		public String jid;
		public String name;
		
		public AnonymousUser(String jid, String name) {
			this.jid = jid;
			this.name = name;
		}
	}
	
	public Chat(String id, String room, boolean anonChat, AnonymousUser anon_user, ArrayList<User> users){		
		this.id = id;
		this.users = users;
		if(users == null)
			this.users = new ArrayList<User>();
		this.anonymousChat = anonChat;
		this.room = room;
		this.anonymousUser = anon_user;
	}
	
	public Chat(String jsonValue){
		try {
			JSONObject jObject = new JSONObject(jsonValue);
			this.id = jObject.getString("_id");
			this.room = jObject.getString("room");
			this.anonymousChat = jObject.getBoolean("anonymous_chat");
			try{
				JSONObject anonUserObj = jObject.getJSONObject("anonymous_user");
				this.anonymousUser = new AnonymousUser(anonUserObj.getString("jid"), anonUserObj.getString("name"));
			} catch(Exception e){
				this.anonymousUser = null;
			}
			
			users = new ArrayList<User>();
			JSONArray usersObj = jObject.getJSONArray("users");			
			for(int i=0; i < usersObj.length(); i++){
				JSONObject userObj = usersObj.getJSONObject(i);
				User user = new User(userObj);
				users.add(user);
			}			
		} catch (Exception e) {			
			e.printStackTrace();
		}
	}
	
	public Chat(JSONObject jObject){
		try {			
			this.id = jObject.getString("_id");
			this.room = jObject.getString("room");
			this.anonymousChat = jObject.getBoolean("anonymous_chat");
			try{
				JSONObject anonUserObj = jObject.getJSONObject("anonymous_user");
				this.anonymousUser = new AnonymousUser(anonUserObj.getString("jid"), anonUserObj.getString("name"));
			} catch(Exception e){
				this.anonymousUser = null;
			}
			
			users = new ArrayList<User>();
			JSONArray usersObj = jObject.getJSONArray("users");			
			for(int i=0; i < usersObj.length(); i++){
				JSONObject userObj = usersObj.getJSONObject(i);
				User user = new User(userObj);
				users.add(user);
			}			
		} catch (Exception e) {			
			e.printStackTrace();
		}
	}
}
