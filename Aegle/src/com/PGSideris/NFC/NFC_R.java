package com.PGSideris.NFC;

import java.util.ArrayList;
import java.util.Date;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import aegle.db.Cuery;
import aegle.db.Database;
import aegle.main.Init;
import aegle.web.CustomHttpTask;
import aegle.web.Server;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.PGSideris.aegle.R;

public class NFC_R extends Activity{

	private boolean mWriteMode = false;
	private static final String TAG = "NFC AEGLEA READ";
	protected String dname, did = "", input, uid;
	Server server = new Server();
	int count = 1;
	Cursor cursor = null,us=null;
	NfcAdapter mNfcAdapter;
	PendingIntent mNfcPendingIntent;
	IntentFilter[] mReadTagFilters, mWriteTagFilters;
	TextView title;
	Button confirm;
	EditText pin;
	protected String docname;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nfc_r);
		title = (TextView) findViewById(R.id.warning);
		confirm = (Button)findViewById(R.id.confirm);
		
		//setting Adapter and then check if Adapter exists
		mNfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
		if (mNfcAdapter == null){
			Toast.makeText(getApplicationContext(),"NFC not detected",Toast.LENGTH_LONG).show();
			finish();
		}
	    
		checkNFC();
		// Create a PendingIntent with FLAG_ACTIVITY_SINGLE_TOP flag so each new scan is not added to the Back Activity Stack(foreground Dispatch)
		mNfcPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(this,getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		// Create intent filter to handle NDEF NFC tags detected from inside our application when in "read mode":
		IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		try{
			ndefDetected.addDataType("application/com.pgsideris.doctors");
		}catch (MalformedMimeTypeException e){
			throw new RuntimeException("Could not add MIME type.", e);
		}
		// Create intent filter to detect any NFC tag when attempting to write  to a tag in "write mode"
		IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
		// create IntentFilter arrays:
		mWriteTagFilters = new IntentFilter[] { tagDetected };
		mReadTagFilters = new IntentFilter[] { ndefDetected, tagDetected };
		//enable clicks
		doclicks();
	}
	
	protected void onResume() {
		super.onResume();
		// Double check if NFC is enabled
		checkNFC();
		Log.d(TAG, "onResume: " + getIntent());
		if (getIntent().getAction() != null){
			// tag received when app is not running and not in the foreground:
			if (getIntent().getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)){
				NdefMessage[] msgs = getNdefMessagesFromIntent(getIntent());
				//create a record to read from
				NdefRecord record = msgs[0].getRecords()[0];
				byte[] payload = record.getPayload();
				setTextFieldValues(new String(payload));
			}
		}
		// Enable priority for current activity to detect scanned tags 
		mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mReadTagFilters, null);
	}
	
	protected void onNewIntent(Intent intent){
		Log.d(TAG, "onNewIntent: " + intent);
		if (!mWriteMode){
			// Currently in tag READING mode
			if (intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)){
				NdefMessage[] msgs = getNdefMessagesFromIntent(intent);
				String payload = new String(msgs[0].getRecords()[0].getPayload());
				setTextFieldValues(payload);
			}else if(intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)){
				Toast.makeText(this,"Unrelated NFC Tag",Toast.LENGTH_LONG).show();
			}
		}
	}
	
	NdefMessage[] getNdefMessagesFromIntent(Intent intent){
		// Parse the intent
		NdefMessage[] msgs = null;
		String action = intent.getAction();
		if (action.equals(NfcAdapter.ACTION_TAG_DISCOVERED) || action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED)){
			//if we found a tag then take the extra info
			Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
			//if not empty and recognized get all NDEF messages
			if (rawMsgs != null) {
				msgs = new NdefMessage[rawMsgs.length];
				for (int i = 0; i < rawMsgs.length; i++){
					msgs[i] = (NdefMessage) rawMsgs[i];
				}
			}else{
				// Unknown tag type
				byte[] empty = new byte[] {};
				NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
				NdefMessage msg = new NdefMessage(new NdefRecord[] { record });
				msgs = new NdefMessage[] { msg };
			}
		}else{
			Log.e(TAG, "Unknown intent.");
			finish();
		}
		return msgs;
	}
	
	@SuppressWarnings("unused")
	private void setTextFieldValues(String jsonString){
		JSONObject inventory = null;
		String name = "";
		String btype2 = "";
		try{
			inventory = new JSONObject(jsonString);
			name = inventory.getString("name");
			did = inventory.getString("did");
			docname=name;
		}catch (JSONException e){
			Log.e(TAG, "Couldn't parse JSON: ", e);
		}
		title.setText("Clicking will give temporary access to ALL of your records to "+name);
	}

	private void checkNFC() {
		Boolean nfcEnabled = mNfcAdapter.isEnabled();
		if (!nfcEnabled){
			new AlertDialog.Builder(NFC_R.this)
			.setTitle("NFC is turned off")
			.setMessage("Please enable NFC")
			.setCancelable(false)
			.setPositiveButton("Update Settings",
					new DialogInterface.OnClickListener(){

				public void onClick(DialogInterface dialog,int id){
					startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
				}})
				.create()
				.show();
		    }
	}

	private void go(){
	   LayoutInflater inflater =  getLayoutInflater();
	   Cuery q = new Cuery(getApplicationContext());
	   cursor = q.fetchOption("SELECT * FROM pin");
	   final View v = inflater.inflate(R.layout.pin, null);
	   pin = (EditText) v.findViewById(R.id.insert_pin);

	   Cuery q2 = new Cuery(getApplicationContext());
	   us = q2.fetchOption("SELECT * FROM save");
	   
	   if( us.getCount() == 0 ){
		   new AlertDialog.Builder(this)
           	.setTitle("Something's wrong!")
           	.setMessage("No user. Try logging in and then swipe the Tag again")
            .setNegativeButton("Ok", new DialogInterface.OnClickListener(){
	                public void onClick(DialogInterface dialog, int id){
	    		 		Intent login = new Intent(getApplicationContext(), Init.class);
	    		 		startActivity(login);
	                    finish();
	                }
	            })
           	.show();
	   }else{
		   uid = us.getString(us.getColumnIndex("uid"));
		   if( cursor.getCount() ==0 ){
		        new AlertDialog.Builder(this)
			        .setView(v)
		            .setTitle("First time. Save new PIN")
		            .setPositiveButton("Save", new DialogInterface.OnClickListener(){
		                public void onClick(DialogInterface dialog, int id){
		                	input = pin.getText().toString();
							
							ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
				        	postParameters.add(new BasicNameValuePair("uid",uid));
				        	postParameters.add(new BasicNameValuePair("pin",input));
				        	postParameters.add(new BasicNameValuePair("type","set"));
	
				            JSONObject response = null;	
				            try {
				        		CustomHttpTask asdf = new CustomHttpTask();
								response = asdf.execute(server.server+"pin.php", postParameters).get();
								if(response.getString("success").equals("1")){
									Database db = new Database(getApplicationContext());
				            		db.addpin(input);
								}
							}catch(Exception e){
								Log.e("On delete","THIS "+e);
							}
							dialog.cancel();
		                    go();
		                }
		            })
		            .setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
		                public void onClick(DialogInterface dialog, int id){
		                    finish();
		                }
		            })
		            .show();
		   }else{
			   new AlertDialog.Builder(this)
	        		.setView(v)
	                .setTitle("Insert PIN")
	                .setPositiveButton("Ok", new DialogInterface.OnClickListener(){
	                    public void onClick(DialogInterface dialog, int id){
	                    	//get the text
	                    	input = pin.getText().toString();
	                    	//get pin from DB
	                    	String conf = cursor.getString(cursor.getColumnIndex("pin"));
	                    	//if wrong 3 times end the Activity
	                    	if(count==3){
	                    		finish();
	                    	}
	                    	//if ok
	                    	if(input.equals(conf)){
	                    		Toast.makeText(getApplicationContext(), "Done", Toast.LENGTH_LONG).show();
	    						
	                    		//getting Session to post
	    						Cuery q = new Cuery(getApplicationContext());
	    						String session = q.getSess();
	    						//adding Parameters to POST
	    						ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
	    			        	postParameters.add(new BasicNameValuePair("session_id", session));
	    			        	postParameters.add(new BasicNameValuePair("uid",uid));
	    			        	postParameters.add(new BasicNameValuePair("did",did));
	    			        	postParameters.add(new BasicNameValuePair("add","nfc"));
	
	    			            JSONObject response = null;	
	    			            try {
	    			        		//Do the POST
	    			            	CustomHttpTask asdf = new CustomHttpTask();
	    							response = asdf.execute(server.server+"add_perm.php", postParameters).get();
	    							//if correct input
	    							if(response.getString("success").equals("1")){
			                    		
	    								Database openHelper = new Database(getApplicationContext());
			    	            		SQLiteDatabase myDB = openHelper.getReadableDatabase(); 
			    						myDB=SQLiteDatabase.openDatabase("data/data/com.PGSideris.aegle/databases/aeglea", null, SQLiteDatabase.OPEN_READWRITE);
			    						
			    						//deleting older entries if it is the same doctor 
			    						myDB.delete("nfc_perm", "did="+did, null);
			    						//getting time
			    						Date  dt = new Date();
			    						Long now=dt.getTime();
			    						//start adding to DB
			    						ContentValues values = new ContentValues();
			    					    values.put("uid", uid);
			    					    values.put("did", did);
			    					    values.put("remain_t", now);
			    						myDB.insert("nfc_perm", null, values);
			    						
			    						CountDownTimer waitTimer = new CountDownTimer(7200000, 100000) {

			    			                 public void onTick(long millisUntilFinished) {
			    			                    //called every 10 seconds
			    			                 }
			    			                 public void onFinish() {
			    			                	 //called every 2 hours
			    			                	//getting Session to post
			    		    					Cuery q = new Cuery(getApplicationContext());
			    		    					String session = q.getSess();
			    		    					//adding Parameters to POST
			    		    					ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
			    		    			        postParameters.add(new BasicNameValuePair("session_id", session));
			    		    			        postParameters.add(new BasicNameValuePair("uid",uid));
			    		    			        postParameters.add(new BasicNameValuePair("did",did));
			    		    			        postParameters.add(new BasicNameValuePair("delete","nfc"));
			    		
			    		    			        JSONObject response = null;	
			    		    			        try {
			    		    			        	//Do the POST
			    		    			        	CustomHttpTask asdf = new CustomHttpTask();
			    		    			        	response = asdf.execute(server.server+"del_perm.php", postParameters).get();
			    		    			        	//if correct input
			    		    			        	if(response.getString("success").equals("1")){
			    		    			        		//called when timer finishes after 2 hours
			    		    			        		Database openHelper = new Database(getApplicationContext());
			    		    			        		SQLiteDatabase myDB = openHelper.getReadableDatabase(); 
			    		    			        		myDB=SQLiteDatabase.openDatabase("data/data/com.PGSideris.aegle/databases/aeglea", null, SQLiteDatabase.OPEN_READWRITE);
			    		    			        		myDB.delete("nfc_perm", "did="+did, null);
			    		    			        		Toast.makeText(getApplicationContext(), "Permissions for "+docname+" removed", Toast.LENGTH_LONG).show();
			    			                	 }
			    		    			        }catch(Exception e){
			    		    			        	Log.e("timer delete","THIS "+e);
			    		    			        }
			    			                 }
			    			              };
			    			              waitTimer.start();
	    							}
	    						}catch(Exception e){
	    							Log.e("On delete","THIS "+e);
	    						}
	    						finish();
	                    	}else{
	                    		Toast.makeText(getApplicationContext(), "Wrong PIN. Try Again.", Toast.LENGTH_SHORT).show();
	                    		count++;
	                    	}
	                    }
	                })
	                .setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
	                    public void onClick(DialogInterface dialog, int id){
	                        dialog.cancel();
	                        finish();
	                    }
	                })
	                .show();
		   }
	   }
    }
	
	private void doclicks(){
		confirm.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				go();
			}
			
		});
		
	}
}
