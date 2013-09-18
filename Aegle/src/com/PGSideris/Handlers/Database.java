package com.PGSideris.Handlers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database  extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DB_name = "aeglea";
    private static final String TABLE_NAME = "user_login";
    private static final String KEY_UID = "uid";
    private static final String KEY_NAME = "name";
    private static final String KEY_LAST = "last";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_USERNAME = "username";
    
	public Database(Context context) {
		//setting the application context for a higher level call
		super(context, DB_name, null, DATABASE_VERSION);
	}
	
	//SLQLiteOpenHelper(it helps with automatic management) methods follow. 
	public void onCreate(SQLiteDatabase db) {
		//creating tables
		String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_NAME + "(" + KEY_UID + " INTEGER PRIMARY KEY,"+ KEY_NAME + " TEXT," + KEY_LAST + " TEXT,"+ KEY_EMAIL + " TEXT UNIQUE,"+ KEY_USERNAME + " TEXT UNIQUE" + ")";
		db.execSQL(CREATE_LOGIN_TABLE);
		
		String CREATE_USER_MED = "CREATE TABLE user_med (id INTEGER PRIMARY KEY, uid INTEGER, name TEXT, current INTEGER)";
		db.execSQL(CREATE_USER_MED);
		
		String CREATE_USER_COND = "CREATE TABLE user_cond (id INTEGER PRIMARY KEY, uid INTEGER, name TEXT, year INTEGER, current INTEGER)";
		db.execSQL(CREATE_USER_COND);
		
		String CREATE_USER_TEST = "CREATE TABLE user_test (id INTEGER PRIMARY KEY, uid INTEGER, name TEXT, value FLOAT, year INTEGER, comments TEXT)";
		db.execSQL(CREATE_USER_TEST);
		
		String CREATE_USER_ALLERGY = "CREATE TABLE user_all (id INTEGER PRIMARY KEY, uid INTEGER, name TEXT)";
		db.execSQL(CREATE_USER_ALLERGY);
		
		String CREATE_USER_PROCEDURE = "CREATE TABLE user_proc (id INTEGER PRIMARY KEY, uid INTEGER, name TEXT, year INTEGER, comments TEXT)";
		db.execSQL(CREATE_USER_PROCEDURE);
		
		String CREATE_USER_VACCINE = "CREATE TABLE user_vacc (id INTEGER PRIMARY KEY, uid INTEGER, name TEXT, year INTEGER)";
		db.execSQL(CREATE_USER_VACCINE);
		
		String CREATE_DOCS = "CREATE TABLE doctor  (id INTEGER PRIMARY KEY, first TEXT, last TEXT, email TEXT, field TEXT, place TEXT, phone TEXT)";
		db.execSQL(CREATE_DOCS);
		
		String CREATE_SEARCH_DOCS = "CREATE TABLE result_doc  (id INTEGER PRIMARY KEY, first TEXT, last TEXT, email TEXT, field TEXT, place TEXT, phone TEXT)";
		db.execSQL(CREATE_SEARCH_DOCS);
		
		String perm="CREATE TABLE permission (uid INTEGER, did INTEGER, type TEXT, fileid INTEGER, PRIMARY KEY(uid, did, type, fileid))";
		db.execSQL(perm);
		
		String sav="CREATE TABLE save (uid INTEGER, username TEXT, sessid VARCHAR PRIMARY KEY, time_set LONGINT )";
		db.execSQL(sav);
		
		String NFC_perm="CREATE TABLE nfc_perm (uid INTEGER, did INTEGER, remain_t LONGINT,PRIMARY KEY(uid, did))";
		db.execSQL(NFC_perm);
		
		String PIN="CREATE TABLE pin (pin TEXT PRIMARY KEY)";
		db.execSQL(PIN);
	}

	
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older tables if exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS user_med");
        db.execSQL("DROP TABLE IF EXISTS user_cond");
        db.execSQL("DROP TABLE IF EXISTS user_test");
        db.execSQL("DROP TABLE IF EXISTS user_all");
        db.execSQL("DROP TABLE IF EXISTS user_proc");
        db.execSQL("DROP TABLE IF EXISTS user_vacc");
        db.execSQL("DROP TABLE IF EXISTS doctor");
        db.execSQL("DROP TABLE IF EXISTS nfc_perm");
        db.execSQL("DROP TABLE IF EXISTS result_doc");
        db.execSQL("DROP TABLE IF EXISTS permission");
        db.execSQL("DROP TABLE IF EXISTS save");
        // Create tables again
        onCreate(db);
	}
	
	/**
	 * DELETING FUNCTIONS 
	 * **/
	public void resetTables(){
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_NAME, null, null);
        db.delete("user_med", null, null);
        db.delete("user_all", null, null);
        db.delete("user_proc", null, null);
        db.delete("user_vacc", null, null);
        db.delete("user_cond", null, null);
        db.delete("user_test", null, null);
        db.delete("doctor", null, null);
        db.delete("result_doc", null, null);
        db.delete("permission", null, null);
        db.close();
    }
	
	public void resetSearch(){
		//drop search results
		SQLiteDatabase db = this.getWritableDatabase();
        db.delete("result_doc", null, null);

        db.close();
	}
	
	public void resetPermissions(){
		//drop permissions
		SQLiteDatabase db = this.getWritableDatabase();
	    db.delete("permission", null, null);

	    db.close();
	}
	
	public void resetPin(){
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete("pin", null, null);
		 
		db.close();
	}
	
	public void resetDocs(){
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete("doctor", null, null);
		 
	    db.close();
	}
	
	public void resetUser(){
		//drop users
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_NAME, null, null);
		 
	    db.close();
	}
	
	public void resetSave(){
		//drop user sessions
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete("save", null, null);

        db.close();
    }
	
	public void resetHistory(){
		//drop user history
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete("user_med", null, null);
        db.delete("user_all", null, null);
        db.delete("user_proc", null, null);
        db.delete("user_vacc", null, null);
        db.delete("user_cond", null, null);
        db.delete("user_test", null, null);

        db.close();
    }
	
	public void logout(){
		this.resetTables();
		this.resetSave();
		this.resetUser();
	}
	
	/**
	 * ADDING FUNCTIONS
	 * **/
	public void addpin(String pin){
		SQLiteDatabase db = this.getWritableDatabase();
	    ContentValues values = new ContentValues();
	    
	    values.put("pin", pin);
	    
	    db.insert("pin", null, values);
	    db.close();
	}
	
	public void addsave(int id, String username, String sessid, Long set){
		SQLiteDatabase db = this.getWritableDatabase();
	    ContentValues values = new ContentValues();
	    
	    values.put("uid", id);
	    values.put("username",username);
	    values.put("sessid",sessid);
	    values.put("time_set", set);
	    
	    db.insert("save", null, values);
	    db.close();
	}

	public void addDoctor(int did,String first,String last, String email, String field, String place, String phone){
		 SQLiteDatabase db = this.getWritableDatabase();
	     ContentValues values = new ContentValues();
	     
	     values.put("id", did); 
	     values.put("first", first);
	     values.put("last", last);
	     values.put("email", email);
	     values.put("field", field);
	     values.put("place", place);
	     values.put("phone", phone);
	        
	     db.insert("doctor", null, values);
	     db.close(); 
	}
	
	public void addResultDoctor(int did,String first,String last, String email, String field, String place, String phone){
		 SQLiteDatabase db = this.getWritableDatabase();
	     ContentValues values = new ContentValues();
	     	values.put("id", did); 
	        values.put("first", first);
	        values.put("last", last);
	        values.put("email", email);
	        values.put("field", field);
	        values.put("place", place);
	        values.put("phone", phone);
	        
	        db.insert("result_doc", null, values);
	        db.close(); 
		 
	}
	public void addPerms(int uid, int did, String type, int fileid){
		SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put("uid", uid); 
        values.put("did", did); 
        values.put("type", type); 
        values.put("fileid", fileid); 
        
        db.insert("permission", null, values);
        db.close();
	}
	
	public void addUser(int uid, String name, String last, String email, String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
       
        values.put(KEY_UID, uid); 
        values.put(KEY_NAME, name); 
        values.put(KEY_LAST, last); 
        values.put(KEY_EMAIL, email); 
        values.put(KEY_USERNAME, username);
        
        db.insert(TABLE_NAME, null, values);
        db.close();
    }
	
    public void addRecords(String type, int id, int uid, String name, int year, String comments, float value, int current, int did){
    	 
    	SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
    	
    	if(type=="medication"){
    		values.put("id", id);
    		values.put("uid",uid);
    		values.put("name", name);
    		values.put("current",current);
    		
    		db.insert("user_med",null, values);
    	}
    	if(type=="vaccine"){
    		values.put("id", id);
    		values.put("uid",uid);
    		values.put("name", name);
    		values.put("year",year);
    		
    		db.insert("user_vacc",null, values);
    	}
    	if(type=="procedure"){
    		values.put("id", id);
    		values.put("uid",uid);
    		values.put("name", name);
    		values.put("year",year);
    		values.put("comments", comments);
    		
    		
    		db.insert("user_proc",null, values);
    	}
    	if(type=="test"){
    		values.put("id", id);
    		values.put("uid",uid);
    		values.put("name", name);
    		values.put("year",year);
    		values.put("comments", comments);
    		values.put("value",value);
    		
    		db.insert("user_test",null, values);
    	}
    	if(type=="allergy"){
    		values.put("id", id);
    		values.put("uid",uid);
    		values.put("name", name);
    		
    		db.insert("user_all",null, values);
    	}
    	if(type=="condition"){
    		values.put("id", id);
    		values.put("uid",uid);
    		values.put("name", name);
    		values.put("year", year);
    		values.put("current",current);
    		
    		db.insert("user_cond",null, values);
    		
    	}
    	db.close();
    }
    
    //check if Doctor equals to Searched doctor then return a bool
    public boolean AddedDoctor(int id) {
        SQLiteDatabase db = this.getReadableDatabase();//setting current DB as readable
        boolean result;
        Cursor cursor = db.rawQuery("SELECT  * FROM doctor WHERE id="+id, null);
        int rowCount = cursor.getCount();
        if(rowCount>0){
        	result=true;
        }else{
        	result=false;
        }
        db.close();
        cursor.close();
        return result;
    }
}
