package com.mpeers.data.entities;

import org.json.JSONException;
import org.json.JSONObject;

public class User {
	public String displayName;
	public String userName;
	public String about;
	public String description;
	public String location;
	public String welcomeMessage;
	public String email;
	public String offlineMessage;
	public String phone;
	public String picture;
	public String id;
	public String password;
	public boolean loggedIn = false;
	
	public User(String id, String username, String displayname, String email, String description, String about, String location, 
			String welcomeMsg, String offlineMsg, String phone, String picture, String password, boolean loggedIn){
		this.displayName = displayname;
		this.userName = username;
		this.about = about;
		this.description = description;
		this.location = location;
		this.welcomeMessage = welcomeMsg;
		this.email = email;
		this.offlineMessage = offlineMsg;
		this.phone = phone;
		this.id = id;
		this.picture = picture;	
		this.password = password;
		if(password == null)
			this.password = "";
		this.loggedIn = loggedIn;
	}
	
	public User(String jsonValue){
		try {
			JSONObject jObject = new JSONObject(jsonValue);
			userName = getField("username", jObject);
			id = getField("_id", jObject);
			email = getField("email", jObject);
			displayName = getField("displayname", jObject);
			about = getField("about", jObject);
			description = getField("description", jObject);
			location = getField("location", jObject);
			welcomeMessage = getField("welcome_message", jObject);
			offlineMessage = getField("offline_message", jObject);
			phone = getField("phone", jObject);
			picture = getField("picture", jObject);						
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private String getField(String name, JSONObject obj){
		try{
			return obj.getString(name);			
		} catch (Exception e){
			return "";
		}
	}
	
	public User(JSONObject jObject){
		try{
			userName = getField("username", jObject);
			id = getField("_id", jObject);
			email = getField("email", jObject);
			displayName = getField("displayname", jObject);
			about = getField("about", jObject);
			description = getField("description", jObject);
			location = getField("location", jObject);
			welcomeMessage = getField("welcome_message", jObject);
			offlineMessage = getField("offline_message", jObject);
			phone = getField("phone", jObject);
			picture = getField("picture", jObject);			
		} catch (Exception e) {		
			e.printStackTrace();
		}
	}
	
	public String toJSON(){
		JSONObject jObj = new JSONObject();
		
		try {
			jObj.put("_id", id);
			jObj.put("username", userName);
			jObj.put("displayname", displayName);
			jObj.put("email", email);
			jObj.put("about", about);
			jObj.put("description", description);
			jObj.put("location", location);
			jObj.put("welcome_message", welcomeMessage);
			jObj.put("offline_message", offlineMessage);
			jObj.put("phone", phone);
			jObj.put("picture", picture);
			return jObj.toString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}
}
