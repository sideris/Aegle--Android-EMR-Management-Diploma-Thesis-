package com.PGSideris.emergency;

import org.json.JSONException;
import org.json.JSONObject;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class LaunchNRead extends Activity {
	
	private boolean mWriteMode = false;
	private static final String TAG = "NFC AEGLEA READ";
    protected String uidd = "";
	NfcAdapter mNfcAdapter;
	PendingIntent mNfcPendingIntent;
	IntentFilter[] mReadTagFilters, mWriteTagFilters;
	TextView info;
	Button con,med,all;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_launch_nread);
		
		//make views
		info = (TextView) findViewById(R.id.user_info);
		con = (Button) findViewById(R.id.us_cond);
		med = (Button) findViewById(R.id.us_med);
		all = (Button) findViewById(R.id.us_al);
		
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
            ndefDetected.addDataType("application/com.pgsideris.aeglea");
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
                Toast.makeText(this,"This NFC tag currently has no NDEF data.",Toast.LENGTH_LONG).show();
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
            //if not empty and recognized gett all NDEF messages
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
        } else{
            Log.e(TAG, "Unknown intent.");
            finish();
        }
        return msgs;
    }
	
    private void setTextFieldValues(String jsonString){
        JSONObject inventory = null;
        String name = "";
        String btype2 = "";
        try{
            inventory = new JSONObject(jsonString);
            name = inventory.getString("name");
            uidd = inventory.getString("uid");
            btype2 = inventory.getString("BType");
        }catch (JSONException e){
            Log.e(TAG, "Couldn't parse JSON: ", e);
        }
        info.setText("Full name: "+name+"\nBlood Type: "+btype2+"\nAeglea ID: "+uidd);
    }
    
	private void checkNFC() {
		Boolean nfcEnabled = mNfcAdapter.isEnabled();
	    if (!nfcEnabled){
	    	new AlertDialog.Builder(LaunchNRead.this)
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
	
	private void doclicks(){
		con.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				 //create new intent
				 Intent extras = new Intent(getApplicationContext(), Extras.class);
				 //put extra data
				 extras.putExtra("uid", uidd);
				 extras.putExtra("type", "condition");
				 // Close all views before launching logged
				 extras.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				 startActivity(extras);
			}
			
		});
		all.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				 //create new intent
				 Intent extras = new Intent(getApplicationContext(), Extras.class);
				 //put extra data
				 extras.putExtra("uid", uidd);
				 extras.putExtra("type", "allergy");
				 // Close all views before launching logged
				 extras.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				 startActivity(extras);
			}
			
		});
		med.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				 //create new intent
				 Intent extras = new Intent(getApplicationContext(), Extras.class);
				 //put extra data
				 extras.putExtra("uid", uidd);
				 extras.putExtra("type", "medication");
				 // Close all views before launching logged
				 extras.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				 startActivity(extras);
			}
			
		});
	}
}
