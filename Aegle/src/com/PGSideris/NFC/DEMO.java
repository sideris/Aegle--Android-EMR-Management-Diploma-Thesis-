package com.PGSideris.NFC;

import java.io.IOException;
import java.nio.charset.Charset;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.PGSideris.aegle.R;


public class DEMO extends Activity{

		
		private static final String TAG = "NFC AEGLEA WRITE";
		// NFC-related variables
		private boolean mWriteMode = false;
		//private Cursor cursor=null;
		NfcAdapter mNfcAdapter;
		PendingIntent mNfcPendingIntent;
		IntentFilter[] mReadTagFilters, mWriteTagFilters;
		// UI elements
		String fullname, did;
		AlertDialog mWriteTagDialog;
	    Button write;

	    /* for demo purposes*/
	    Button doc;
	    
		public void onCreate(Bundle savedInstanceState){
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_nfcw);
	        
	        findViewById(R.id.write_usNFC).setOnClickListener(mTagWriter);
	        doc = (Button) findViewById(R.id.demo_doc);
	        doc.setVisibility(0);
	        
	        //setting Adapter and then check if Adapter exists
	        mNfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
	        if (mNfcAdapter == null){
	            Toast.makeText(getApplicationContext(),"NFC not detected",Toast.LENGTH_LONG).show();
	            finish();
	        }
	        //check if NFC is enabled
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
	                confirmDisplayedContentOverwrite(msgs[0]);
	            }else if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)){
	                Toast.makeText(this,"This NFC tag currently has no NDEF data.",Toast.LENGTH_LONG).show();
	            }
	        }else{
	            // Currently in tag WRITING mode
	            if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
	                Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
	                writeTag(createNdefFromJson(), detectedTag);
	                mWriteTagDialog.cancel();
	            }
	        }
	    }
	    
	    
	    /**
	     * **** READING MODE METHODS ****
	     **/
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
	    /**
	   I don't want these. They are for reading data and then parsing them to the TextFields. 
	   Leave it for the "emergency scenario". Hospital personnel will be able to read with this methods 
	   
	   **/
	   
	    private void confirmDisplayedContentOverwrite(final NdefMessage msg){
	        new AlertDialog.Builder(this)
	                .setTitle("New Tag found!")
	                .setMessage("Read Tag and replace data?")
	                .setPositiveButton("Yes", new DialogInterface.OnClickListener(){
	                    public void onClick(DialogInterface dialog, int id){
	                        // use the current values in the NDEF payload to update the text fields
	                        String payload = new String(msg.getRecords()[0].getPayload());
	                        setTextFieldValues(payload);
	                    }
	                })
	                .setNegativeButton("No", new DialogInterface.OnClickListener(){
	                    public void onClick(DialogInterface dialog, int id){
	                        dialog.cancel();
	                    }
	                }).show();
	    }

	    private void setTextFieldValues(String jsonString){
	        JSONObject inventory = null;
	        String name = "";
	        String didd = "";
	        try{
	            inventory = new JSONObject(jsonString);
	            name = inventory.getString("name");
	            didd = inventory.getString("did");
	        }catch (JSONException e){
	            Log.e(TAG, "Couldn't parse JSON: ", e);
	        }
	        Toast.makeText(getApplicationContext(), "name: "+name+" did: "+didd+"", Toast.LENGTH_LONG).show();

	    }

	    /**
	     * **** WRITING MODE METHODS ****
	     **/

	    private View.OnClickListener mTagWriter = new View.OnClickListener(){
	        public void onClick(View arg0){

	            enableTagWriteMode();

	            AlertDialog.Builder builder = new AlertDialog.Builder(DEMO.this)
	                    .setTitle("Ready to Write")
	                    .setMessage("Touch a Tag to write")
	                    .setCancelable(true)
	                    .setNegativeButton("Cancel",
	                            new DialogInterface.OnClickListener(){
	                                public void onClick(DialogInterface dialog,int id){
	                                    dialog.cancel();
	                                }
	                            })
	                    .setOnCancelListener(new DialogInterface.OnCancelListener(){
	                        public void onCancel(DialogInterface dialog){
	                            enableTagReadMode();
	                        }
	                    });
	            mWriteTagDialog = builder.create();
	            mWriteTagDialog.show();
	        }
	    };

	    private NdefMessage createNdefFromJson(){

	        // create a JSON object out of the values:
	        JSONObject doc = new JSONObject();
	        try{
	        	doc.put("name", "Gregory House");
	        	doc.put("did", "7");
	        } catch (JSONException e){
	            Log.e(TAG, "Could not create JSON: ", e);
	        }

	        // create a new NDEF record and containing NDEF message using the app's custom MIME type:
	        String mimeType = "application/com.pgsideris.doctors";
	        byte[] mimeBytes = mimeType.getBytes(Charset.forName("UTF-8"));
	        String data = doc.toString();
	        byte[] dataBytes = data.getBytes(Charset.forName("UTF-8"));
	        byte[] id = new byte[0];
	        NdefRecord record = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,mimeBytes, id, dataBytes);
	        NdefMessage m = new NdefMessage(new NdefRecord[] { record });
	        // return the NDEF message
	        return m;
	    }

	    private void enableTagWriteMode(){
	        mWriteMode = true;
	        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mWriteTagFilters, null);
	    }

	    private void enableTagReadMode() {
	        mWriteMode = false;
	        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mReadTagFilters, null);
	    }

	    boolean writeTag(NdefMessage message, Tag tag){
	        int size = message.toByteArray().length;
	        try{
	            Ndef ndef = Ndef.get(tag);
	            if (ndef != null){
	                ndef.connect();

	                if (!ndef.isWritable()) {
	                    Toast.makeText(this,"This Tag is read-only.", Toast.LENGTH_LONG).show();
	                    return false;
	                }
	                if (ndef.getMaxSize() < size){
	                    Toast.makeText(this,"Message size (" + size + " bytes) exceeds this tag's capacity of "+ ndef.getMaxSize() + " bytes.",Toast.LENGTH_LONG).show();
	                    return false;
	                }

	                ndef.writeNdefMessage(message);
	                Toast.makeText(this, "Pre-formatted Tag updated.", Toast.LENGTH_LONG).show();
	                return true;
	            } else{
	                NdefFormatable format = NdefFormatable.get(tag);
	                if (format != null) {
	                    try{
	                        format.connect();
	                        format.format(message);
	                        Toast.makeText(this,"Tag successfully formatted and updated.",Toast.LENGTH_LONG).show();
	                        return true;
	                    } catch (IOException e) {
	                        Toast.makeText(this,"Tap it once more(I/O Exception).",Toast.LENGTH_LONG).show();
	                        return false;
	                    }
	                } else{
	                    Toast.makeText(this, "Tag doesn't support NDEF.", Toast.LENGTH_LONG).show();
	                    return false;
	                }
	            }
	        } catch (Exception e){
	            Toast.makeText(this, "Sorry something's wrong :/.", Toast.LENGTH_LONG).show();
	        }
	        return false;
	    }

	    
	    
		private void checkNFC() {
			Boolean nfcEnabled = mNfcAdapter.isEnabled();
		        if (!nfcEnabled){
		            new AlertDialog.Builder(DEMO.this)
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
	}
