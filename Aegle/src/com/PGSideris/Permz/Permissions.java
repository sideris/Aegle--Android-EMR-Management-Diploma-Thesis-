package com.PGSideris.Permz;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;

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
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.PGSideris.HStaff.Doctors;
import com.PGSideris.NFC.NFCW;
import com.PGSideris.aegle.R;

public class Permissions extends Activity{
	
	protected String uid,did;
	protected String[] names=null,dids=null;
	protected Cursor pdoc,perm=null;
	protected SQLiteDatabase myDB=null;
	private ArrayAdapter<String> listAdapter;
	private Cursor cursor;
	ArrayList<String> itemlist = new ArrayList<String>();
	ArrayList<String> idlist = new ArrayList<String>();	
	ListView list=null;
	String[] actions = new String[] {"Permissions","Doctors"};
	ActionBar actionBar;
	Button adperm;
	View mainView;
	Server server = new Server();
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_permissions);
		//make Views and get intent data
		list = (ListView)findViewById(R.id.perm_docs_list);
		list.setHapticFeedbackEnabled(true);
		adperm = (Button) findViewById(R.id.add_perms);
		uid=getIntent().getStringExtra("uid");
		//set ActioBar and enable clicks
		actionBar = getActionBar();
		add_apter();
	}
	
	protected void onResume(){
		super.onResume();
		//set to the current item in ActioBar
    	actionBar.setSelectedNavigationItem(0);
    	if(names!=null){
    		listAdapter.clear();
    	}
    	//fetch data and enable clicks
    	getdata();
		doclicks();
	}

	private void add_apter(){
		Cuery q = new Cuery(this);
		cursor = q.fetchOption("SELECT * FROM user_login");//use above to execute SQL query
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_dropdown_item, actions);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		ActionBar.OnNavigationListener navigationListener = new OnNavigationListener() {
            public boolean onNavigationItemSelected(int itemPosition, long itemId) {
            	//enable actions for selected item and reset navigation to this if return here
            	nav(itemPosition);
            	return false;
            }
		  };
		getActionBar().setListNavigationCallbacks(adapter, navigationListener);    
	}
	
	private void nav(int go){
		//navigation by numbers
		//1=Doctors
		if(go==1){
			Intent doctors = new Intent(getApplicationContext(), Doctors.class);
	        // Close all views before launching logged
	    	doctors.putExtra("uid", uid);
	    	doctors.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        startActivity(doctors);
		}
	}
	
	private void getdata(){
		Cuery q = new Cuery(this);

		LinkedHashSet<String> hs = new LinkedHashSet<String>();
		LinkedHashSet<String> hs2 = new LinkedHashSet<String>();
		perm=q.fetchOption("SELECT * FROM permission");
		
		if(perm.getCount()>0){
			//for the first records
			pdoc=q.fetchOption("SELECT * FROM doctor WHERE id="+perm.getString(perm.getColumnIndex("did")));
			names=new String[] {pdoc.getString(pdoc.getColumnIndex("first"))+" "+pdoc.getString(pdoc.getColumnIndex("last"))};
			dids = new String[]{pdoc.getString(pdoc.getColumnIndex("id"))};
			
			itemlist.addAll( Arrays.asList(names) ); //adding to the 2 Array lists
			idlist.addAll(Arrays.asList(dids));
			 
			for(int i=1;i<perm.getCount();i++){
				perm.moveToNext();
				pdoc = q.fetchOption("SELECT * FROM doctor WHERE id="+perm.getString(perm.getColumnIndex("did")));
				idlist.add(pdoc.getString(pdoc.getColumnIndex("id")));
				itemlist.add(pdoc.getString(pdoc.getColumnIndex("first"))+" "+pdoc.getString(pdoc.getColumnIndex("last"))); //filling aray lists
			}
			hs.addAll(itemlist); 
			hs2.addAll(idlist); //parsing arraylists sto Hash Sets(linked in order to not list order)
			itemlist.clear(); 
			idlist.clear();
			itemlist.addAll(hs); 
			idlist.addAll(hs2); //parsing unique data again to arraylists

			listAdapter = new ArrayAdapter<String>(this, R.layout.item, itemlist);//set item.xml as the screen for each item for itemlist
			list.setAdapter(listAdapter);	 // Set the ArrayAdapter as the ListView's adapter.  
		}else{
			names = new String[] { "No permissions given"}; 
			itemlist.addAll( Arrays.asList(names) ); 
			listAdapter = new ArrayAdapter<String>(this, R.layout.item, itemlist);
			// Set the ArrayAdapter as the ListView's adapter.  
			list.setAdapter(listAdapter); 
		}
	}
	
	
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
	
	
	public void doclicks(){
		
		adperm.setOnClickListener(new OnClickListener(){

			public void onClick(View arg0) {
				Intent add = new Intent(getApplicationContext(), GPerm.class);
				add.putExtra("uid", uid);
				add.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	   		startActivity(add);
			}
			
		});
		
		list.setOnItemClickListener(new OnItemClickListener() {
		
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
				String did2 = idlist.get(arg2).toString();
				String doc_nam = itemlist.get(arg2).toString();
				//create new intent
    	   		Intent sdoc_perm = new Intent(getApplicationContext(), DocPerms.class);
                // Close all views before launching logged
    	   		sdoc_perm.putExtra("uid", uid);
    	   		sdoc_perm.putExtra("did", did2);
    	   		sdoc_perm.putExtra("dname",doc_nam);
    	   		sdoc_perm.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	   		startActivity(sdoc_perm);
			}
		  });
		list.setOnItemLongClickListener(new OnItemLongClickListener(){
            final Database openHelper = new Database(getApplicationContext());
			
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				did = idlist.get(arg2).toString();
				final int pos=arg2;
				arg1.performHapticFeedback(0);
				new AlertDialog.Builder(arg0.getContext())
			    .setTitle("Remove ALL rights")
			    .setMessage("Remove?")
			    //on positive choice
			    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) { 
						//get Session to post for validity
						Cuery q = new Cuery(getApplicationContext());
						String session = q.getSess();
						//Create an ArrayList with nameValuePairs in order to add data to POST
			            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
			        	postParameters.add(new BasicNameValuePair("session_id",session));
			            postParameters.add(new BasicNameValuePair("uid",uid));
			            postParameters.add(new BasicNameValuePair("did",idlist.get(pos).toString()));
			            postParameters.add(new BasicNameValuePair("delete","doctor"));
			            JSONObject response = null;	
			            try {
			        		CustomHttpTask asdf = new CustomHttpTask();
							response = asdf.execute(server.server+"del_perm.php", postParameters).get();
							if(response.getString("success").equals("1")){
								myDB = openHelper.getReadableDatabase(); 
								myDB=SQLiteDatabase.openDatabase("data/data/com.PGSideris.aegle/databases/aeglea", null, SQLiteDatabase.OPEN_READWRITE);
								myDB.delete("permission", "did="+Integer.parseInt(idlist.get(pos).toString()), null);
								listAdapter.clear();
								getdata();
							}
						}catch(Exception e){
								Log.e("Doctor Perm Delete",""+e);
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
	}
}
