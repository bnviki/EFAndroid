package com.mpeers.data;

import java.util.ArrayList;

import com.mpeers.data.entities.Chat;
import com.mpeers.data.entities.Chat.AnonymousUser;
import com.mpeers.data.entities.User;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ChatTable implements DatabaseTable{
	
	private DatabaseManager databaseManager;
	private static ChatTable instance;
	private static Context context;
	
	public static ChatTable getInstance(Context context) {
		if(instance == null){
			instance = new ChatTable(DatabaseManager.getInstance(context));
			DatabaseManager.getInstance(context).addTable(instance);
		}
		ChatTable.context = context;				
		return instance;
	}

	private ChatTable(DatabaseManager databaseManager) {
		this.databaseManager = databaseManager;
	}
	
	public static final String TABLE_NAME = "chats";
	public static final String ID = "id";
	public static final String ROOM = "room";
	public static final String ANONYMOUS_CHAT = "anonymous_chat";
	public static final String ANONYMOUS_USER_NAME = "anonymous_user_name";
	public static final String ANONYMOUS_USER_JID = "anonymous_user_jid";
	

	@Override
	public void create(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + ID
				+ " TINYTEXT PRIMARY KEY," + ROOM + " TINYTEXT,"
				+ ANONYMOUS_CHAT + " INTEGER," + ANONYMOUS_USER_NAME + " TINYTEXT,"
				+ ANONYMOUS_USER_JID + " TINYTEXT);" ;
		DatabaseManager.execSQL(db, sql);
	}

	@Override
	public void migrate(SQLiteDatabase db, int toVersion) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clear() {
		SQLiteDatabase db = databaseManager.getWritableDatabase();
		DatabaseManager.dropTable(db, TABLE_NAME);
		create(db);		
		
		UserChatTable.getInstance(context).clear();
	}
	
	public boolean saveChat(Chat chat){
		if(chat.id == null)
			return false;
		for(User user : chat.users){
			UserTable.getInstance(ChatTable.context).saveUser(user);
		}
		
		ContentValues values = new ContentValues();
		values.put(ID, chat.id);
		if(chat.anonymousChat){
			values.put(ANONYMOUS_CHAT, 1);
			values.put(ANONYMOUS_USER_JID, chat.anonymousUser.jid);
			values.put(ANONYMOUS_USER_NAME, chat.anonymousUser.name);
		} else {
			values.put(ANONYMOUS_CHAT, 0);
			values.put(ANONYMOUS_USER_JID, "");
			values.put(ANONYMOUS_USER_NAME, "");
		}
		values.put(ROOM, chat.room);
		
		long res = databaseManager.getWritableDatabase().insert(TABLE_NAME, ID, values);
		if(res == -1)
			return false;	
		
		for(User user : chat.users){
			if(!UserChatTable.getInstance(ChatTable.context).saveChatUsers(user, chat))
				return false;
		}
		return true;	
	}
	
	public ArrayList<Chat> getAllChats(){
		ArrayList<Chat> chats = new ArrayList<Chat>();
		Cursor cursor = databaseManager.getReadableDatabase().query(TABLE_NAME, null, null, null, null,
				null, null);
		try {
			if (cursor.moveToFirst()) {
				do {
					Chat chat = fillChatDetails(cursor);
					chats.add(chat);
				} while (cursor.moveToNext());
			}
		} finally {
			cursor.close();
		}
		return chats;
	}
	
	public Chat fillChatDetails(Cursor cursor){
		Chat chat = new Chat(
				cursor.getString(cursor.getColumnIndex(ID)),
				cursor.getString(cursor.getColumnIndex(ROOM)),
				cursor.getInt(cursor.getColumnIndex(ANONYMOUS_CHAT)) == 1,
				new AnonymousUser(cursor.getString(cursor.getColumnIndex(ANONYMOUS_USER_JID)), 
						cursor.getString(cursor.getColumnIndex(ANONYMOUS_USER_NAME))), 
				null				
			);
		chat.users = UserChatTable.getInstance(context).getChatUsers(chat.id);
		return chat;		
	}	

}
