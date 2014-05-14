package com.mpeers.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseManager extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "mpeers_data";
	private static final int DATABASE_VERSION = 1;

	private final ArrayList<DatabaseTable> registeredTables;

	private static DatabaseManager instance;		

	public static DatabaseManager getInstance(Context context) {
		if(instance == null)
			instance = new DatabaseManager(context.getApplicationContext());
		return instance;
	}

	private DatabaseManager(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		registeredTables = new ArrayList<DatabaseTable>();
	}

	public void addTable(DatabaseTable table) {
		registeredTables.add(table);
		table.create(getWritableDatabase());
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.enableWriteAheadLogging();
		for (DatabaseTable table : registeredTables)
			table.create(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		clear();
		for (DatabaseTable table : registeredTables)
			table.create(db);
	}	

	public void clear() {
		for (DatabaseTable table : registeredTables)
			table.clear();
	}

	public static void execSQL(SQLiteDatabase db, String sql) {		
		db.execSQL(sql);
	}

	public static void dropTable(SQLiteDatabase db, String table) {
		execSQL(db, "DROP TABLE IF EXISTS " + table + ";");
	}

	public static void renameTable(SQLiteDatabase db, String table,
			String newTable) {
		execSQL(db, "ALTER TABLE " + table + " RENAME TO " + newTable + ";");
	}

	public static String commaSeparatedFromCollection(Collection<String> strings) {
		StringBuilder builder = new StringBuilder();
		for (String value : strings) {
			if (builder.length() > 0)
				builder.append(",");
			builder.append(value.replace("\\", "\\\\").replace(",", "\\,"));
		}
		return builder.toString();
	}

	public static Collection<String> collectionFromCommaSeparated(String value) {
		Collection<String> collection = new ArrayList<String>();
		boolean escape = false;
		StringBuilder builder = new StringBuilder();
		for (int index = 0; index < value.length(); index++) {
			char chr = value.charAt(index);
			if (!escape) {
				if (chr == '\\') {
					escape = true;
					continue;
				} else if (chr == ',') {
					collection.add(builder.toString());
					builder = new StringBuilder();
					continue;
				}
			}
			escape = false;
			builder.append(chr);
		}
		collection.add(builder.toString());
		return Collections.unmodifiableCollection(collection);
	}

}
