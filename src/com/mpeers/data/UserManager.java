package com.mpeers.data;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;

import com.mpeers.onUserLoginListner;
import com.mpeers.connection.SocketConnection;
import com.mpeers.connection.XmppConnection;
import com.mpeers.data.entities.User;
import com.mpeers.utils.RestClient;
import com.mpeers.utils.RestClient.RequestMethod;
import com.mpeers.utils.RestResponse;

public class UserManager {
	
	public static enum UserState {
		LOGGED_IN,
		NOT_LOGGED_IN, 
		NO_ACCOUNT
	}
	
	private static UserManager instance;
	
	private Context context;
	private User currentUser;
	private UserTable userTable;
	private RestClient restClient;	
	private ArrayList<onUserLoginListner> loginListners; 
	
	public static UserManager getInstance(Context context) {
		if(instance == null)
			instance = new UserManager(context.getApplicationContext());
		return instance;
	}
	
	private UserManager(Context context){
		this.context = context;
		userTable = UserTable.getInstance(context);
		restClient = RestClient.getInstance();		
		loginListners = new ArrayList<onUserLoginListner>();
	}
	
	public void addLoginListner(onUserLoginListner loginListner){
		loginListners.add(loginListner);
	}
	
	public void dispatchOnUserLogin(User user){
		for(onUserLoginListner listner : loginListners){
			listner.onUserLogin(user);
		}
	}
	
	public UserState checkUser(){	
		if(currentUser != null)
			return UserState.LOGGED_IN;
		User userToLogin = userTable.getLoggedInUser();
		if(userToLogin == null)
			return UserState.NO_ACCOUNT;	
		return UserState.NOT_LOGGED_IN;
	}
	
	public User loginCurrentUserFromDB(){
		User user = userTable.getLoggedInUser(); 
		if(loginUser(user.userName, user.password) != null){
			return user;
		}
		return null;
	}
	
	public User loginUser(String username, String password){
		if(currentUser != null)
			logoutUser();
		
		username = username.trim();
		password = password.trim();	
		
		HashMap<String, String> params = new HashMap<String, String>();		
		params.put("username", username);
		params.put("password", password);
    	RestResponse res = restClient.execute("/login", RequestMethod.POST, params, null);    	
    	 
    	if(res.responseCode == 200){    		    	
    		User user = new User(res.response);
    		user.password = password;
    		user.loggedIn = true;
    		userTable.clear();
    		if(userTable.saveUser(user)){
    			currentUser = user;
        		System.out.println("logged in user : " + user.userName);       		       		
        		SocketConnection.getInstance(context).connect(restClient.getHost());
        		XmppConnection.getInstance(context).connect(username, username);
        		XmppConnection.getInstance(context).addNewChatMsgListner(ChatManager.getInstance(context));
        		dispatchOnUserLogin(user); 
        		return user;
    		}    		
    	}
    	
		return null;
	}
	
	public boolean logoutUser(){		
		RestResponse res = restClient.execute("/logout", RequestMethod.GET, null, null);
		if(res.responseCode == 200){
			currentUser = null;
			userTable.clear();
			restClient.resetConnection();
			SocketConnection.getInstance(context).resetConnection();
			return true;
		}
		return false;
	}
	
	public User getCurrentUser(){
		return currentUser;
	}

}
