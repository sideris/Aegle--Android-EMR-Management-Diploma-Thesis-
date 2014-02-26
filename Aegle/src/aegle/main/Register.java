package aegle.main;

import com.PGSideris.aegle.R;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class Register extends Activity{
	private Cursor query = null;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_register);	
	}
	
	private void setupViews(){
		
	}
	
	private int sendRegistration() {
		return 0;
	}

}
