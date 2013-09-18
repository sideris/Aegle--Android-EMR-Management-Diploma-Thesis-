package com.PGSideris.HRecs;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.PGSideris.Handlers.Cuery;
import com.PGSideris.aegle.R;

public class Item extends Activity{
	protected SQLiteDatabase myDB=null;
	protected String name,table;
	protected int uid;
	protected Cursor cur;
	TextView yeart,year,itemname,comment,commentt,value,valuet,curr,currt;
	LinearLayout a;
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.herp);
		//getting data from the Intent
		name=getIntent().getStringExtra("name");
		uid=Integer.parseInt(getIntent().getStringExtra("uid"));
		table=getIntent().getStringExtra("table");
		//Setting TextViews
		itemname=(TextView) findViewById(R.id.itemName);itemname.setText(name);
		year=(TextView) findViewById(R.id.itemYear);
		yeart=(TextView) findViewById(R.id.year);
		comment=(TextView) findViewById(R.id.itemComments);
		commentt=(TextView) findViewById(R.id.comments);
		curr=(TextView) findViewById(R.id.itemcurrent);
		currt=(TextView) findViewById(R.id.current);
		value=(TextView) findViewById(R.id.itemValue);
		valuet=(TextView) findViewById(R.id.value);
		//quering for User info
		Cuery q = new Cuery(this);
		cur = q.fetchOption("SELECT * FROM "+table+" WHERE uid="+uid+" AND name='"+name+"'");
		
		/**
		  In the following code block we have various "if" where we see the history_go table 
		 and I hide accordingly the TextViews we do not want to show in each scenario. 
		 I also populate the TextViews we want
		 */
		if(table.contentEquals("user_med")){
			if(Integer.parseInt(cur.getString(cur.getColumnIndex("current")))==1){
				curr.setText("Currently Taking");
			}else{
				curr.setText("Not taking");
			}
			//hiding if not needed
			valuet.setVisibility(View.GONE);
			value.setVisibility(View.GONE);
			comment.setVisibility(View.GONE);
			commentt.setVisibility(View.GONE);
			year.setVisibility(View.GONE);
			yeart.setVisibility(View.GONE);
		}
		if(table.contentEquals("user_cond")){
			if(Integer.parseInt(cur.getString(cur.getColumnIndex("current")))==1){
				curr.setText("Currently under condition");
			}else{curr.setText("Cured/Receded");}
			
			if(Integer.parseInt(cur.getString(cur.getColumnIndex("year")))==0){
				year.setText("Not yet set");
			}else{
				year.setText(cur.getString(cur.getColumnIndex("year")));
			}
			valuet.setVisibility(View.GONE);
			value.setVisibility(View.GONE);
			comment.setVisibility(View.GONE);
			commentt.setVisibility(View.GONE);
		}
		if(table.contentEquals("user_test")){
			if(Integer.parseInt(cur.getString(cur.getColumnIndex("year")))==0){
				year.setText("Not yet set");
			}else{
				year.setText(cur.getString(cur.getColumnIndex("year")));
				}
			if(Float.parseFloat(cur.getString(cur.getColumnIndex("value")))==0){
				value.setText("No value");
			}else{
				value.setText(cur.getString(cur.getColumnIndex("value")));
				}
			if(cur.getString(cur.getColumnIndex("comments")).contentEquals("")){
				comment.setText("No comments");
			}else{
				comment.setText(cur.getString(cur.getColumnIndex("comments")));
				}
			curr.setVisibility(View.GONE);
			currt.setVisibility(View.GONE);
		}
		if(table.contentEquals("user_all")){
			valuet.setVisibility(View.GONE);
			value.setVisibility(View.GONE);
			comment.setVisibility(View.GONE);
			commentt.setVisibility(View.GONE);
			year.setVisibility(View.GONE);
			yeart.setVisibility(View.GONE);
			curr.setVisibility(View.GONE);
			currt.setVisibility(View.GONE);
		}
		if(table.contentEquals("user_vacc")){
			if(Integer.parseInt(cur.getString(cur.getColumnIndex("year")))==0){
				year.setText("Not yet set");
			}else{
				year.setText(cur.getString(cur.getColumnIndex("year")));
			}
			
			valuet.setVisibility(View.GONE);
			value.setVisibility(View.GONE);
			comment.setVisibility(View.GONE);
			commentt.setVisibility(View.GONE);
			curr.setVisibility(View.GONE);
			currt.setVisibility(View.GONE);
		}
		if(table.contentEquals("user_proc")){
			if(Integer.parseInt(cur.getString(cur.getColumnIndex("year")))==0){
				year.setText("Not yet set");
			}else{
				year.setText(cur.getString(cur.getColumnIndex("year")));
			}
			if(cur.getString(cur.getColumnIndex("comments")).contentEquals("")){
				comment.setText("No comments");
			}else{
				comment.setText(cur.getString(cur.getColumnIndex("comments")));
			}
			valuet.setVisibility(View.GONE);
			value.setVisibility(View.GONE);
			curr.setVisibility(View.GONE);
			currt.setVisibility(View.GONE);
		}
	}
}
