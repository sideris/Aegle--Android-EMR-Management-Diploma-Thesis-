package com.PGSideris.Handlers;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import android.os.AsyncTask;
//we need to create a class so we can have Asynchronous Server communication.
public class CustomHttpTask extends AsyncTask<Object,JSONObject,JSONObject> {
	   public static final int HTTP_TIMEOUT = 30 * 1000; // milliseconds
	   private JSONObject response = null;
	   
	   @Override
	   protected JSONObject doInBackground(Object... params) {
	       //subclass of the superclass CustomHttpClient
	       String url = (String) params[0];
	       @SuppressWarnings("unchecked")
	       //here we set the post parameters. We need params[1] specifically, because in our implementation 
	       ArrayList<NameValuePair> postParameters = (ArrayList<NameValuePair>) params[1];
	       try {
			response = CustomHttpClient.executeHttpPost(url, postParameters);
	       } catch (Exception e) {
			e.printStackTrace();
	       }
	        // if we need response string back
	        return response;
	   }
	   
	    @Override
	    protected void onPostExecute(JSONObject result) {
	    	//nothing we handle it later. The reason is that the Asynchronous part fails as we use a synchronous task in a higher implementation level.
	    }
	    
	    public JSONObject getval(){
	    	//we return the item we want as a JSONObject
	    	return response;
	    }
		
	}