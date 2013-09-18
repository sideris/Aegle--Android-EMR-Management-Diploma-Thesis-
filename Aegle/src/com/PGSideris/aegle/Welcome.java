package com.PGSideris.aegle;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.PGSideris.HRecs.Record;
import com.PGSideris.HStaff.Doctors;
import com.PGSideris.Handlers.Cuery;
import com.PGSideris.Handlers.Database;
import com.PGSideris.Handlers.Sync;
import com.PGSideris.NFC.NFCW;
import com.PGSideris.Permz.Permissions;

public class Welcome extends Activity{
	private Cursor cursor;
	private String uid;
	protected SQLiteDatabase myDB=null;
	public TextView msg;
	Button hr,docs,perm,NFC;
	ProgressBar pb;
	ImageButton tes,con,al,me,pr,va;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
		msg = (TextView) findViewById(R.id.msg);
		NFC = (Button) findViewById(R.id.NFC);
		docs = (Button) findViewById(R.id.HProfessionalsButton);
		perm = (Button) findViewById(R.id.PermissionsButton);
		// setting corresponding buttons
		tes = (ImageButton) findViewById(R.id.testbut);
		con = (ImageButton) findViewById(R.id.condbut);
		al = (ImageButton) findViewById(R.id.albut);
		me = (ImageButton) findViewById(R.id.medbut);
		pr = (ImageButton) findViewById(R.id.procbut);
		va = (ImageButton) findViewById(R.id.vacbut);
		pb = (ProgressBar) findViewById(R.id.Main_PB);
		// manage action bar and enable clicks
		doclicks();
	}
	 
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// use an inflater to populate the ActionBar with items
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	 protected void onResume(){
		 super.onResume();
		 
		 if (getIntent().getStringExtra("EXIT") != null) {
			 finish();
		 }else{
			 Cuery q = new Cuery(getApplicationContext());
			 cursor = q.fetchOption("SELECT * FROM user_login");//use above to execute SQL query
			 uid = cursor.getString(cursor.getColumnIndex("uid"));
			 msg.setText("Username: "+cursor.getString(cursor.getColumnIndex("username"))
					 +"\nFull name: "+cursor.getString(cursor.getColumnIndex("name"))+" "+cursor.getString(cursor.getColumnIndex("last"))
					 +"\ne-mail: "+cursor.getString(cursor.getColumnIndex("email"))
					 +"\nAeglea id:"+cursor.getString(cursor.getColumnIndex("uid")));
		 }
	 }

	 protected void onStop(){
		 super.onStop();
	 }
	 
	 protected void onDestroy(){
		 super.onDestroy();
		 Database db = new Database(getApplicationContext());
		 Ac removed = new Ac();
		 if(msg.isShown()){
			 db.resetTables();
		 }
		 if(removed.delete){
			 Log.e("USER","Removed via task");
			 db.resetTables();
		 }
	 }
	
	 public void onBackPressed(){
		 super.onBackPressed();
		 Log.e("BACK PRESSED",":/");
		 Database db = new Database(getApplicationContext());
		 db.resetTables();
	 }
		
	 @Override
	 public boolean onOptionsItemSelected(MenuItem item){
		 // same as using a normal menu
		 switch(item.getItemId()) {
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
		 		settings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
		 		startActivity(settings);
		 		break;
		 		
		 	case R.id.NFC:
				 NfcManager manager = (NfcManager) getApplicationContext().getSystemService(Context.NFC_SERVICE);
				 NfcAdapter adapter = manager.getDefaultAdapter();
		 		if (adapter != null ) {
			 		//create new intent
			    	 Intent nfc = new Intent(getApplicationContext(), NFCW.class);
			         // Close all views before launching logged
			    	 nfc.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
				finish();
		 		break;
	    }
		 return true;
	 }
	private void doclicks() {

		perm.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// create new intent
				Intent perms = new Intent(getApplicationContext(), Permissions.class);
				// Close all views before launching logged
				perms.putExtra("uid",cursor.getString(cursor.getColumnIndex("uid")));
				perms.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(perms);
			}
		});
		docs.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				// create new intent
				Intent doctors = new Intent(getApplicationContext(), Doctors.class);
				// Close all views before launching logged
				doctors.putExtra("uid",	cursor.getString(cursor.getColumnIndex("uid")));
				doctors.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(doctors);
			}
		});
		tes.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				// create new intent
				Intent record = new Intent(getApplicationContext(),	Record.class);
				record.putExtra("title", "Tests");
				record.putExtra("table", "user_test");
				record.putExtra("norton", "test");
				record.putExtra("uid", uid);
				// Close all views before launching logged
				record.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(record);
			}
		});

		con.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				// create new intent
				Intent record = new Intent(getApplicationContext(),	Record.class);
				record.putExtra("title", "Medical Conditions");
				record.putExtra("table", "user_cond");
				record.putExtra("norton", "condition");
				record.putExtra("uid", uid);
				// Close all views before launching logged
				record.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(record);
			}
		});

		 al.setOnClickListener(new View.OnClickListener() {
			 public void onClick(View arg0) {
				 //create new intent
				Intent record = new Intent(getApplicationContext(),
						Record.class);
				record.putExtra("title", "Allergies");
				record.putExtra("table", "user_all");
				record.putExtra("norton", "allergy");
				record.putExtra("uid", uid);
				// Close all views before launching logged
				record.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(record);
				// Close Login Screen
				onPause();
			}
		});
		me.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				// create new intent
				Intent record = new Intent(getApplicationContext(),
						Record.class);
				record.putExtra("title", "Medication");
				record.putExtra("table", "user_med");
				record.putExtra("norton", "medication");
				record.putExtra("uid", uid);
				// Close all views before launching logged
				record.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(record);
				// Close Login Screen
				onPause();
			}
		});
		pr.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				// create new intent
				Intent record = new Intent(getApplicationContext(),
						Record.class);
				record.putExtra("title", "Medical Procedures");
				record.putExtra("table", "user_proc");
				record.putExtra("norton", "procedure");
				record.putExtra("uid", uid);
				// Close all views before launching logged
				record.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(record);
				// Close Login Screen
				onPause();
			}
		});
		va.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				// create new intent
				Intent record = new Intent(getApplicationContext(),	Record.class);
				record.putExtra("title", "Vaccinations");
				record.putExtra("table", "user_vacc");
				record.putExtra("norton", "vaccine");
				record.putExtra("uid", uid);
				// Close all views before launching logged
				record.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(record);
				// Close Login Screen
				onPause();
			}
		});
	}
}
class Ac extends Service{
	public boolean delete=false;
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void onTaskRemoved (Intent rootIntent){
		delete=true;
	}
}
