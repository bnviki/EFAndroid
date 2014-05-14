package com.mpeers.service;

import com.mpeers.Login;
import com.mpeers.data.UserManager;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

public class UserAuthService extends IntentService{
	public static String TYPE = "type"; 
	public static enum ActionType {
		LOGIN, LOGOUT, CHECK_USER
	}
	public static String LOGIN = "login"; 
	public static String LOGOUT = "logout"; 
	
	private UserManager userManager;

	public UserAuthService(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate() {
		super.onCreate();
		userManager = UserManager.getInstance(getApplicationContext());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		Bundle values = intent.getExtras();
		ActionType type = (ActionType) values.get(TYPE);
		switch(type){
		case LOGIN:
		case LOGOUT: userManager.logoutUser();
		startLoginActivity();
		break;
		case CHECK_USER: 
		}
	}
	
	public void startLoginActivity(){
		Intent upanel = new Intent(getApplicationContext(), Login.class);
		upanel.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(upanel);
	}

}
