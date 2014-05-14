package com.mpeers.connection;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

import java.net.CookieManager;
import java.net.MalformedURLException;

import javax.net.ssl.SSLContext;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.mpeers.onUserLoginListner;
import com.mpeers.data.ChatManager;
import com.mpeers.data.UserManager;
import com.mpeers.data.entities.Chat;
import com.mpeers.data.entities.User;
import com.mpeers.utils.RestClient;

public class SocketConnection{
	private static SocketConnection instance;
	private Context context;	
	private SocketIO connection;

	public static SocketConnection getInstance(Context context){
		if(instance == null){
			instance = new SocketConnection(context.getApplicationContext());
		}
		return instance;
	}

	private SocketConnection(Context context){
		this.context = context;				
	}

	public void resetConnection(){
		if(connection != null){
			connection.disconnect();
			connection = null;
		}		
	}

	public SocketIO getConnection(){
		return connection;
	}

	//this function must be called only after login as this requires session id.
	public void connect(String host){
		resetConnection();		
		connection = new SocketIO();
		String cookie = RestClient.getInstance().getCookie("connect.sid");
		cookie = "connect.sid=" + cookie;
		connection.addHeader("Cookie", cookie);

		try {
			connection.connect(host, new IOCallback() {
				@Override
				public void onMessage(JSONObject json, IOAcknowledge ack) {
					try {
						System.out.println("Server said:" + json.toString(2));
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}

				@Override
				public void onMessage(String data, IOAcknowledge ack) {
					System.out.println("Server said: " + data);
				}

				@Override
				public void onError(SocketIOException socketIOException) {
					System.out.println("an Error occured");
					socketIOException.printStackTrace();
				}

				@Override
				public void onDisconnect() {
					System.out.println("Connection terminated.");
				}

				@Override
				public void onConnect() {
					System.out.println("Connection established ... registering");
					User current = UserManager.getInstance(context).getCurrentUser();
					JSONObject obj = new JSONObject();
					try {
						obj.put("name", "register");
						obj.put("username", current.userName);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}		        
					connection.emit("register", obj);
				}

				@Override
				public void on(String event, IOAcknowledge ack, Object... args) {
					System.out.println("Server triggered event '" + event + "'");
					if(event.equals("NEW_CHAT")){
						final JSONObject jsonChat = (JSONObject) args[0];
						Chat newChat = new Chat(jsonChat);
						ChatManager.getInstance(context).addChatToList(newChat);
						ChatManager.getInstance(context).dispatchChatListChange();
					}
				}
			});
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
