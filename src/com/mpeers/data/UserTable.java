package com.mpeers.data;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mpeers.data.entities.User;

public class UserTable implements DatabaseTable{
	
	private DatabaseManager databaseManager;
	private static UserTable instance;
	
	public static final String TABLE_NAME = "users";
	public static final String ID = "id";
	public static final String DISPLAYNAME = "display_name";
	public static final String USERNAME = "user_name";
	public static final String ABOUT = "about";
	public static final String DESCRIPTION = "description";
	public static final String lOCATION = "location";
	public static final String WELCOME_MESSAGE = "welcome_message";
	public static final String EMAIL = "email";
	public static final String OFFLINE_MESSAGE = "offline_message";
	public static final String PHONE = "phone";
	public static final String PICTURE = "picture";
	public static final String PASSWORD = "password";
	public static final String LOGGEDIN = "logged_in";
	
	public static UserTable getInstance(Context context) {
		if(instance == null){
			instance = new UserTable(DatabaseManager.getInstance(context));
			DatabaseManager.getInstance(context).addTable(instance);
		}
		return instance;
	}

	private UserTable(DatabaseManager databaseManager) {
		this.databaseManager = databaseManager;
	} 
	
	@Override
	public void create(SQLiteDatabase db) {
		String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + ID
				+ " TINYTEXT PRIMARY KEY," + USERNAME + " TINYTEXT,"
				+ DISPLAYNAME + " TINYTEXT," + ABOUT + " TEXT,"
				+ DESCRIPTION + " TEXT," + lOCATION + " TEXT," 
				+ PICTURE + " TEXT," + WELCOME_MESSAGE + " TEXT," 
				+ EMAIL + " TEXT," + OFFLINE_MESSAGE + " TEXT," 
				+ PASSWORD + " TEXT," + LOGGEDIN + " INTEGER," + PHONE + " TEXT);";
		DatabaseManager.execSQL(db, sql);		
	}

	@Override
	public void migrate(SQLiteDatabase db, int toVersion) {
		
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		SQLiteDatabase db = databaseManager.getWritableDatabase();
		DatabaseManager.dropTable(db, TABLE_NAME);
		create(db);		
	}
	
	public ArrayList<User> getAllUsers(){
		ArrayList<User> users = new ArrayList<User>();
		Cursor cursor = databaseManager.getReadableDatabase().query(TABLE_NAME, null, null, null, null,
				null, null);
		try {
			if (cursor.moveToFirst()) {
				do {
					User user = fillUserDetails(cursor);
					users.add(user);
				} while (cursor.moveToNext());
			}
		} finally {
			cursor.close();
		}
		return users;
	}
	
	public boolean saveUser(User user){
		if(user.id == null)
			return false;
		ContentValues values = new ContentValues();
		values.put(DESCRIPTION, user.description);
		values.put(USERNAME, user.userName);
		values.put(EMAIL, user.email);
		values.put(DISPLAYNAME, user.displayName);
		values.put(PICTURE, user.picture);
		values.put(lOCATION, user.location);
		values.put(WELCOME_MESSAGE, user.welcomeMessage);
		values.put(OFFLINE_MESSAGE, user.offlineMessage);
		values.put(PHONE, user.phone);
		values.put(ABOUT, user.about);		
		values.put(ID, user.id);
		values.put(PASSWORD, user.password);
		if(user.loggedIn)
			values.put(LOGGEDIN, 1);
		else
			values.put(LOGGEDIN, 0);
		long res = databaseManager.getWritableDatabase().insertWithOnConflict(TABLE_NAME, ID, values, 4);
		if(res == -1)
			return false;		
		return true;
	}
	
	public User getLoggedInUser(){
		Cursor cursor = databaseManager.getReadableDatabase().query(TABLE_NAME, null, LOGGEDIN + "=1", null, null,
				null, null);
		if(cursor.moveToFirst()){
			User user = fillUserDetails(cursor);
			return user;
		}
		return null;
	}
	
	public static User fillUserDetails(Cursor cursor){
		User user = new User(
				cursor.getString(cursor.getColumnIndex(ID)),
				cursor.getString(cursor.getColumnIndex(USERNAME)),
				cursor.getString(cursor.getColumnIndex(DISPLAYNAME)),
				cursor.getString(cursor.getColumnIndex(EMAIL)),
				cursor.getString(cursor.getColumnIndex(DESCRIPTION)),
				cursor.getString(cursor.getColumnIndex(ABOUT)),
				cursor.getString(cursor.getColumnIndex(lOCATION)),
				cursor.getString(cursor.getColumnIndex(WELCOME_MESSAGE)),
				cursor.getString(cursor.getColumnIndex(OFFLINE_MESSAGE)),
				cursor.getString(cursor.getColumnIndex(PHONE)),
				cursor.getString(cursor.getColumnIndex(PICTURE)),
				cursor.getString(cursor.getColumnIndex(PASSWORD)),
				cursor.getInt(cursor.getColumnIndex(LOGGEDIN)) == 1
			);
		return user;
	}

}
