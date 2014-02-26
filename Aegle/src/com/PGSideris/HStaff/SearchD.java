package com.PGSideris.HStaff;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import aegle.db.Cuery;
import aegle.db.Database;
import aegle.web.CustomHttpTask;
import aegle.web.Server;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.PGSideris.aegle.R;

public class SearchD extends Activity{

	Button sid,sna;
	EditText first,last,id;
	JSONObject buffer;
	String uid=null;
	Server server = new Server();
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_searchd);
		//data from Intent
		uid=getIntent().getStringExtra("uid");
		//Set Views and enable click actions
		sid =(Button) findViewById(R.id.sea_id);
		sna = (Button) findViewById(R.id.sea_name);
		first = (EditText) findViewById(R.id.doc_first);
		last = (EditText) findViewById(R.id.doc_last);
		id  = (EditText) findViewById(R.id.DId);
		doclicks();
	}
	
	private void doclicks(){
		//click listeners
		sna.setOnClickListener(new View.OnClickListener() {
			private Database db = new Database(getApplicationContext());
			
			boolean ok=false;
			JSONArray result_docs;
			public void onClick(View v) {
				//take session and add parameters to POST
				Cuery q = new Cuery(getApplicationContext());
				String session = q.getSess();
				ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
	        	postParameters.add(new BasicNameValuePair("session_id", session));
	        	//reset older searches
	        	db.resetSearch();
				if(!first.getText().toString().equals("") && !last.getText().toString().equals("")){
					postParameters.add(new BasicNameValuePair("first",first.getText().toString()));
					postParameters.add(new BasicNameValuePair("last",last.getText().toString()));
					ok=true;
				}else if(first.getText().toString().equals("") && !last.getText().toString().equals("")){
					postParameters.add(new BasicNameValuePair("last",last.getText().toString()));
					ok=true;
				}else if(!first.getText().toString().equals("") && last.getText().toString().equals("")){
					postParameters.add(new BasicNameValuePair("first",first.getText().toString()));
					ok=true;
				}else{
					Toast.makeText(getApplicationContext(), "Input values", Toast.LENGTH_SHORT).show();
				}
				JSONObject response = null;
				if(ok){
		        	try {
		        		CustomHttpTask asdf = new CustomHttpTask();
						response = asdf.execute(server.server+"sdoc.php", postParameters).get();
						if(response.getString("success").equals("1")){
							if(response.has("doctor")){
								result_docs=response.getJSONArray("doctor");
								for(int i=0;i<result_docs.length();i++){
									buffer=result_docs.getJSONObject(i);
									db.addResultDoctor(
										Integer.parseInt(buffer.getString("id")), 
										buffer.getString("first"),
										buffer.getString("last"), 
										buffer.getString("email"),
										buffer.getString("field"),
										buffer.getString("place"),
										buffer.getString("phone"));
								}
								//create new intent
						   		Intent rdocs = new Intent(getApplicationContext(), ResultDoctors.class);
					            // Close all views before launching logged
						   		rdocs.putExtra("uid",uid);
						   		rdocs.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					            startActivity(rdocs);
							}else{
								Toast.makeText(getApplicationContext(), "No results try again", Toast.LENGTH_SHORT).show();
							}
						}
	        		} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					} catch (JSONException e) {
						e.printStackTrace();
					}
			}
		}
		});
		
		sid.setOnClickListener(new View.OnClickListener() {
			private Database db = new Database(getApplicationContext());
			
			boolean ok=false;
			JSONArray result_docs;
			public void onClick(View v) {
				//take session and add parameters to POST
				Cuery q = new Cuery(getApplicationContext());
				String session = q.getSess();
				ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
	        	postParameters.add(new BasicNameValuePair("session_id", session));
	        	
				db.resetSearch();
				if(!id.getText().toString().equals("")){
					postParameters.add(new BasicNameValuePair("did",id.getText().toString()));
					ok=true;
				}else{
					Toast.makeText(getApplicationContext(), "Input values", Toast.LENGTH_SHORT).show();
				}
				JSONObject response = null;
				if(ok){
		       		try {
		       			CustomHttpTask asdf = new CustomHttpTask();
						response = asdf.execute(server.server+"sdoc.php", postParameters).get();
						if(response.getString("success").equals("1")){
							if(response.has("doctor")){
								result_docs=response.getJSONArray("doctor");
								for(int i=0;i<result_docs.length();i++){
									buffer=result_docs.getJSONObject(i);
									db.addResultDoctor(
										Integer.parseInt(buffer.getString("id")), 
										buffer.getString("first"),
										buffer.getString("last"), 
										buffer.getString("email"),
										buffer.getString("field"),
										buffer.getString("place"),
										buffer.getString("phone"));
								}
								//create new intent
							   	Intent rdocs = new Intent(getApplicationContext(), ResultDoctors.class);
						        // Close all views before launching logged
						   		rdocs.putExtra("uid",uid);
							   	rdocs.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					            startActivity(rdocs);
					            finish();
							}else{
								Toast.makeText(getApplicationContext(), "No results try again", Toast.LENGTH_SHORT).show();
							}
						}
	        		} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					} catch (JSONException e) {
						e.printStackTrace();
					}
			}
		}
	});
	}
}
