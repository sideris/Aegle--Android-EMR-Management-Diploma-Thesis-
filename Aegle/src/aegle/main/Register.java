package aegle.main;

import com.PGSideris.aegle.R;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.os.Vibrator;

public class Register extends Activity{
	private Cursor query = null;
	TextView fname, mail, pass, usname, reg_error;
	Button reg_but;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_register);
		//init functionality
		
		setupViews();
		initConnection();
		setupClick();
	}
	
	private void setupViews(){
		fname = (TextView) findViewById(R.id.register_fullname);
		mail = (TextView) findViewById(R.id.register_email);
		pass = (TextView) findViewById(R.id.register_pwd);
		usname = (TextView) findViewById(R.id.register_username);
		reg_but = (Button) findViewById(R.id.reg_button);
		reg_error = (TextView) findViewById(R.id.register_error);
	}
	
	private void initConnection(){
		
	}
	
	private void setupClick(){
		reg_but.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				int success = sendRegistration();
				if(success  != 0) addToDB();
				else {
					Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
					vib.vibrate(50);

					reg_error.setText("Trololololo");
				}
			}
			
		});
	}
	
	private int sendRegistration() {
		return 0;
	}
	
	private void addToDB(){
		
	}

}
