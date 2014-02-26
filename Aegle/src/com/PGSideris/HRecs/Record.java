package com.PGSideris.HRecs;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import aegle.db.Cuery;
import aegle.db.Database;
import aegle.main.Settings;
import aegle.main.Welcome;
import aegle.web.CustomHttpTask;
import aegle.web.Server;
import aegle.web.Sync;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.PGSideris.HStaff.Doctors;
import com.PGSideris.NFC.NFCW;
import com.PGSideris.Permz.Permissions;
import com.PGSideris.aegle.R;

public class Record extends Activity{
	private ArrayAdapter<String> listAdapter ;
	protected SQLiteDatabase myDB=null;
	TextView title=null;
	Cursor cur2=null,cursor=null,cursor2=null;
	ListView list=null;
	String user_id,uid,table,norton;
	ActionBar actionBar;
	String[] fids,actions = new String[] {"Record","Doctors","Permissions"};
	Server server = new Server();
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record);
		//setting Views
		title = (TextView) findViewById(R.id.recordTitle);
		list = (ListView) findViewById(R.id.listView1);
		list.setHapticFeedbackEnabled(true);
		//gettting data from Intent
		title.setText(getIntent().getStringExtra("title"));
		norton=getIntent().getStringExtra("norton");
		table=getIntent().getStringExtra("table");
		uid = getIntent().getStringExtra("uid");
		//managin Handlers
		actionBar = getActionBar();
		add_apter();

	}
	
	protected void onResume(){
		super.onResume();
    	actionBar.setSelectedNavigationItem(0);
		//take data for user
		Cuery q = new Cuery(this);
		cursor = q.fetchOption("SELECT * FROM user_login");//use above to execute SQL query
		//more handlers
		pulldata();
		doclicks();
	}
	
	private void add_apter(){
		//ActionBar->no title, List mode
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_dropdown_item, actions);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		//creating "click" listener for list
		ActionBar.OnNavigationListener navigationListener = new OnNavigationListener() {
            public boolean onNavigationItemSelected(int itemPosition, long itemId) {
            	nav(itemPosition);
            	return false;
            }
		  };
		 //appending adapter(data to populate) and the Listener to the ActionBar
		getActionBar().setListNavigationCallbacks(adapter, navigationListener);    
	}
	
	
	private void pulldata(){

		ArrayList<String> itemlist = new ArrayList<String>();  
		String[] names=null;
		
		//do query to select from the table we want
		Cuery q = new Cuery(getApplicationContext());
		cur2=q.fetchOption("SELECT * FROM "+getIntent().getStringExtra("table"));
		
		fids= new String[cur2.getCount()];
		//check for results
		if (cur2.getCount()==0) {
			//if we don't have anything
			names = new String[] { "Nothing Added here. Go to the site to add more."}; 
        }else{
        	fids[0]= cur2.getString(cur2.getColumnIndex("id"));
        	names = new String[] {cur2.getString(cur2.getColumnIndex("name"))};
        	user_id=cur2.getString((cur2.getColumnIndex("uid")));
        }
		 //add the array as list to the ArrayList
		 itemlist.addAll( Arrays.asList(names) ); 
		 listAdapter = new ArrayAdapter<String>(this, R.layout.item, itemlist); 
		 //if results add the rest
		 if(cur2.getCount()!=0){
			 for(int i=0;i<(cur2.getCount()-1);i++){
				cur2.moveToNext();
				listAdapter.add(cur2.getString(cur2.getColumnIndex("name"))); 
	        	fids[i+1] = cur2.getString(cur2.getColumnIndex("id"));
			 }
		 }
		 // Set the ArrayAdapter as the ListView's adapter.  
		 list.setAdapter(listAdapter); 
		 //remove the navigation history
		 cur2.close();
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
			case R.id.item_Synchronise:
		 		Sync sync = new Sync(getApplicationContext());
		 		sync.user();
		 		sync.others();
				break;
		 	case R.id.item_settings:
		 		//create new intent
		 		Intent settings = new Intent(getApplicationContext(), Settings.class);
		 		// Close all views before launching logged
		 		settings.putExtra("name", cursor.getString(cursor.getColumnIndex("name")));
		 		settings.putExtra("last", cursor.getString(cursor.getColumnIndex("last")));
		 		settings.putExtra("email", cursor.getString(cursor.getColumnIndex("email")));
		 		settings.putExtra("uid", cursor.getString(cursor.getColumnIndex("uid")));
		 		settings.putExtra("username", cursor.getString(cursor.getColumnIndex("username")));
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
			    	 nfc.putExtra("uid", cursor.getString(cursor.getColumnIndex("uid")));
			    	 nfc.putExtra("fullname", cursor.getString(cursor.getColumnIndex("name"))+" "+cursor.getString(cursor.getColumnIndex("last")));
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
	
	 @Override
	 public boolean onCreateOptionsMenu(Menu menu) {
		 //use an inflater to populate the ActionBar with items
		 MenuInflater inflater = getMenuInflater();
		 inflater.inflate(R.menu.menu, menu);
		 return true;
	 }
	 
	private void nav(int go){
		//navigation by numbers(4 is always home)
		//1=doctors
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
		//set Click Listener on item
		 list.setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
					//create new intent
	    	   		Intent item = new Intent(getApplicationContext(), Item.class);
	                //we add data to pass to the Item Activity
	    	   		item.putExtra("name", ((TextView)arg1).getText());
	    	   		item.putExtra("uid", user_id);
	    	   		item.putExtra("table", table);
	    	   		// Close all views before launching logged
	    	   		item.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    	   		startActivity(item);
				}
			  });
		 list.setOnItemLongClickListener(new OnItemLongClickListener(){

			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,int arg2, long arg3) {
	            final Database openHelper = new Database(getApplicationContext());
				final int pos=arg2;
				arg1.performHapticFeedback(0);

	    		new AlertDialog.Builder(arg0.getContext())
			    .setTitle("Delete")
			    .setMessage("Delete record?")
			    //On YES click actions to do
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) { 
						//getting Session to post
						Cuery q = new Cuery(getApplicationContext());
						String session = q.getSess();
						//adding Parameters to POST
						ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
			        	postParameters.add(new BasicNameValuePair("session_id", session));
			        	postParameters.add(new BasicNameValuePair("fileid", fids[pos]));
			        	postParameters.add(new BasicNameValuePair("uid",user_id));
			            postParameters.add(new BasicNameValuePair("norton",norton));
			            JSONObject response = null;	
			            try {
			        		CustomHttpTask asdf = new CustomHttpTask();
							response = asdf.execute(server.server+"del_hisItem.php", postParameters).get();
							if(response.getString("success").equals("1")){
								//if valid result open DB and Delete
								myDB = openHelper.getReadableDatabase(); 
								myDB=SQLiteDatabase.openDatabase("data/data/com.PGSideris.aegle/databases/aeglea", null, SQLiteDatabase.OPEN_READWRITE);
								myDB.delete(table,"id="+fids[pos], null);
								myDB.delete("permission", "type='"+norton+"' AND fileid="+fids[pos], null);
								pulldata();
							}
						}catch(Exception e){
								Log.e("On delete","THIS "+e);
						}
					}
				 })
				     //on "No" do nothing
				 .setNegativeButton("No", new DialogInterface.OnClickListener() {
				       public void onClick(DialogInterface dialog, int which) { 
				        	//do nothing noob
				        }}).show();
	    		return true;
			}
			 
		 });
	}
	
}
