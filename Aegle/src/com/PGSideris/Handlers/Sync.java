package com.PGSideris.Handlers;

import java.util.ArrayList;
import java.util.Date;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class Sync {

	protected Cursor cursor=null,cursor2=null;
	private Context context;
	private	Database db;
	protected String type="session",name,pw;
	Server server = new Server();
	String session = null;
	
	public Sync(Context conte, String nam, String pass){
		context=conte; 
		type="normal";
		name=nam;
		pw=pass;
		db = new Database(context);
	}
	
	public Sync(Context conte){
		context=conte; 
		db = new Database(context);
	}
	
	public int user(){
		int result=0;
		if(type=="session"){
			Cuery q = new Cuery(context);
	  		cursor2 = q.fetchOption("SELECT * FROM save");//execute SQL query
			
		    ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
		    postParameters.add(new BasicNameValuePair("uid",cursor2.getString(cursor2.getColumnIndex("uid"))));
		    postParameters.add(new BasicNameValuePair("sessid", cursor2.getString(cursor2.getColumnIndex("sessid"))));
		    //String valid = "1";
		    JSONObject response = null;
		    try {
		    	//do post to check if sign in
		        CustomHttpTask asdf = new CustomHttpTask();
		        response = asdf.execute(server.server+"log.php", postParameters).get();
		        //if ok then insert user and sign in
		        if(response.getString("success").equals("1")){
		        	//reset older entries
		           	db.resetTables();
		           	//store user to database in success. add user
		           	db.addUser(Integer.parseInt(response.getString("id")), response.getString("name"), response.getString("last"), response.getString("email"), response.getString("username"));
		        }else{
		        	result=1;
		        }
		     }catch (Exception e) {
		    	 result=2;
		    	 Log.e("Session Login",""+e);
		     }
		    
		}else if(type=="normal"){
			
			ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("username", name));
            postParameters.add(new BasicNameValuePair("password", pw));
            //String valid = "1";
            JSONObject response = null;
            try {
            	//do post to check if sign in
            	CustomHttpTask asdf = new CustomHttpTask();
            	response = asdf.execute(server.server+"log.php", postParameters).get();
            	//if ok then insert user and sign in
            	if(response.getString("success").equals("1")){
               		//reset older entries
               		db.resetTables();
               		db.resetSave();
               		//get time now
               		Date  dt =new Date();
        			Long now=dt.getTime();
               		//store user to database in success. add user
               		db.addUser(Integer.parseInt(response.getString("id")), response.getString("name"), response.getString("last"), response.getString("email"), response.getString("username"));
               		db.addsave(Integer.parseInt(response.getString("id")), response.getString("username"), response.getString("session"), now);
            	}else{
            		result=1;
            	}
            }catch (Exception e) {
   		    	  Log.e("Normal Login",""+e);
   		    	  result=2;
            }
        }
		return result;
	}

	public void others(){
		lala();
		docs();
		history();
		perms();
		pin();
	}
	
	public void addperms(){
		lala();
		perms();
	}
	
	private void lala(){
		Cuery q = new Cuery(context);
		cursor = q.fetchOption("SELECT * FROM user_login");//use above to execute SQL query

	}
	
	private void docs(){
		//get Session to post for validity
		Cuery q = new Cuery(context);
		session = q.getSess();
		//setting parameters to POST
		ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
       	postParameters.add(new BasicNameValuePair("uid", cursor.getString(cursor.getColumnIndex("uid"))));
		postParameters.add(new BasicNameValuePair("session_id", session));

       	JSONArray udocs=null;
       	JSONObject response,buffer = null;
       	try {
       		CustomHttpTask asdf = new CustomHttpTask();
       		response = asdf.execute(server.server+"user_doctors.php", postParameters).get();
			if(response.getString("success").equals("1")){
    	   		//get JSON Arrays if successful
				udocs = response.getJSONArray("doctor");
				db.resetDocs();
				for(int i=0;i<udocs.length();i++){
					buffer=udocs.getJSONObject(i);
					db.addDoctor(
						Integer.parseInt(buffer.getString("id")),
						buffer.getString("first"),
						buffer.getString("last"),
						buffer.getString("email"), 
						buffer.getString("field"), 
						buffer.getString("place"), 
						buffer.getString("phone"));
				}
 			}
    	}catch (Exception e1) {
    		Log.e("doctors",""+e1);
        }
	}
	
	public void pin(){
		ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
       	postParameters.add(new BasicNameValuePair("uid", cursor.getString(cursor.getColumnIndex("uid"))));
       	postParameters.add(new BasicNameValuePair("type", "get"));

       	JSONObject response = null;
       	try {
       		CustomHttpTask asdf = new CustomHttpTask();
       		response = asdf.execute(server.server+"pin.php", postParameters).get();
			if(response.getString("success").equals("1")){
    	   		//get JSON Arrays
				String pin = response.getString("pin");
				db.resetPin();
				db.addpin(pin);
 			}
    	}catch (Exception e1) {
    		Log.e("pin",""+e1);
        }
	}
	
	private void history(){
		Database db = new Database(context);
		JSONArray allergy=null,condition=null,medication=null,procedure=null,test = null,vaccine=null;
		//get Session to post for validity
		Cuery q = new Cuery(context);
		session = q.getSess();
		
		//setting and adding POST parameters
		ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
		postParameters.add(new BasicNameValuePair("uid", cursor.getString(cursor.getColumnIndex("uid"))));
		postParameters.add(new BasicNameValuePair("session_id", session));
		
		 JSONObject response = null;
		 try {
			 //creating Client for query
			 CustomHttpTask asdf = new CustomHttpTask();
			 response = asdf.execute(server.server+"history.php", postParameters).get();
			 if(response.getString("success").equals("1")){
				 //get JSON Arrays from response
				 try{test = response.getJSONArray("test");}catch(Exception e){}
				 try{allergy = response.getJSONArray("allergy");}catch(Exception e){}
				 try{condition = response.getJSONArray("condition");}catch(Exception e){}
				 try{medication = response.getJSONArray("medication");}catch(Exception e){}
				 try{procedure = response.getJSONArray("procedure");}catch(Exception e){}
				 try{vaccine = response.getJSONArray("vaccine");}catch(Exception e){}
			 }
		 }catch (Exception e) {
			 Log.e("history",""+e);
		 }
		 //temp JSONOnject
		 JSONObject buffer=null;
		 //reset older values
		 db.resetHistory();
		 //Store values 
		 try{
			 for(int i=0;i<allergy.length();i++){
				 buffer=allergy.getJSONObject(i);
				 db.addRecords("allergy", 
						 Integer.parseInt(buffer.getString("id")), 
						 Integer.parseInt(buffer.getString("uid")), 
						 buffer.getString("name"), 
						 1, 
						 "",
						 (float) 0.1, 
						 1, 
						 1);
            	   		}
		 }catch(Exception e){
		 }
		 try{
			 for(int i=0;i<condition.length();i++){
				 buffer=condition.getJSONObject(i);
				 db.addRecords("condition", 
						 Integer.parseInt(buffer.getString("id")), 
						 Integer.parseInt(buffer.getString("uid")), 
						 buffer.getString("name"),
						 Integer.parseInt(buffer.getString("year")) , 
						 "",
						 (float) 0.1, 
						 Integer.parseInt(buffer.getString("current")), 
						 1);
			 }
		 }catch(Exception e){
		 }
		 try{
			 for(int i=0;i<medication.length();i++){
				 buffer=medication.getJSONObject(i);
				 db.addRecords("medication", 
						 Integer.parseInt(buffer.getString("id")), 
						 Integer.parseInt(buffer.getString("uid")), 
						 buffer.getString("name"), 
						 1, 
						 "",
						 (float) 0.1, 
						 Integer.parseInt(buffer.getString("current")), 
						 1);
			 }
		 }catch(Exception e){
		 }
		 try{
			 for(int i=0;i<procedure.length();i++){
				 buffer=procedure.getJSONObject(i);
				 db.addRecords("procedure", 
						 Integer.parseInt(buffer.getString("id")), 
						 Integer.parseInt(buffer.getString("uid")), 
						 buffer.getString("name"), 
						 Integer.parseInt(buffer.getString("year")), 
						 buffer.getString("comments"),
						 (float) 0.1, 
						 0, 
						 1);
			 }
		 }catch(Exception e){
		 }
		 try{
			 for(int i=0;i<vaccine.length();i++){
				 buffer=vaccine.getJSONObject(i);
				 db.addRecords("vaccine", 
						 Integer.parseInt(buffer.getString("id")),
						 Integer.parseInt(buffer.getString("uid")), 
						 buffer.getString("name"),
						 Integer.parseInt(buffer.getString("year")), 
						 "",
						 (float) 0.1, 
						 0, 
						 1);
			 }
		 }catch(Exception e){
		 }
		 try{
			 for(int i=0;i<test.length();i++){
				 buffer=test.getJSONObject(i);
				 db.addRecords("test", 
						 Integer.parseInt(buffer.getString("id")), 
						 Integer.parseInt(buffer.getString("uid")), 
						 buffer.getString("name"), 
						 Integer.parseInt(buffer.getString("year")), 
						 buffer.getString("comments"),
						 (float) Float.parseFloat(buffer.getString("value")), 
						 1, 
						 1);
			 }
		 }catch(Exception e){
		 }
	}
	
	private void perms(){
		//new DB object
		Database db = new Database(context);
		
		//get Session to post for validity
		Cuery q = new Cuery(context);
		session = q.getSess();
		//adding parameters to POST to server
		ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
       	postParameters.add(new BasicNameValuePair("uid", cursor.getString(cursor.getColumnIndex("uid"))));
       	postParameters.add(new BasicNameValuePair("parmassions", "asdfg"));
       	postParameters.add(new BasicNameValuePair("session_id", session));

       	JSONObject response,buffer = null;
       	JSONArray perm = null;
       	try {
       		CustomHttpTask asdf = new CustomHttpTask();
       		response = asdf.execute(server.server+"user_perms.php", postParameters).get();
       		//if valid response
			if(response.getString("success").equals("1")){
   	   		//get JSON Arrays
				perm = response.getJSONArray("perms");
				db.resetPermissions();
				for(int i=0;i<perm.length();i++){
					buffer=perm.getJSONObject(i);
					db.addPerms(
						Integer.parseInt(buffer.getString("uid")),
						Integer.parseInt(buffer.getString("did")),
						buffer.getString("type"),
						Integer.parseInt(buffer.getString("fileid")));
				}
			}
       	}catch (Exception e1) {
       		Log.e("perms",""+e1);
       	}
	}
}
