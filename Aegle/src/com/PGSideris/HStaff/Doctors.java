package com.PGSideris.HStaff;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.PGSideris.Handlers.Cuery;
import com.PGSideris.Handlers.CustomHttpTask;
import com.PGSideris.Handlers.Database;
import com.PGSideris.Handlers.Server;
import com.PGSideris.Handlers.Sync;
import com.PGSideris.NFC.NFCW;
import com.PGSideris.Permz.Permissions;
import com.PGSideris.aegle.R;
import com.PGSideris.aegle.Settings;
import com.PGSideris.aegle.Welcome;


public class Doctors extends Activity{
	protected String uid;
	protected SQLiteDatabase myDB,myDB2=null;
	protected Cursor cursor=null,cursor2=null;
	private ArrayAdapter<String> listAdapter ;
	Button search;
	ListView list=null;
	String[] dids,actions = new String[] {"Doctors","Permissions"};
	ActionBar actionBar;
	Server server = new Server();
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_doctors);

		uid=getIntent().getStringExtra("uid");
		//Setting Views
		list= (ListView)findViewById(R.id.Doctors_list);
		list.setHapticFeedbackEnabled(true);
		search=(Button) findViewById(R.id.Se_doc_but);
		//Setting ActionBar and actions
		actionBar = getActionBar();
		add_apter();
		//populating with data the View and enabling actions for Clicks
		doclicks();
	}
	
	protected void onResume(){
		super.onResume();
		pulldata();
    	//set to the current item in ActioBar
    	actionBar.setSelectedNavigationItem(0);
	}
	
	private void add_apter(){
		Cuery q = new Cuery(this);
		cursor2 = q.fetchOption("SELECT * FROM user_login");//execute SQL query
		//ActionBar: No title, List mode
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_dropdown_item, actions);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		//ActionBar "Click" listener
		ActionBar.OnNavigationListener navigationListener = new OnNavigationListener() {
            public boolean onNavigationItemSelected(int itemPosition, long itemId) {
            	//navigate to the # of the clicked child
            	nav(itemPosition);
            	return false;
            }
		  };
		//append adapter and listener to ActionBar
		getActionBar().setListNavigationCallbacks(adapter, navigationListener);    
	}
	
	private void nav(int go){
		//navigation by numbers(4 is always home)
		//1=Permissions
		if(go==1){
			//create new intent
	    	Intent perms = new Intent(getApplicationContext(), Permissions.class);
	        // Close all views before launching logged
	    	perms.putExtra("uid",uid);
	    	perms.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    	startActivity(perms);
		}
	}
	
	private void pulldata(){
		//selecting all Doctors
		Cuery q = new Cuery(this);
		cursor=q.fetchOption("SELECT * FROM doctor");
		ArrayList<String> itemlist = new ArrayList<String>();  
		String[] names=null;
		//array with ids of docs
		dids=new String[cursor.getCount()];
		
		if (cursor.getCount()==0) {
			//if no doctors
			names = new String[] { "You have not added any doctors yet"};
        }else{//initialize the names array
        	if(!cursor.getString(cursor.getColumnIndex("field")).equals("null")){
        		names=new String[] {cursor.getString(cursor.getColumnIndex("first"))+" "+cursor.getString(cursor.getColumnIndex("last"))+"\nfield: "+cursor.getString(cursor.getColumnIndex("field"))};
        		dids[0]= cursor.getString(cursor.getColumnIndex("id"));
        	}else{
        		names=new String[] {cursor.getString(cursor.getColumnIndex("first"))+" "+cursor.getString(cursor.getColumnIndex("last"))};
        		dids= new String[] {cursor.getString(cursor.getColumnIndex("id"))};
        	}
        }
		//add all the names to the ArrayList(something like a dynamic array, but more flexible)
		itemlist.addAll( Arrays.asList(names) ); 
		//create arrayAdapter with ArrayList and as View = "item.xml"
		listAdapter = new ArrayAdapter<String>(this, R.layout.item, itemlist); 
		 
		//if results add the rest
		if(cursor.getCount()!=0){
			 for(int i=0;i<(cursor.getCount()-1);i++){
				 cursor.moveToNext();
				 dids[i+1]=cursor.getString(cursor.getColumnIndex("id"));
				 if(!cursor.getString(cursor.getColumnIndex("field")).equals("null")){
					 listAdapter.add(cursor.getString(cursor.getColumnIndex("first"))+" "+cursor.getString(cursor.getColumnIndex("last"))+"\nfield: "+cursor.getString(cursor.getColumnIndex("field")));
				 }else{
					 listAdapter.add(cursor.getString(cursor.getColumnIndex("first"))+" "+cursor.getString(cursor.getColumnIndex("last")));
		        }
			 }
		 }
		 // Set the ArrayAdapter as the ListView's adapter.  
		 list.setAdapter(listAdapter); 
	}
	//the Actionbar and respective actions
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	            // This is called when the Home (Up) button is pressed in the Action Bar.
	            Intent parentActivityIntent = new Intent(this, Welcome.class);
	            parentActivityIntent.addFlags(
	                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
	                    Intent.FLAG_ACTIVITY_NEW_TASK);
	            startActivity(parentActivityIntent);
	            finish();
	            return true;
			case R.id.item_Synchronise:
		 		Sync sync = new Sync(getApplicationContext());
		 		sync.user();
		 		sync.others();
				break;
		 	case R.id.item_settings:
		 		//create new intent
		 		Intent settings = new Intent(getApplicationContext(), Settings.class);
		 		// Close all views before launching logged
		 		settings.putExtra("name", cursor2.getString(cursor2.getColumnIndex("name")));
		 		settings.putExtra("last", cursor2.getString(cursor2.getColumnIndex("last")));
		 		settings.putExtra("email", cursor2.getString(cursor2.getColumnIndex("email")));
		 		settings.putExtra("uid", cursor2.getString(cursor2.getColumnIndex("uid")));
		 		settings.putExtra("username", cursor2.getString(cursor2.getColumnIndex("username")));
		 		settings.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		 		startActivity(settings);
		 		break;
		 	case R.id.NFC:
				NfcManager manager = (NfcManager) getApplicationContext().getSystemService(Context.NFC_SERVICE);
				NfcAdapter adapter = manager.getDefaultAdapter();
		 		if (adapter != null ) {
		 		//create new intent
		    	 Intent nfc = new Intent(getApplicationContext(), NFCW.class);
		         // Close all views before launching logged
		    	 nfc.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		    	 nfc.putExtra("uid", cursor2.getString(cursor2.getColumnIndex("uid")));
		    	 nfc.putExtra("fullname", cursor2.getString(cursor2.getColumnIndex("name"))+" "+cursor2.getString(cursor2.getColumnIndex("last")));
		         startActivity(nfc);
		 		}else{
		 			Toast.makeText(getApplicationContext(), "You do not have NFC", Toast.LENGTH_SHORT).show();
		 		}
		 		break;
		 	case R.id.logout:
				Database db = new Database(getApplicationContext());
				db.logout();
			    //finish the welcome activity
				Intent intent = new Intent(getApplicationContext(), Welcome.class);
			    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			    intent.putExtra("EXIT", "yes");
			    startActivity(intent);
			    //finish this activity
			    finish();
		 		break;
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	 //inflate the menu
	@Override
	 public boolean onCreateOptionsMenu(Menu menu) {
		 //use an inflater to populate the ActionBar with items
		 MenuInflater inflater = getMenuInflater();
		 inflater.inflate(R.menu.menu, menu);
		 return true;
	 }
	
	
	private void doclicks(){
		final Database openHelper2 = new Database(this);
		//set ItemClick Listener
		list.setOnItemLongClickListener(new OnItemLongClickListener(){
			//do things on LongClick
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,int arg2, long arg3) {
				final String alo=dids[arg2];
				arg1.performHapticFeedback(0);
				//Creating alert Dialog
				new AlertDialog.Builder(arg0.getContext())
			    .setTitle("Delete")
			    .setMessage("Delete Doctor?(removes rights)")
			    //On YES click actions to do
			    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int which) { 
						//get Session to post for validity
						Cuery q = new Cuery(getApplicationContext());
						String session = q.getSess();
			        	// continue with delete
				        ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
				        postParameters.add(new BasicNameValuePair("session_id",session));
				        postParameters.add(new BasicNameValuePair("did",alo));
				        postParameters.add(new BasicNameValuePair("uid",uid));
				        postParameters.add(new BasicNameValuePair("kind","delete"));
						
				        JSONObject response = null;
				        try {
			        		CustomHttpTask asdf = new CustomHttpTask();
							response = asdf.execute(server.server+"add_del_doc.php", postParameters).get();
							
							if(response.getString("success").equals("1")){
								myDB2 = openHelper2.getReadableDatabase(); 
								myDB2=SQLiteDatabase.openDatabase("data/data/com.PGSideris.aegle/databases/aeglea", null, SQLiteDatabase.OPEN_READWRITE);
								//delete the doctor for the list and the the related permissions
								myDB2.delete("doctor","id="+alo, null);
								myDB2.delete("permission","did="+alo,null);
								//refresh the views
								pulldata();
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
			     //on No do nothing
			    .setNegativeButton("No", new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int which) { 
			        	//do nothing noob
			        }}).show();
				return true;
			}
		 });
		
		 search.setOnClickListener(new OnClickListener(){
				private Database db = new Database(getApplicationContext());
			 
				public void onClick(View arg0) {
					db.resetSearch();
					//create new intent
	    	   		Intent search = new Intent(getApplicationContext(), SearchD.class);
	                // Close all views before launching logged
	    	   		search.putExtra("uid", uid);
	    	   		search.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    	   		startActivity(search);
	                // Close Login Screen
				}});
			 list.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
						String alo=dids[arg2];
						//create new intent
		    	   		Intent doc = new Intent(getApplicationContext(), Doc.class);
		                // Close all views before launching logged
		    	   		doc.putExtra("did", alo);
		    	   		doc.putExtra("tavle", "doctor");
		    	   		doc.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		    	   		startActivity(doc);
		                // Close Login Screen
					}
				  });
	}
}
