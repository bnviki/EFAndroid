package com.mpeers;

import com.mpeers.data.ChatManager;
import com.mpeers.data.ChatMessageManager;
import com.mpeers.data.UserManager;
import com.mpeers.service.ConnectionManager;

import android.app.Application;
import android.content.Intent;

public class MpeersApp extends Application {
	private static MpeersApp instance;
	
	public static MpeersApp getInstance() {
		return instance;
	}
	
	private UserManager userManager;
	private ChatManager chatManager;
	private ChatMessageManager messageManager;
	
	@Override
	public final void onCreate() {
		super.onCreate();
		instance = this;
		
		userManager = UserManager.getInstance(this);
		chatManager = ChatManager.getInstance(this);
		messageManager = ChatMessageManager.getInstance(this);
		

		Intent intent = new Intent(this, ConnectionManager.class);	
		intent.putExtra("type", "CheckUser");
		startService(intent);
	}
}
