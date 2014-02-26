package aegle.main;

import java.util.Date;

import com.PGSideris.aegle.R;

import aegle.db.Cuery;
import aegle.db.Database;
import aegle.web.Sync;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Init extends Activity {
	private Cursor cursor = null;
	EditText name, pwd;
	TextView error, register;
	Button ok_btn;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_init);
		//init elements and animations
		initElements();
		startAnimations();
  		//Check existence of session with DB query
  		Cuery q = new Cuery(this);//Class Cursor from Handlers
  		cursor = q.fetchOption("SELECT * FROM save");//execute SQL query and go to first result
		//if no previous session then sign in with username/password
  		if(cursor.getCount()==0){
			clicks();
		}else{
			//if Session exists
			//Time of session and current time(in milliseconds)
			Long set = cursor.getLong( cursor.getColumnIndex("time_set") );
			Date  dt = new Date();
			Long now = dt.getTime();
			//172800000=2 days, time for Session expiration
			if((now-set)>172800000){
				//If session expired.
				Database db = new Database(getBaseContext());	
        	    db.resetSave();
				clicks();
			}else{
				//sign In with session
				Session_login();
			}
		}

		//check if it's the first time the app runs
		SharedPreferences pref = getSharedPreferences("settings", MODE_PRIVATE);
		SharedPreferences.Editor firstTime = pref.edit();
		if( pref.getBoolean("isFirstTime", true) ){
			firstTime.putBoolean("isFirstTime", false);
			firstTime.commit();
			initElements();
			startAnimations();
		}else{
			//if activity has been started again
		}
	}
	
	private void initElements(){
		name = (EditText) findViewById(R.id.login_username);
		pwd = (EditText) findViewById(R.id.login_password);
		ok_btn = (Button) findViewById(R.id.angry_btn);
		register = (TextView) findViewById(R.id.register_link);
        error = (TextView) findViewById(R.id.login_error);
	}
	
    private void Session_login(){
    	//Create Sync object from Handlers(Constructor 1)
    	Sync sync = new Sync(getApplicationContext());
		/**Get results from Syncing
		 * 0=Everything Ok. 
		 * 1=Session Error 
		 * 2=Server error/empty response**/
    	int result = sync.user();
    	Database db = new Database(getApplicationContext());
        if( result == 0 ){
       		sync.others();
           	Intent log = new Intent(getApplicationContext(), Main.class);
            log.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(log);
            finish();
        }else if( result == 1 ){
        	//If session error(most of the times it's server-side)
        	db.resetSave();
            error.setText("Your session is bad and you should feel bad.");
           	clicks();
        }else if( result == 2 ){
        	//if error with server response or JSONObject
          	error.setText("Connection Error");
           	clicks();
        }else if( result == 3 ){
          	error.setText("Session error");
           	clicks();
        }
    }
    
    private void clicks(){
    	ok_btn.setOnClickListener(new View.OnClickListener() {
        	
    		public void onClick(View v) {
        		//new Sync obj from Handlers(Constructor 2)
        		Sync sync = new Sync(
        				getBaseContext(), 
        				name.getText().toString(),
        				pwd.getText().toString() );
        		/**
        		 * Get results from Syncing
        		 * 0 = Everything Ok. 
        		 * 1 = Wrong password/username 
        		 * 2 = Server error/empty response
        		 * **/
        		int result=sync.user();
	            //if ok then insert user and sign in
	            if(result==0){
	            	//reset views
	              	error.setText("");
	              	ok_btn.setVisibility(View.GONE);
	               	//draw data
	               	sync.others();
	               	//create new intent
	               	Intent log = new Intent(getApplicationContext(), Main.class);
	                // Close all views before launching logged
	                log.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	                startActivity(log);
	                // Close Login Screen
	                finish();
	            }else if(result==1){
	               //if not valid credentials set error message
	               	error.setText("Wrong Username or Password");
	            }else if(result==2) {
	            	//for server error
	            	error.setText("Connection Error. Please try again later");
	            }
        	}
        });
    	
    	register.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
	           	Intent reg = new Intent(getApplicationContext(), Register.class);
	           	reg.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(reg);
			}
    		
    	});
    }
	
	private void startAnimations(){
		/**Animation: part 3**/
		AlphaAnimation textFieldsIn = new AlphaAnimation( 0.0f, 1.0f ); 
		textFieldsIn.setStartOffset(500);
		textFieldsIn.setDuration(400);
		//run animation for all interested elements
		name.startAnimation(textFieldsIn);
		pwd.startAnimation(textFieldsIn); 
		ok_btn.startAnimation(textFieldsIn);
	}
};