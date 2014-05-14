package com.mpeers.data;

import java.util.ArrayList;

import com.mpeers.data.entities.Chat;
import com.mpeers.data.entities.User;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class UserChatTable implements DatabaseTable{
	private DatabaseManager databaseManager;
	private static UserChatTable instance;
	
	public static UserChatTable getInstance(Context context) {
		if(instance == null){
			instance = new UserChatTable(DatabaseManager.getInstance(context));
			DatabaseManager.getInstance(context).addTable(instance);
		}
		return instance;
	}

	private UserChatTable(DatabaseManager databaseManager) {
		this.databaseManager = databaseManager;
	}
	
	public static final String TABLE_NAME = "user_chat";
	public static final String USERID = "user_id";
	public static final String CHATID = "chat_id";

	@Override
	public void create(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + USERID
				+ " TINYTEXT," + CHATID + " TINYTEXT,"
				+ " PRIMARY KEY (" + USERID + "," + CHATID +  ")," 
				+ " FOREIGN KEY (" + USERID + ") REFERENCES " + UserTable.TABLE_NAME + "(" + UserTable.ID + "),"
				+ " FOREIGN KEY (" + CHATID + ") REFERENCES " + ChatTable.TABLE_NAME + "(" + ChatTable.ID + "));";
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
	}
	
	public ArrayList<User> getChatUsers(String id){		
		ArrayList<User> res = new ArrayList<User>();
		if(id == null || id == "")
			return res;
		
		String query = "select * from user_chat, users where user_chat.chat_id = ? and users.id = user_chat.user_id";
		String[] args = {id};
		Cursor cursor = databaseManager.getReadableDatabase().rawQuery(query, args);	
		try {
			if (cursor.moveToFirst()) {
				do {
					User user = UserTable.fillUserDetails(cursor);
					res.add(user);
				} while (cursor.moveToNext());
			}
		} finally {
			cursor.close();
		}
		return res;
	}
	
	public boolean saveChatUsers(User user, Chat chat){
		if(chat.id == null || user.id == null)
			return false;
		
		ContentValues values = new ContentValues();
		values.put(CHATID, chat.id);
		values.put(USERID, user.id);
		
		long res = databaseManager.getWritableDatabase().insert(TABLE_NAME, CHATID + ", " + USERID, values);
		if(res == -1)
			return false;		
		return true;	
	}
}
