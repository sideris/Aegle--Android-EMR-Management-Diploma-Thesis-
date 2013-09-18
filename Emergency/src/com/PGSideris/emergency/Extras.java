package com.PGSideris.emergency;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import Handlers.CustomHttpTask;
import Handlers.Server;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class Extras extends Activity{
	
	private ArrayAdapter<String> listAdapter ;
	protected String uid,type;
	protected JSONObject response = null;
	protected JSONArray data=null;
	ListView list;
	TextView title;
	Server server = new Server();

	protected void onCreate(Bundle savedInstanceState ){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_extras);
		list = (ListView) findViewById(R.id.list1);
		title = (TextView) findViewById(R.id.title);

		//get data from Intent
		uid = getIntent().getStringExtra("uid");
		type = getIntent().getStringExtra("type");
		//setting title
		if(type.equals("allergy")){
			title.setText("Allergies");
		}
		if(type.equals("condition")){
			title.setText("Medical Conditions");
		}
		if(type.equals("medication")){
			title.setText("Medication");
		}
	}
	
	protected void onResume(){
		super.onResume();
		pulldata();
	}
	
	private void pulldata(){
		//setting Paramaters for POST
		ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
     	postParameters.add(new BasicNameValuePair("SUPER_SECRET_KEY", "26b0b0945263eb85a444bb197c09fec3"));
     	postParameters.add(new BasicNameValuePair("type", type));
     	postParameters.add(new BasicNameValuePair("uid",uid));
     	try {
     		//executing POST
    		CustomHttpTask asdf = new CustomHttpTask();
			response = asdf.execute(server.server+"emergency.php", postParameters).get();
			if(response.getString("success").equals("1")){
				//getting the Data and populating
				data = response.getJSONArray("values");
				populate();
			}
		}catch(Exception e){
				Log.e("POST DATA","THIS "+e);
		}
	}
	
	private void populate(){
		ArrayList<String> itemlist = new ArrayList<String>();  
		String[] names=null;
		if (data.length()==0) {
			//if we don't have anything
			names = new String[] { "Nothing here."};
		}else{
			 try {
				names = new String[]{ data.getString(0)};
			} catch (JSONException e1) {
				Log.e("init name", ""+e1);
			}
			//add the array "names" as list to the ArrayList and set the listAdapter with correspondence to the list
			itemlist.addAll( Arrays.asList(names) ); 
			listAdapter = new ArrayAdapter<String>(this, R.layout.item, itemlist);
			
			if(data.length()>1){
				try{
					for(int i=1;i<data.length();i++){
						//buffer = data.getJSONObject(i);
						listAdapter.add(data.getString(i));
					}
				}catch(Exception e){
					Log.e("populating",""+e);
				}
			}
			// Set the ArrayAdapter as the ListView's adapter.  
			 list.setAdapter(listAdapter);
		}
	}
}
