package com.appstore.market.cp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

public class MyContentProvider extends ContentProvider {

	private static final String PROVIDER_NAME = "com.appstore.market.cp";

	public static final Uri CONTENT_URI = Uri.parse("content://"
			+ PROVIDER_NAME + "/urlpermission");

	public static final String _ID = "id";

	public static final String SERVICE_NAME = "service_name";

	public static final String URL = "url";
	
	public static final String RULE = "rule";
	
	private static SQLiteDatabase database;

	private static SQLiteDatabase historyDatabase;
	
	private static final String TABLE_URL_PERMISSIONS = "urlpermission";
	private static final String TABLE_HISTORY = "urlhistory";
	
	private static final int ITEMS = 1;

	private static final int ITEM = 2;

	private static UriMatcher uriMatcher;

	private static final int DATABASE_VERSION = 3;

	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(PROVIDER_NAME, TABLE_URL_PERMISSIONS, ITEMS);
		uriMatcher.addURI(PROVIDER_NAME, TABLE_URL_PERMISSIONS + "/#", ITEM);
	}


	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case ITEMS:
			return "vnd.android.cursor.dir/vnd.appstore.market.cp";
		case ITEM:
			return "vnd.android.cursor.item/vnd.appstore.market.cp";
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public boolean onCreate() {
		database = new MyDatabaseHelper(getContext(), TABLE_URL_PERMISSIONS, null,
				DATABASE_VERSION).getWritableDatabase();
		
		historyDatabase = new MyHistoryDatabaseHelper(getContext(), TABLE_HISTORY, null,
				DATABASE_VERSION).getWritableDatabase();
		
		return database != null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		
		Log.e("appstorequery", "uri is " + uri.toString());
		Log.e("appstorequery", "seletcion is " + selection);
		Log.e("appstorequery", "selectionArgs is " + selectionArgs[0]);
	    Log.e("appstorequery", "selectionArgs is " + selectionArgs[1]);
	    
	    selection = "url=? AND service_name=?";
	    selectionArgs = new String[] { "http://www.baidu.com/1", "service1"};
	    
		switch (uriMatcher.match(uri)) {
		case ITEMS:
			//Log.d("appstorequery", "uri is ITEMS ");
			return database.query(TABLE_URL_PERMISSIONS, projection, selection,
					selectionArgs, null, null, sortOrder);
		case ITEM:
			//Log.d("appstorequery", "uri is ITEM ");
			return database.query(TABLE_URL_PERMISSIONS, projection, _ID + "="
					+ uri.getPathSegments().get(1), selectionArgs, null, null,
					null);
		default:
			throw new IllegalArgumentException("unknown uri: " + uri);
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		Log.e("appstore insert", "uri is " + uri.toString());
		
    	String serviceURL = (String) values.get("serviceURL");
    	int eventType = (Integer) values.get("eventType");        	
    	String deviceUser = (String) values.get("deviceUser");
    	String operationResult = (String) values.get("operationResult");
    	String operationDesc = (String) values.get("operationDesc");    	
    	String createDate = (String) values.get("createDate"); 
    	

    	Log.e("appstore insert", "serviceURL is " + serviceURL);
    	Log.e("appstore insert", "eventType is " + eventType);
    	Log.e("appstore insert", "deviceUser is " + deviceUser);
    	Log.e("appstore insert", "operationResult is " + operationResult);
    	Log.e("appstore insert", "operationDesc is " + operationDesc);
    	Log.e("appstore insert", "createDate is " + createDate);
    	
    	historyDatabase.insert(TABLE_HISTORY, null, values);
    	
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}	

	private static class MyDatabaseHelper extends SQLiteOpenHelper {

		private Context context;

		public MyDatabaseHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
			this.context = context;
		}


		@Override
		public void onCreate(SQLiteDatabase database) {
			database.execSQL("create table if not exists urlpermission("
					+ " id integer primary key autoincrement," 
					+ " service_name text,"
					+ " url text,"
					+ " rule text);");
			
			SQLiteStatement statement = database
					.compileStatement("insert into urlpermission(service_name, url, rule) values(?,?,?)");
			int index = 1; 
			statement.bindString(index++, "service1");
			statement.bindString(index++, "http://www.baidu.com/1");
			statement.bindString(index++, "YES");
			
			statement.execute();

			index = 1;
			statement.bindString(index++, "service2");
			statement.bindString(index++, "http://www.baidu.com/2");
			statement.bindString(index++, "NO");
			statement.execute();

			statement.close();
		}

		@Override
		public void onUpgrade(SQLiteDatabase database, int oldVersion,
				int newVersion) {
			Log.w("mycp", "updating database from version " + oldVersion
					+ " to " + newVersion);
			database.execSQL("drop table if exists urlpermission");
			database.execSQL("drop table if exists urlhistory");
			onCreate(database);
		}

	}


	private static class MyHistoryDatabaseHelper extends SQLiteOpenHelper {

		private Context context;

		public MyHistoryDatabaseHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
			this.context = context;
		}


		@Override
		public void onCreate(SQLiteDatabase database) {

			database.execSQL("create table if not exists urlhistory("
					+ " id integer primary key autoincrement," 
					+ " serviceURL text,"
					+ " eventType integer,"
					+ " deviceUser text,"
					+ " operationResult text,"
					+ " operationDesc text,"
					+ " createDate text);");
			
		}

		@Override
		public void onUpgrade(SQLiteDatabase database, int oldVersion,
				int newVersion) {
			Log.w("mycp", "updating database from version " + oldVersion
					+ " to " + newVersion);
			database.execSQL("drop table if exists urlhistory");
			onCreate(database);
		}

	}
	


}




