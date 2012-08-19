package com.xiaorui.MemoNotes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MemoNotesDBAdapter {

	public static final String	KEY_ID = "id";				//date in '2012-8-17' format										

	public static final String	KEY_START = "t_start";		//start time of a day												

	public static final String	KEY_END = "t_end";			//end time of a day
	
	public static final String	KEY_WEEKDAY = "weekday";	//weekday in integer format
	
	public static final String	KEY_OTHER = "other";		//some comment for special use

	private static final String	DB_NAME	= "MemoNotes.db";
	
	private static final String	DB_TABLE = "MemoNotesTable";
	
	private static final int	DB_VERSION	= 1;

	private Context				mContext = null;
	
	private static final String	DB_CREATE = "CREATE TABLE " + DB_TABLE + 	" (" + 
												KEY_ID + " TEXT," + KEY_START + " TEXT," + KEY_END + " TEXT," +
												KEY_WEEKDAY + " INTEGER," + KEY_OTHER + " TEXT)";

	private SQLiteDatabase		mSQLiteDatabase	= null;

	private DatabaseHelper		mDatabaseHelper	= null;
	
	
	private static class DatabaseHelper extends SQLiteOpenHelper
	{

		DatabaseHelper(Context context)
		{
			super(context, DB_NAME, null, DB_VERSION);				
		}

		/* create a table */
		@Override
		public void onCreate(SQLiteDatabase db)
		{
			// if db not exist in database, then create one
			db.execSQL(DB_CREATE);
		}

		/* update database */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			db.execSQL("DROP TABLE IF EXISTS notes");
			onCreate(db);
		}
	}
	
	/* get Context */
	public MemoNotesDBAdapter(Context context)
	{
		mContext = context;
	}


	// open database, return handle
	public void open() throws SQLException
	{
		mDatabaseHelper = new DatabaseHelper(mContext);
		mSQLiteDatabase = mDatabaseHelper.getWritableDatabase();
	}

	// close database
	public void close()
	{
		mDatabaseHelper.close();
	}

	/* insert a data */
	public long insertData(String id, String t_start, String t_end, int weekday, String other)
	{
		ContentValues tmpValues = new ContentValues();
		tmpValues.put(KEY_ID, id);
		tmpValues.put(KEY_START, t_start);
		tmpValues.put(KEY_END, t_end);
		tmpValues.put(KEY_WEEKDAY, weekday);
		tmpValues.put(KEY_OTHER,other);

		return mSQLiteDatabase.insert(DB_TABLE, KEY_ID, tmpValues);
	}

	/* delete a data */
	public boolean deleteData(String id)
	{
		return mSQLiteDatabase.delete(DB_TABLE, KEY_ID+"=\""+id+"\"", null) > 0;
	}

	/* get all data through Cursor */
	public Cursor fetchAllData()
	{
		return mSQLiteDatabase.query(DB_TABLE, new String[] {KEY_ID, KEY_START, KEY_END, KEY_WEEKDAY, KEY_OTHER}, null, null, null, null, null);
	}

	/* query a specified data */
	public Cursor fetchData(String id) throws SQLException
	{
		
		Cursor mCursor =
		mSQLiteDatabase.query(false, DB_TABLE, new String[] {KEY_ID, KEY_START, KEY_END, KEY_WEEKDAY, KEY_OTHER}, KEY_ID+"=\""+id+"\"", null, null, null, null, null);
				
		if (mCursor != null)
		{
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	/* update a data */
	public boolean updateData(String id, String t_start, String t_end, int weekday, String other)
	{
		ContentValues args = new ContentValues();
		args.put(KEY_ID, id);
		args.put(KEY_START, t_start);
		args.put(KEY_END, t_end);
		args.put(KEY_WEEKDAY, weekday);
		args.put(KEY_OTHER,other);

		return mSQLiteDatabase.update(DB_TABLE, args, KEY_ID + "=\"" + id + "\"", null) > 0;
	}
}

//end class
