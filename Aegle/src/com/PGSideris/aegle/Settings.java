package com.PGSideris.aegle;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.PGSideris.HStaff.Doctors;
import com.PGSideris.Handlers.Cuery;
import com.PGSideris.Handlers.CustomHttpTask;
import com.PGSideris.Handlers.Database;
import com.PGSideris.Handlers.Server;
import com.PGSideris.NFC.NFCW;
import com.PGSideris.Permz.Permissions;

public class Settings extends Activity{
	protected SQLiteDatabase myDB=null;
	protected String name,last,email,uid;;
	boolean change=false;
	boolean ok=true;
	boolean empty=false;
	EditText sname,slast,semail,spwd;
	Button save;
	ActionBar actionBar;
	String[] actions = new String[] {"Settings","Doctors","Permissions"};
	Server server = new Server();
	
	protected void onCreate(Bundle savedInstanceState){ 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		//get data from intent
		name = getIntent().getStringExtra("name");
		last = getIntent().getStringExtra("last");
		email = getIntent().getStringExtra("email");
		uid = getIntent().getStringExtra("uid");
		
		//Assign variables to View's components
		sname= (EditText) findViewById(R.id.set_uname);
		slast= (EditText) findViewById(R.id.set_lname);
		semail= (EditText) findViewById(R.id.set_email);
		spwd= (EditText) findViewById(R.id.set_pwd);
		save= (Button) findViewById(R.id.settings_save);
		sname.setText(name);
		slast.setText(last);
		semail.setText(email);
		
		//Enable ActionBar and click actions. 
		actionBar = getActionBar();
    	add_apter();
	}
	
	protected void onResume(){
		super.onResume();
		//reset navigation items on Resume
    	actionBar.setSelectedNavigationItem(0);
    	//Enable Button clicks
		doclicks();
	}
	private void add_apter(){
		//setting ActionBar in List navigation and hide Application's title
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_dropdown_item, actions);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		//ActionBar "click" actions
		ActionBar.OnNavigationListener navigationListener = new OnNavigationListener() {
            public boolean onNavigationItemSelected(int itemPosition, long itemId) {
            	//navigate to action in clicked position
            	nav(itemPosition);
            	return false;
            }
		  };
		getActionBar().setListNavigationCallbacks(adapter, navigationListener);    
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	            // This is called when the Home (Up) button is pressed
	            // in the Action Bar.
	            Intent parentActivityIntent = new Intent(this, Welcome.class);
	            parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
	            startActivity(parentActivityIntent);
	            finish();
	            return true;
		 	case R.id.NFC:
				NfcManager manager = (NfcManager) getApplicationContext().getSystemService(Context.NFC_SERVICE);
				NfcAdapter adapter = manager.getDefaultAdapter();
		 		if (adapter != null ) {
		 		//create new intent
		    	 Intent nfc = new Intent(getApplicationContext(), NFCW.class);
		         // Close all views before launching logged
		    	 nfc.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		    	 nfc.putExtra("uid", uid);
		    	 nfc.putExtra("fullname", name+" "+last);
		         startActivity(nfc);
		 		}else{
		 			Toast.makeText(getApplicationContext(), "You do not have NFC", Toast.LENGTH_SHORT).show();
		 		}
		 		break;
		 	case R.id.logout:
				Database db = new Database(getApplicationContext());
				db.logout();
				finish();
		 		break;
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	 @Override
	 public boolean onCreateOptionsMenu(Menu menu) {
		 //use an inflater to populate the ActionBar with items
		 MenuInflater inflater = getMenuInflater();
		 inflater.inflate(R.menu.menu, menu);
		 return true;
	 }
	 
	private void nav(int go){
		//navigation by numbers
		//1=Doctors
		//2=Permissions
		if(go==1){
			//create new intent
	    	Intent doctors = new Intent(getApplicationContext(), Doctors.class);
	        // Close all views before launching logged
	    	 doctors.putExtra("uid", uid);
	    	 doctors.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    	 startActivity(doctors);
		}
		if(go==2){
			//create new intent
			Intent perms = new Intent(getApplicationContext(), Permissions.class);
			// Close all views before launching logged
			perms.putExtra("uid",uid);
			perms.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(perms);
		}
	}
	
	private void doclicks(){
		save.setOnClickListener(new View.OnClickListener() {
				
		public void onClick(View arg0) {
			Database db = new Database(getApplicationContext());
			//get Session to post for validity
			Cuery q = new Cuery(getApplicationContext());
			String session = q.getSess();
			//Create an ArrayList with nameValuePairs in order to add data to POST
			ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
			//add data to be POSTed in postParameters
			postParameters.add(new BasicNameValuePair("session_id",session));
			postParameters.add(new BasicNameValuePair("uid",uid));
           	//Initializing a response JSONObject
           	JSONObject response = null;
           	//check cases where we have change and then add them to POST list
			if(!sname.getText().toString().equals(name)){
				postParameters.add(new BasicNameValuePair("name", sname.getText().toString()));
				change=true;
			}
			if(!slast.getText().toString().equals(last)){
				postParameters.add(new BasicNameValuePair("last", slast.getText().toString()));
				change=true;
			}
			if(!semail.getText().toString().equals(email)){
				postParameters.add(new BasicNameValuePair("email", semail.getText().toString()));
				change=true;
			}
			if(!spwd.getText().toString().equals("")){
				postParameters.add(new BasicNameValuePair("password", spwd.getText().toString()));
				change=true;
			}
			if(sname.getText().toString().equals("")||slast.getText().toString().equals("")||semail.getText().toString().equals("")){
           		change=false;ok=false;empty=true;
				Toast.makeText(getApplicationContext(), "Do not leave empty fields", Toast.LENGTH_SHORT).show();
           	}
			if(change){
				try {
	       			CustomHttpTask asdf = new CustomHttpTask();
					response = asdf.execute(server.server+"changeset.php", postParameters).get();
					if(response.getString("success").equals("1")){
						String nam,las,emai;
						if(!response.isNull("name")){
							nam=response.getString("name");
							name=nam;
						}else{
							nam=name;
						}
						if(!response.isNull("last")){
							las=response.getString("last");
							last=las;
						}else{
							las=last;
						}
						if(!response.isNull("email")){
							emai=response.getString("email");
							email=emai;
						}else{
							emai=email;
						}
						//reset user data and remake
						db.resetUser();
						db.addUser(Integer.parseInt(uid), nam, las, emai, getIntent().getStringExtra("username"));
						ok=true;
						if(!response.isNull("warning")){
							if(response.getString("warning").equals("1")){
								Toast.makeText(getApplicationContext(), "Invalid e-mail", Toast.LENGTH_SHORT).show();
								ok=false;
							}
							if(response.getString("warning").equals("2") && !semail.getText().toString().equals(email)){
								Toast.makeText(getApplicationContext(), "E-mail already exists", Toast.LENGTH_SHORT).show();
								ok=false;
							}
						}
					}
					else{
						Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				}else{
					if(!empty){
						ok=false;
						ok=false;
						Toast.makeText(getApplicationContext(), "No changes detected!", Toast.LENGTH_SHORT).show();
					}
				}
				if(ok==true){
					Toast.makeText(getApplicationContext(), "Changes Saved!", Toast.LENGTH_SHORT).show();
					finish();
				}
			}}); 
 }

}
