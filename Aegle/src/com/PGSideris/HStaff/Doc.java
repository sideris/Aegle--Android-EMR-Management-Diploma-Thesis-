package com.PGSideris.HStaff;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;

import com.PGSideris.Handlers.Cuery;
import com.PGSideris.Handlers.Database;
import com.PGSideris.aegle.R;

public class Doc extends Activity{

	protected SQLiteDatabase myDB=null;
	protected Cursor c;
	protected int did;
	protected String tavle;
	TextView title,email,field,place,phone;
	String ex=""; //string to annotate if the Doctor is already added
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_doc);
		//getting data from Intent
		did=Integer.parseInt(getIntent().getStringExtra("did"));
		tavle=getIntent().getStringExtra("tavle");
		//linking TextViews with variables
		title = (TextView) findViewById(R.id.doc_fullname);
		email = (TextView) findViewById(R.id.doc_email2);
		field = (TextView) findViewById(R.id.field);
		place = (TextView) findViewById(R.id.place);
		phone = (TextView) findViewById(R.id.phone);
		//new Database onject
		Database openHelper = new Database(this);
		//Getting Doctors(query)
		Cuery q = new Cuery(this);
		c=q.fetchOption("SELECT * FROM "+tavle+" WHERE id="+did);
		//If we are from a Search and the Doctor is already added then set the annotation to (Added) 
		if(openHelper.AddedDoctor(did) && tavle.equals("result_doc")){
			ex="(Added)"; 
		}
		//populating with data
		title.setText(c.getString(c.getColumnIndex("first"))+" "+c.getString(c.getColumnIndex("last"))+ex);
		email.setText(c.getString(c.getColumnIndex("email")));
		
		if(c.getString(c.getColumnIndex("field")).equals("null")){
			field.setText("Not set");
		}else{
			field.setText(c.getString(c.getColumnIndex("field")));
		}
		
		if(c.getString(c.getColumnIndex("place")).equals("null")){
			place.setText("Not set");
		}else{
			place.setText(c.getString(c.getColumnIndex("place")));
		}
		
		if(c.getString(c.getColumnIndex("phone")).equals("null")){
			phone.setText("Not set");
		}else{
			phone.setText(c.getString(c.getColumnIndex("phone")));
		}
	}
}
