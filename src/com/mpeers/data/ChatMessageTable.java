package com.mpeers.data;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mpeers.data.entities.ChatMessage;
import com.mpeers.data.entities.User;

public class ChatMessageTable implements DatabaseTable{

	private DatabaseManager databaseManager;
	private static ChatMessageTable instance;
	private Context context;

	public static final String TABLE_NAME = "chat_messages";
	public static final String MESSAGE = "msg";
	public static final String ROOM = "room";
	public static final String FROM = "from_user";

	public static ChatMessageTable getInstance(Context context) {
		if(instance == null){
			instance = new ChatMessageTable(DatabaseManager.getInstance(context));
			DatabaseManager.getInstance(context).addTable(instance);
		}
		instance.context = context;				
		return instance;
	}

	private ChatMessageTable(DatabaseManager databaseManager) {
		this.databaseManager = databaseManager;
	}

	@Override
	public void create(SQLiteDatabase db) {
		String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + ROOM
				+ " TINYTEXT NOT NULL, " + FROM + " TINYTEXT,"				
				+ MESSAGE + " TEXT);" ;
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
		System.out.println("****clearing table: " + TABLE_NAME);
		create(db);		
	}

	public ArrayList<ChatMessage> getRoomMessages(String room){
		ArrayList<ChatMessage> msgs = new ArrayList<ChatMessage>();
		String[] args = {room.toLowerCase()};
		Cursor cursor = databaseManager.getReadableDatabase().query(TABLE_NAME, null, ROOM + " = ?", args, null, null, null);
		try {
			if (cursor.moveToFirst()) {
				do {
					ChatMessage msg = new ChatMessage(cursor.getString(cursor.getColumnIndex(ROOM)),
							cursor.getString(cursor.getColumnIndex(FROM)),
							cursor.getString(cursor.getColumnIndex(MESSAGE)));
					msgs.add(msg);
				} while (cursor.moveToNext());				
			}
		} finally {
			cursor.close();			
		}
		return msgs;
	}
	
	public boolean saveMessage(ChatMessage msg){
		ContentValues values = new ContentValues();
		values.put(ROOM, msg.room);
		values.put(MESSAGE, msg.message);
		values.put(FROM, msg.from);
		
		long res = databaseManager.getWritableDatabase().insert(TABLE_NAME, null, values);
		if(res == -1)
			return false;
		System.out.println("writing messge: " + msg.message + " res: " + res);
		return true;
	}

}
