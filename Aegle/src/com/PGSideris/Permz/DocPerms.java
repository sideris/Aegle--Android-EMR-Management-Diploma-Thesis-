package com.PGSideris.Permz;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import aegle.db.Cuery;
import aegle.db.Database;
import aegle.web.CustomHttpTask;
import aegle.web.Server;
import android.app.ExpandableListActivity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.TextView;

import com.PGSideris.aegle.R;

public class DocPerms extends ExpandableListActivity{
	protected int uid,did;
	protected String dname;
	protected SQLiteDatabase myDB=null,myDB2=null;
	ExpandableListAdapter mAdapter;
	String[][] ch = null,chid = null;
	TextView dnam,dfield;
	Cursor doc = null;
	Server server = new Server();
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
	}

	protected void onResume(){
		super.onResume();
		//populate with data
		popu();
	}
	
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
       //This sets the menu after a long click
    	menu.setHeaderTitle("Remove Permission");
        menu.add(0, 0, 0, "Remove");
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	//setting an ExpandableList Menu
    	ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
		String title = ((TextView) info.targetView).getText().toString();
		//helper Array
		String[] sync = {"test","condition","medication","procedure","vaccine","allergy"};
        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
        //if long click on a child
        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
        	Database openHelper = new Database(this);
     		myDB = openHelper.getReadableDatabase(); 
     		myDB=SQLiteDatabase.openDatabase("data/data/com.PGSideris.aegle/databases/aeglea", null, SQLiteDatabase.OPEN_READWRITE);
            //take the group and child position of the clicked item
     		int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition); 
            int childPos = ExpandableListView.getPackedPositionChild(info.packedPosition); 
            //if we have none permission then nothing else do bellow
            if(!title.equals("None.")){
				//get Session to post for validity
				Cuery q = new Cuery(getApplicationContext());
				String session = q.getSess();
				//Create an ArrayList with nameValuePairs in order to add data to POST
	            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
	        	postParameters.add(new BasicNameValuePair("session_id",session));
	        	postParameters.add(new BasicNameValuePair("did",getIntent().getStringExtra("did")));
	        	postParameters.add(new BasicNameValuePair("uid",getIntent().getStringExtra("uid")));
	            postParameters.add(new BasicNameValuePair("fileid",chid[groupPos][childPos]));
	            postParameters.add(new BasicNameValuePair("type",sync[groupPos]));
	            postParameters.add(new BasicNameValuePair("delete","specific"));
	            JSONObject response = null;
	            
	           try {
	        	   //post to server the deletion and delete from local SQLite
	    			CustomHttpTask asdf = new CustomHttpTask();
					response = asdf.execute(server.server+"del_perm.php", postParameters).get();
					if(response.getString("success").equals("1")){
						myDB.execSQL("DELETE FROM permission WHERE did="+did+" AND type='"+sync[groupPos]+"' AND fileid="+chid[groupPos][childPos]);
						 ((TextView) info.targetView).setBackgroundColor(4444);
						  popu();
					}
	        	} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				myDB.close();
	            return true;
            }else{
            	return false;
            }
        } else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {//if long click to a category remove all rights
            int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
            Database openHelper = new Database(this);
    		myDB2 = openHelper.getReadableDatabase(); 
    		myDB2 =SQLiteDatabase.openDatabase("data/data/com.PGSideris.aegle/databases/aeglea", null, SQLiteDatabase.OPEN_READWRITE);
            
			//get Session to post for validity
			Cuery q = new Cuery(getApplicationContext());
			String session = q.getSess();
			//Create an ArrayList with nameValuePairs in order to add data to POST
            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        	postParameters.add(new BasicNameValuePair("session_id",session));
        	postParameters.add(new BasicNameValuePair("did",getIntent().getStringExtra("did")));
        	postParameters.add(new BasicNameValuePair("uid",getIntent().getStringExtra("uid")));
            postParameters.add(new BasicNameValuePair("type",sync[groupPos]));
            postParameters.add(new BasicNameValuePair("delete","group"));
            JSONObject response = null;
            
	           try {
	    			CustomHttpTask asdf = new CustomHttpTask();
					response = asdf.execute(server.server+"del_perm.php", postParameters).get();
					if(response.getString("success").equals("1")){
						myDB2.execSQL("DELETE FROM permission WHERE did="+did+" AND type='"+sync[groupPos]+"'");
						popu();
					}
	        	} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
            myDB2.close();
            return true;
        }
        return false;
    }
	
    private void popu(){
    	//append data and activate the adapter
    	mAdapter = new MyExpandableListAdapter();
        setListAdapter(mAdapter);
        registerForContextMenu(getExpandableListView());
        getExpandableListView().setBackgroundResource(R.drawable.back);
        //get data from intent
        dname = getIntent().getStringExtra("dname");
        uid = Integer.parseInt(getIntent().getStringExtra("uid"));
		did = Integer.parseInt(getIntent().getStringExtra("did"));
		
		ch = ((MyExpandableListAdapter) mAdapter).gat();//return children names
		chid = ((MyExpandableListAdapter) mAdapter).gid();//retun all children id
		
    }
    
	public class MyExpandableListAdapter extends BaseExpandableListAdapter {
       //this class Handles the Expandable list from BaseExp.Li.Adapter
		private String[] groups = { "Medical Tests", "Medical Conditions", "Medication", "Medical Procedures", "Vaccinations", "Allergies" };
        //we fill the below String arrays here because the adapter needs a "static" reference of values, 
		//else we would have to use ArrayList which would make it a lot harder to implement
		String[] a= fill("test");
        String[] b= fill("cond");
        String[] c= fill("med");
        String[] d= fill("proc");
        String[] e= fill("vacc");
        String[] f= fill("all");
        String[] id1= fillid("test");
        String[] id2= fillid("cond");
        String[] id3= fillid("med");
        String[] id4= fillid("proc");
        String[] id5= fillid("vacc");
        String[] id6= fillid("all");
        //create "static" String Arrays with children names and ids
        public String[][] children  = {a,b,c,d,e,f};
        public String[][] chid = {id1,id2,id3,id4,id5,id6};
        
        protected String[] fill(String table){
        	int did2 = Integer.parseInt(getIntent().getStringExtra("did"));
        	Cursor temp = null;
    		Cursor buffer = null;
    		String type_from_table = null;
    		String[] items =null;
    		//we take the callback and make a String which can be adjoined to an SQL query
    		if(table=="med") {type_from_table = "medication";}
    		if(table=="test") {type_from_table = "test";}
    		if(table=="all") {type_from_table = "allergy";}
    		if(table=="proc") {type_from_table = "procedure";}
    		if(table=="cond") {type_from_table = "condition";}
    		if(table=="vacc") {type_from_table = "vaccine";}
    		//do queries
    		Cuery q = new Cuery(getApplicationContext());
    		temp = q.fetchOption("SELECT * FROM permission WHERE did="+did2+" AND type='"+type_from_table+"'");
    		//if we have data
    		if(temp.getCount()>0){
    			items =  new String [temp.getCount()];
    			for(int i=0;i<temp.getCount();i++){
    				buffer = q.fetchOption("SELECT * FROM user_"+table+" WHERE id="+temp.getString(temp.getColumnIndex("fileid")));
    				items[i] = buffer.getString(buffer.getColumnIndex("name")); 
    				temp.moveToNext();
    			}
    		}else{
    			items = new String[] { "None."}; 
    		}
    		return items;
        }
        
        protected String[] fillid(String table){
        	int did2 = Integer.parseInt(getIntent().getStringExtra("did"));
        	Cursor temp = null;
    		Cursor buffer = null;
    		String type_from_table = null;
    		String[] items =null;
    		//same here as above
    		if(table=="med") {type_from_table = "medication";}
    		if(table=="test") {type_from_table = "test";}
    		if(table=="all") {type_from_table = "allergy";}
    		if(table=="proc") {type_from_table = "procedure";}
    		if(table=="cond") {type_from_table = "condition";}
    		if(table=="vacc") {type_from_table = "vaccine";}
        
    		Cuery q = new Cuery(getApplicationContext());
    		temp = q.fetchOption("SELECT * FROM permission WHERE did="+did2+" AND type='"+type_from_table+"'");
    		if(temp.getCount()>0){//if for each category we have data
    			items =  new String [temp.getCount()];//get count and if>0
    			for(int i=0;i<temp.getCount();i++){
    				//select the data and add it to an Array. Then, move the cursor's pointer
    				buffer = q.fetchOption("SELECT * FROM user_"+table+" WHERE id="+temp.getString(temp.getColumnIndex("fileid")));
    				items[i] = buffer.getString(buffer.getColumnIndex("id")); 
    				temp.moveToNext();
    			}
    		}else{
    			items = new String[] { "None."}; 
    		}
    		return items;
        }
        
        public String[][] gat(){
        	return children;
        }
        
        public String[][] gid(){
        	return chid;
        }
        //default from BaseExpandableListAdapter
        public Object getChild(int groupPosition, int childPosition) {
            return children[groupPosition][childPosition];
        }

        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        public int getChildrenCount(int groupPosition) {
            return children[groupPosition].length;
        }

        public TextView getGenericView() {
            // Layout parameters for the ExpandableListView
            AbsListView.LayoutParams lp = new AbsListView.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, 64);
            TextView textView = new TextView(DocPerms.this);
            textView.setLayoutParams(lp);
            // Center the text vertically
            textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER);
            // Set the text starting position
            textView.setPadding(36, 0, 0, 0);
            return textView;
        }
        
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            TextView textView = getGenericView();
            textView.setText(getChild(groupPosition, childPosition).toString());
            return textView;
        }

        public Object getGroup(int groupPosition) {
            return groups[groupPosition];
        }

        public int getGroupCount() {
            return groups.length;
        }

        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        public View getGroupView(int groupPosition, boolean isExpanded, View convertView,ViewGroup parent) {
            TextView textView = getGenericView();
            textView.setTextColor(Color.parseColor("#0078ed"));
            textView.setTypeface(null,1);
            textView.setText(getGroup(groupPosition).toString());
            return textView;
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        public boolean hasStableIds() {
            return true;
        }

    }
}
