package com.mpeers.connection;

import java.util.ArrayList;
import java.util.HashMap;

import org.jivesoftware.smack.AndroidConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Body;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.muc.MultiUserChat;

import android.content.Context;

import com.mpeers.data.entities.ChatMessage;

public class XmppConnection {
	private static XmppConnection instance;
	private Context context;	
	private XMPPConnection connection;
	private ArrayList<NewChatMessageListner> newChatMsgListners;
	private HashMap<String, MultiUserChat> joinedRooms;
	
	public static final String HOST = "54.254.216.45";
	public static final int PORT = 5222;
	public static final String SERVICE = "";
	public static final String HOSTNAME = "ip-172-31-9-1";

	public static XmppConnection getInstance(Context context){
		if(instance == null){
			instance = new XmppConnection(context.getApplicationContext());
		}
		return instance;
	}

	private XmppConnection(Context context){
		this.context = context.getApplicationContext();				
		SmackAndroid.init(this.context);
		newChatMsgListners = new ArrayList<NewChatMessageListner>();
		joinedRooms = new HashMap<String, MultiUserChat>();
	}
	
	public void addNewChatMsgListner(NewChatMessageListner listner){
		newChatMsgListners.add(listner);
	}
	
	public void dispatchNewChatMsg(ChatMessage newMsg){
		for(NewChatMessageListner listner : newChatMsgListners){
			listner.onNewChatMessage(newMsg);
		}
	}
	
	public XMPPConnection getConnection(){
		return connection;
	}
	
	public void connect(String username, String password){
		try {
			AndroidConnectionConfiguration conf = new AndroidConnectionConfiguration(HOST, PORT, SERVICE);
			connection = new XMPPConnection(conf);
			
			connection.connect();
			System.out.println("connected to XMPP server .. logging in");
			
			connection.login(username, password);
			
			Presence presence = new Presence(Presence.Type.available);
			presence.setStatus("lets talk");
			connection.sendPacket(presence);
			System.out.println("xmpp online .. ");
			
			PacketFilter filter = new MessageTypeFilter(Message.Type.groupchat);
			connection.addPacketListener(new PacketListener() {
			  public void processPacket(Packet packet) {
			    Message message = (Message) packet;
			    String body = message.getBody();
			    String from = message.getFrom();
			    String room = from.split("@")[0];
			    if(from.contains("/")){
			    	if(from.indexOf("###") != -1)
			    		from = from.substring(from.indexOf("/") + 1, from.indexOf("###"));
			    	else
			    		from = "anonymous";
			    } else {
			    	from = "system";
			    }		    	
			    
			    if(from != null && !from.equals("")){			    	
			    	//System.out.println("message received from  " + room + " :" + body);
			    	ChatMessage msg = new ChatMessage(room, from, body);
			    	dispatchNewChatMsg(msg);
			    }
			  }
			}, filter);
			
		} catch (XMPPException e) {
			System.out.println("failed in connecting to XMPP server");
			e.printStackTrace();
		}
	}
	
	public void disconnect(){
		if(connection != null && connection.isConnected())
			connection.disconnect();
	}
	
	public void sendMessage(String toRoomName, String msg){
		String roomName = toRoomName + "@conference." + HOSTNAME;		
		Message message = new Message(roomName, Message.Type.groupchat);
		message.setBody(msg);
		try {
			joinedRooms.get(toRoomName).sendMessage(msg);
		} catch (XMPPException e) {			
			e.printStackTrace();
		}
		//connection.sendPacket(message);
	}
	
	public void joinRoom(String room, String nickname){
		String roomAddr = room + "@conference." + HOSTNAME; 
		MultiUserChat muc = new MultiUserChat(connection, roomAddr);
		try {		
			muc.join(nickname);
			joinedRooms.put(room, muc);
		} catch (XMPPException e) {
			System.out.println("could not join room : " + room);
			e.printStackTrace();			
		}
	}
}
