package com.PGSideris.Permz;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import aegle.db.Cuery;
import aegle.db.Database;
import aegle.web.CustomHttpTask;
import aegle.web.Server;
import aegle.web.Sync;
import android.app.Activity;
import android.content.Context;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.PGSideris.aegle.R;


public class GPerm extends Activity{
	
	protected String uid,did="0";
	protected SQLiteDatabase myDB=null,myDB2=null;
	private Cursor cursor=null;
	private ArrayAdapter<String> listAdapter ;
	ExpandableListAdapter mAdapter;
	String[] dids;
	String[][] ch = null,chid = null;
	Cursor doc = null;
	ArrayList<String> itemlist = new ArrayList<String>();
	ArrayList<String> idlist = new ArrayList<String>();	
	ListView list=null;
	ExpandableListView listView = null;
	Server server = new Server();
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_giveperm);
		list = (ListView) findViewById(R.id.doc_list);
		
		uid = getIntent().getStringExtra("uid");
		
		mAdapter = new MyExpandableListAdapter(this);
		listView = (ExpandableListView) findViewById(R.id.perm_list);
		listView.setAdapter(mAdapter);
		registerForContextMenu(listView);
		Toast.makeText(getApplicationContext(),"Select Doctor and long click to provide permissions",Toast.LENGTH_LONG).show();
	}

	protected void onResume(){
		super.onResume();
		Cuery q = new Cuery(this);
		cursor = q.fetchOption("SELECT * FROM user_login");//use above to execute SQL query
		
		id_names();
		Fill_DoctorList();
		doclicks();
	}

	private void doclicks(){
		list.setOnItemClickListener(new OnItemClickListener(){

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
				for(int i=0;i<arg0.getChildCount();i++){
					arg0.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
				}
				arg1.setBackgroundColor(Color.CYAN);
				did = dids[arg2];
			}
			
		});
	}
	
	private void Fill_DoctorList(){
		//selecting all Doctors
		Cuery q = new Cuery(this);
		cursor = q.fetchOption("SELECT * FROM doctor");
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
				 }
				 else{
					 listAdapter.add(cursor.getString(cursor.getColumnIndex("first"))+" "+cursor.getString(cursor.getColumnIndex("last")));
		        	}
			 }
		 }
		 // Set the ArrayAdapter as the ListView's adapter.  
		 list.setAdapter(listAdapter); 
	}
	
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	 if(did.equals("0")){
         	Toast.makeText(getApplicationContext(), "Select a Doctor and then LongClick on records", Toast.LENGTH_SHORT).show();
         }else{
	    	//This sets the menu after a long click
	    	menu.setHeaderTitle("Give Permission");
	        menu.add(0, 0, 0, "Confirm");
        }
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
	        	postParameters.add(new BasicNameValuePair("uid",uid));
	        	postParameters.add(new BasicNameValuePair("did",did));
	            postParameters.add(new BasicNameValuePair("fileid",chid[groupPos][childPos]));
	            postParameters.add(new BasicNameValuePair("type",sync[groupPos]));
	            postParameters.add(new BasicNameValuePair("add","specific"));
	            JSONObject response = null;
	           try {
	        	   //post to server the deletion and delete from local SQLite
	    			CustomHttpTask asdf = new CustomHttpTask();
					response = asdf.execute(server.server+"add_perm.php", postParameters).get();
					if(response.getString("success").equals("1")){
						Sync syncP = new Sync(getApplicationContext());
						syncP.addperms();
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
            //setting database
            Database openHelper = new Database(this);
    		myDB2 = openHelper.getReadableDatabase(); 
    		myDB2 =SQLiteDatabase.openDatabase("data/data/com.PGSideris.aegle/databases/aeglea", null, SQLiteDatabase.OPEN_READWRITE);
			//get Session to post for validity
			Cuery q = new Cuery(getApplicationContext());
			String session = q.getSess();
			//Create an ArrayList with nameValuePairs in order to add data to POST
            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        	postParameters.add(new BasicNameValuePair("session_id",session));
        	postParameters.add(new BasicNameValuePair("uid",uid));
        	postParameters.add(new BasicNameValuePair("did",did));
        	postParameters.add(new BasicNameValuePair("type",sync[groupPos]));
            postParameters.add(new BasicNameValuePair("add","group"));
            JSONObject response = null;
	        try {
	        	CustomHttpTask asdf = new CustomHttpTask();
				response = asdf.execute(server.server+"add_perm.php", postParameters).get();
				if(response.getString("success").equals("1")){
					Sync syncP = new Sync(getApplicationContext());
					syncP.addperms();
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
	
    private void id_names(){
    	//append data and activate the adapter
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
	        @SuppressWarnings("unused")
			private Context context;

	        public MyExpandableListAdapter(Context context) {
	            this.context = context;
	        }

	        
	        protected String[] fill(String table){
	        	Cursor temp = null;
	    		String[] items =null;
	    		//do queries
	    		Cuery q = new Cuery(getApplicationContext());
	    		temp = q.fetchOption("SELECT * FROM user_"+table);
	    		//if we have data
	    		if(temp.getCount()>0){
	    			items =  new String [temp.getCount()];
	    			for(int i=0;i<temp.getCount();i++){
	    				items[i] = temp.getString(temp.getColumnIndex("name")); 
	    				temp.moveToNext();
	    			}
	    		}else{
	    			items = new String[] { "None."}; 
	    		}
	    		return items;
	        }
	        
	        protected String[] fillid(String table){
	        	Cursor temp = null;
	    		String[] items =null;
	    		//do queries
	    		Cuery q = new Cuery(getApplicationContext());
	    		temp = q.fetchOption("SELECT * FROM user_"+table);
	    		if(temp.getCount()>0){
	    			//if for each category we have data
	    			items =  new String [temp.getCount()];//get count and if>0
	    			for(int i=0;i<temp.getCount();i++){
	    				//select the data and add it to an Array. Then, move the cursor's pointer
	    				items[i] = temp.getString(temp.getColumnIndex("id"));
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
	            TextView textView = new TextView(GPerm.this);
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
