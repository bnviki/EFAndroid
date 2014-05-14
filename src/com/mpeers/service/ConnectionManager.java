package com.mpeers.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import com.mpeers.data.ChatManager;
import com.mpeers.data.UserManager;
import com.mpeers.data.UserManager.UserState;
import com.mpeers.data.entities.User;

public class ConnectionManager extends Service {

	private UserManager userManager;
	private ChatManager chatManager;
	private final IBinder mBinder = new ConnectionBinder();
	private Thread userConnector;
	private boolean reconnectRequest;

	public User getCurrentUser(){
		if(userManager != null)
			return userManager.getCurrentUser();
		return null;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}

	public class ConnectionBinder extends Binder {
		public ConnectionManager getService() {
			// Return this instance of LocalService so clients can call public methods
			return ConnectionManager.this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		reconnectRequest = false;
		registerReceiver(new BroadcastReceiver() {      
	        public void onReceive(Context context, Intent intent) {
	        	ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);	        	 
	        	NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
	        	boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
	        	if(isConnected){
	        		System.out.println("network connected");
	        		if(reconnectRequest)
	        			checkUserAndLogin();
	        	}
	        	else
	        		System.out.println("network connection lost");
	        }}, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
		
		userManager = UserManager.getInstance(getApplicationContext()); 
		chatManager = ChatManager.getInstance(getApplicationContext());

		//checkUserAndLogin();
	}

	public void checkUserAndLogin(){
		userConnector = new Thread(new Runnable() {
			@Override
			public void run() {
				ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);	        	 
	        	NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
	        	boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
	        	if(!isConnected){
	        		reconnectRequest = true;
	        		return;
	        	}
				if(userManager.checkUser() == UserState.NOT_LOGGED_IN){
					userManager.loginCurrentUserFromDB();
					System.out.println("usermanager: logged in user");	             	
				}
			}
		});
		userConnector.start();
	}

	public void loginUser(final String username, final String password){		
		userConnector = new Thread(new Runnable() {
			@Override
			public void run() {
				userManager.loginUser(username, password);
			}
		});

		userConnector.start();
	}

	public void logoutUser(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				userManager.logoutUser();			
			}
		}).start();		
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent != null){
			Bundle values = intent.getExtras();

			if(values != null && values.containsKey("type")){
				if(values.getString("type").equals("UserLogin") && (userConnector == null || !userConnector.isAlive())){
					loginUser(values.getString("username"), values.getString("password"));
				}			
				else if(values.getString("type").equals("CheckUser") && (userConnector == null || !userConnector.isAlive())){
					checkUserAndLogin();
				}
			}
		} else {
			if(userConnector == null || !userConnector.isAlive())
				checkUserAndLogin();
		}
		
		Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
		return START_STICKY;
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();	
	}
}
