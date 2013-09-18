package com.PGSideris.aegle;

import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.PGSideris.Handlers.Cuery;
import com.PGSideris.Handlers.Database;
import com.PGSideris.Handlers.Sync;

public class Login extends Activity {
	protected Cursor cursor=null, cur=null;
	EditText un,pw;
	TextView error;
    Button ok;
    ProgressBar pb;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        
        //Assign View's components to variables
        un=(EditText)findViewById(R.id.ins_un);
        pw=(EditText)findViewById(R.id.ins_pw);
        ok=(Button)findViewById(R.id.btn_login);
        error=(TextView)findViewById(R.id.tv_error);
   		pb = (ProgressBar) findViewById(R.id.pBar);
  		
   		//Check existence of session with DB query
  		Cuery q = new Cuery(this);//Class Cursor from Handlers
  		cursor = q.fetchOption("SELECT * FROM save");//execute SQL query and go to first result
		//if no previous session then sign in with username/password
  		if(cursor.getCount()==0){
			clicks();
		}else{
			//if Session exists
			//Time of session and current time(in milliseconds)
			Long set=cursor.getLong((cursor.getColumnIndex("time_set")));
			Date  dt = new Date();
			Long now=dt.getTime();
			//172800000=2 days, time for Session expiration
			if((now-set)>172800000){
				//If session expired then create new Database object and clear previous sessions 
				Database db = new Database(getApplicationContext());	
        	    db.resetSave();
				clicks();
			}else{
				//if everything is ok Sign In with session
				Sess_log();
			}
		}
    }
    
    private void Sess_log(){
    	//Create Sync obj from Handlers(Constructor 1)
    	Sync sync =new Sync(getApplicationContext());
		/**Get results from Syncing
		 * 0=Everything Ok. 
		 * 1=Session Error 
		 * 2=Server error/empty response**/
    	int result=sync.user();
    	Database db = new Database(getApplicationContext());
        if(result==0){
        	pb.setVisibility(View.VISIBLE);
       		//draw data
       		sync.others();
          	//create new intent
           	Intent log = new Intent(getApplicationContext(), Welcome.class);
            // Close all views before launching logged
            log.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(log);
            // Close Login Screen
            finish();
        }else if(result==1){
        	//If session error(most of the times it's server-side)
        	db.resetSave();
            error.setText("Your session is bad and you should feel bad.");
            ok.setText("Ok :(");
           	clicks();
        }else if(result==2){
        	//if error with server response or JSONObject
          	error.setText("Connection Error. Please try again later ");
           	clicks();
        }else if(result==3){
          	error.setText("Sessions do not match ");
           	clicks();
        }
    }
    
    private void clicks(){
    	ok.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		//new Sync obj from Handlers(Constructor 2)
        		Sync sync = new Sync(getApplicationContext(),un.getText().toString(),pw.getText().toString());
        		/**Get results from Syncing
        		 * 0=Everything Ok. 
        		 * 1=Wrong password/username 
        		 * 2=Server error/empty response**/
        		int result=sync.user();
	            //if ok then insert user and sign in
	            if(result==0){
	            	//reset views
	              	pb.setVisibility(View.VISIBLE);
	              	error.setText("");
	               	ok.setVisibility(View.GONE);
	               	//draw data
	               	sync.others();
	               	//create new intent
	               	Intent log = new Intent(getApplicationContext(), Welcome.class);
	                // Close all views before launching logged
	                log.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	                startActivity(log);
	                // Close Login Screen
	                finish();
	            }else if(result==1){
	               //if not valid credentials set error message
	               	error.setText("Wrong Username or Password");
	               	pb.setVisibility(View.GONE);
	              	ok.setVisibility(View.VISIBLE);
	            }else if(result==2) {
	            	//for server error
	            	error.setText("Connection Error. Please try again later");
	            }
        	}
        });
    }
}
