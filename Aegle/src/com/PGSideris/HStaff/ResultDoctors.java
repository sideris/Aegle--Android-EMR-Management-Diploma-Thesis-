package com.PGSideris.HStaff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import aegle.db.Cuery;
import aegle.db.Database;
import aegle.web.CustomHttpTask;
import aegle.web.Server;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.PGSideris.aegle.R;


public class ResultDoctors extends Activity{
	protected String uid;
	protected Cursor cursor=null;
	protected SQLiteDatabase myDB,myDB2=null;
	private ArrayAdapter<String> listAdapter ;
	Button search;
	ListView list=null;
	String[] dids;
	String ex="";//string which annotates if the doctor is added or not
	Server server = new Server();
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_doctors);
		//get data from Intent
		uid=getIntent().getStringExtra("uid");
		//Assign Views and enable clicks
		list= (ListView)findViewById(R.id.Doctors_list);
		list.setHapticFeedbackEnabled(true);
		list.performHapticFeedback(0);
		search=(Button) findViewById(R.id.Se_doc_but);
		search.setText("Search Again");
		
		doclicks();
	}
	
	protected void onResume(){
		super.onResume();
		//getting data
		fetchdata();
	}
	
	private void fetchdata(){
		Database openHelper = new Database(this);
		//quering DB
		Cuery q = new Cuery(this);
		cursor=q.fetchOption("SELECT * FROM result_doc");
		ArrayList<String> itemlist = new ArrayList<String>();  
		String[] names=null;
		dids=new String[cursor.getCount()];
		
		if (cursor.getCount()==0) {//if empty results
			names = new String[] { "No Results. Go back and Search again"}; 
        }else{
        	if(openHelper.AddedDoctor(Integer.parseInt(cursor.getString(cursor.getColumnIndex("id"))))){
        		ex="(Added)";//doctor is added 
        	}
        	if(!cursor.getString(cursor.getColumnIndex("field")).equals("null")){//initialize did Array with Doc Id's and names Array
        		names=new String[] {cursor.getString(cursor.getColumnIndex("first"))+" "+cursor.getString(cursor.getColumnIndex("last"))+ex+"\nfield: "+cursor.getString(cursor.getColumnIndex("field"))};
        		dids[0]= cursor.getString(cursor.getColumnIndex("id"));
        	}else{
        		names=new String[] {cursor.getString(cursor.getColumnIndex("first"))+" "+cursor.getString(cursor.getColumnIndex("last"))+ex};
        		dids= new String[] {cursor.getString(cursor.getColumnIndex("id"))};
        	}
        }
		//Add to Array list the names and append it to ArrayAdapter
		itemlist.addAll( Arrays.asList(names) ); 
		listAdapter = new ArrayAdapter<String>(this, R.layout.item, itemlist); 
		 
		//if results add the rest
		if(cursor.getCount()!=0){
			 for(int i=0;i<(cursor.getCount()-1);i++){
				 cursor.moveToNext();

				 if(openHelper.AddedDoctor(Integer.parseInt(cursor.getString(cursor.getColumnIndex("id"))))){ex="(Added)"; }else{ex="";}
				 
				 dids[i+1]=cursor.getString(cursor.getColumnIndex("id"));
				 
				 if(!cursor.getString(cursor.getColumnIndex("field")).equals("null")){
					 listAdapter.add(cursor.getString(cursor.getColumnIndex("first"))+" "+cursor.getString(cursor.getColumnIndex("last"))+ex+"\nfield: "+cursor.getString(cursor.getColumnIndex("field")));
				 }
				 else{
					 listAdapter.add(cursor.getString(cursor.getColumnIndex("first"))+" "+cursor.getString(cursor.getColumnIndex("last"))+ex);
		        	}
			 }
		 }
		 // Set the ArrayAdapter as the ListView's adapter.  
		 list.setAdapter(listAdapter); 
	}
	
	private void doclicks(){
		//click listeners
		list.setOnItemLongClickListener(new OnItemLongClickListener(){
			 
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,int arg2, long arg3) {
				final String alo=dids[arg2];
				final Database db = new Database(getApplicationContext());
				arg1.performHapticFeedback(1);
				Object item = arg0.getItemAtPosition(arg2);
				String title = item.toString();

			if(!title.contains("(Added)")){
				new AlertDialog.Builder(arg0.getContext())
			    .setTitle("Add")
			    .setMessage("Add Doctor?")
			    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int which) {
							//get Session to post for validity
							Cuery q = new Cuery(getApplicationContext());
							String session = q.getSess();
				            // continue with adding doctor
				        	ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
				        	postParameters.add(new BasicNameValuePair("session_id",session));
				        	postParameters.add(new BasicNameValuePair("did",alo));
				        	postParameters.add(new BasicNameValuePair("uid",uid));
				        	postParameters.add(new BasicNameValuePair("kind","add"));
							JSONObject response = null;
				        	try {
			        			CustomHttpTask asdf = new CustomHttpTask();
								response = asdf.execute(server.server+"add_del_doc.php", postParameters).get();
								if(response.getString("success").equals("1")){
									JSONObject b=null;
									b=response.getJSONObject("doctor");
									db.addDoctor(
											Integer.parseInt(b.getString("id")),
											b.getString("first"), 
											b.getString("last"),
											b.getString("email"), 
											b.getString("field"),
											b.getString("place"), 
											b.getString("phone"));
									fetchdata();
								}
				        	} catch (InterruptedException e) {
								e.printStackTrace();
							} catch (ExecutionException e) {
								e.printStackTrace();
							} catch (JSONException e) {
								e.printStackTrace();
							}
				        	
				        }
			     })
			    .setNegativeButton("No", new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int which) { 
			        	//do nothing noob
			        }}).show();
			}return true;
			
			}
			 
		 });
		
		 list.setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
					String alo=dids[arg2];
					
					//create new intent
	    	   		Intent doc = new Intent(getApplicationContext(), Doc.class);
	                // Close all views before launching logged
	    	   		doc.putExtra("did", alo);
	    	   		doc.putExtra("tavle", "result_doc");
	    	   		doc.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    	   		startActivity(doc);
				}
			  });
		 
		 search.setOnClickListener(new OnClickListener(){

			public void onClick(View arg0) {
				//create new intent
   	   		Intent search = new Intent(getApplicationContext(), SearchD.class);
               // Close all views before launching logged
   	   		search.putExtra("uid", uid);
   	   		search.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
   	   		startActivity(search);
               // Close Login Screen
			}});
	}
	
}
