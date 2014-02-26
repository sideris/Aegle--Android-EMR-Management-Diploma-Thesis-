package aegle.db;


import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class Cuery {
	//SQLite DB
	protected SQLiteDatabase myDB=null;

	public Cuery(Context a){
		//constructor initializes
		Database openHelper = new Database(a); //create new Database to take advantage of the SQLiteOpenHelper class
		myDB = openHelper.getReadableDatabase(); // or getWritableDatabase();
		myDB=SQLiteDatabase.openDatabase("data/data/com.PGSideris.aegle/databases/aeglea", null, SQLiteDatabase.OPEN_READONLY); //set myDB to aeglea
	}
	
	public Cursor fetchOption(String query) throws SQLException {
		//executing query in database and returning Cursor if not null
        Cursor mCursor = myDB.rawQuery(query, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
	}
	
	public String getSess(){
		Cursor s = this.fetchOption("SELECT sessid FROM save");
		return s.getString(0);
	}
}