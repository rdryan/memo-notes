package com.xiaorui.MemoNotes;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.Time;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MemoNotesActivity extends Activity {
	
	private ListView mListView;
	private Button mButtonSignIn;
	private Button mButtonSignOut;
	private MemoNotesDBAdapter mDB;
	private static int	mCount = 10;
	   
	private String[] Weekdays = {"Sunday", "Monday", "Tuesday", "Wednesday",
                "Thursday", "Friday", "Saturday"};	 
	
    protected static final int MENU_MODIFY_START = Menu.FIRST;
    protected static final int MENU_MODIFY_END = Menu.FIRST+1;
    protected static final int MENU_DELETE_ITEM = Menu.FIRST+2;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
                
        mButtonSignIn = (Button)findViewById(R.id.buttonSignIn);
        mButtonSignOut = (Button)findViewById(R.id.buttonSignOut);
        mListView = (ListView)findViewById(R.id.listViewSub);
        mCount = 10;
        
        mDB = new MemoNotesDBAdapter(this);
        
        //get database handle
        mDB.open();
                
        //update view
        UpdateAdapter();
        

         /*
        mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				setTitle("Select Item"+arg2);
			}
		});

        mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
    		@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				setTitle("this is a test");
				return true;
			}
        });
        */
      
        //LongPress Process
        mListView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {			
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				menu.setHeaderTitle("Option");   
				menu.add(0, MENU_MODIFY_START, 0, "Modify SignIn Time");
				menu.add(0, MENU_MODIFY_END, 0, "Modify SignOut Time");
				menu.add(0, MENU_DELETE_ITEM, 0, "Delete This Record");
			}
		}); 
		
        
        mButtonSignIn.setOnClickListener(new Button.OnClickListener(){
           	public void onClick(View v)
        	{      	
           		//insert start time, weekday
           		String TodayDate = GetTodayDate();
           		Cursor cur = mDB.fetchData(TodayDate);
           		
           		if (cur.getCount() != 0)	//Have this record in database
           		{
           			ShowMsgBox("Today has already signed in!\nYou can modify the record by LongPress that item.");
           			return;           		
           		}
           		
               	mDB.insertData(TodayDate, GetTime(), "", GetWeekday(), "");
               		
	            //mCount += 1;
	            UpdateAdapter();
        	}
        });
        
        mButtonSignOut.setOnClickListener(new Button.OnClickListener(){
           	public void onClick(View v)
        	{      
           		//update end time
           		String TodayDate = GetTodayDate();
           		Cursor cur = mDB.fetchData(TodayDate);
           		int count = cur.getCount();
           		
           		if (cur == null) return;	//Error return
           		if (count != 0)		//Have this record in database
           		{
           			int ColumStart = cur.getColumnIndex("t_start");
           			int ColumEnd = cur.getColumnIndex("t_end");
           			int ColumWeekday = cur.getColumnIndex("weekday");
           			String startBak = cur.getString(ColumStart);
           			String endBak = cur.getString(ColumEnd);
           			int weekdayBak = cur.getInt(ColumWeekday);
           			
           			if (!endBak.equalsIgnoreCase(""))
           			{
           				ShowMsgBox("Today has already signed out!\nYou can modify the record by LongPress that item.");
           				return;
           			}
           		
           			String t_end = GetTime();
           			boolean ret = mDB.updateData(GetTodayDate(), startBak, t_end, weekdayBak, "");
           			if (!ret) 	//if update database not success, then return
           			{
           				ShowMsg("Update SignOut time failed!");
           				return;
           			}
           		}
           		
	            //mCount += 1;
	            UpdateAdapter();
        	}
        });  
        
        
    }
     
	//LongPress Menu Process Function
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
		View v = info.targetView;
		TextView ItemID =(TextView) v.findViewById(R.id.ItemID);		
		String id = ItemID.getText().toString();
		
		switch (item.getItemId())
		{
		case MENU_MODIFY_START:
				ModifyStart(id);
				break;
		case MENU_MODIFY_END:
				ModifyEnd(id);
				break;
		case MENU_DELETE_ITEM:
				ProcessDeleteItem(id);
				break;
		default:
				break;			
		}

		return super.onContextItemSelected(item);
	} 

	
	@Override
	public void onBackPressed()
	{
		mDB.close();	//close database
		super.onBackPressed();
	}

	public void UpdateAdapter()
	{
        final ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
        Cursor cur = mDB.fetchAllData();
        int total = cur.getCount();
        mCount = total;		//Need do some modification
        
        cur.moveToLast();        
        for(int i= mCount-1;i>=0 && total>0 ;i--)  
        {  
            HashMap<String, Object> map = new HashMap<String, Object>();  
            int columID = cur.getColumnIndex("id");
            int columStart = cur.getColumnIndex("t_start");
            int columEnd = cur.getColumnIndex("t_end");
            int columWeekday = cur.getColumnIndex("weekday");
            
            String content = "sign IN at ";
            content += cur.getString(columStart);
            content += " -- sign OUT at ";
            content += cur.getString(columEnd);   
            
            String weekday = Weekdays[cur.getInt(columWeekday)];
            
            map.put("ItemID", cur.getString(columID));		//date in list view     
            map.put("ItemWeekday", weekday);
            map.put("ItemContent", content);  				//content in list view
            listItem.add(map);  
            
            if (!cur.moveToPrevious()) break;        		    
        }  
                    
        final SimpleAdapter listItemAdapter = new SimpleAdapter(this,listItem,
            R.layout.list_item,       
            new String[] {"ItemID", "ItemWeekday", "ItemContent"},   
            new int[] {R.id.ItemID, R.id.ItemWeekday, R.id.ItemContent}  
        );  
                
        mListView.setAdapter(listItemAdapter);  
	}
	
	public String GetTodayDate()
	{
		String Date = null;
		Time time = new Time();
		time.setToNow();	
		
		int month = 0;
		int day = 0;
		
		month = time.month + 1;
		day = time.monthDay;
		
		Date = 	time.year + "-" + month + "-" + day;
		
		return Date ;
	}
	
	public int GetWeekday()
	{
		Time time = new Time();
		time.setToNow();
	
		return time.weekDay;
	}

	
	public String GetTime()
	{
		String t = null;
		Time time = new Time();
		time.setToNow();
		//t = time.hour + ":" + time.minute;
		t = time.format("%0H:%0M");
		return t;
	}
	
	public void ShowMsg(String str)
	{
		Toast.makeText(this,str, Toast.LENGTH_SHORT).show(); 
	}
	
	public void ShowMsgBox(String str)
	{
		new AlertDialog.Builder(this)
		.setTitle("Notice")
		.setIcon(R.drawable.alert_dialog_icon)
		.setMessage(str)
		.setPositiveButton("OK",null)
		.show();
	}
	

	public void ModifyStart(final String id)
	{
		LayoutInflater inflater = LayoutInflater.from(this);
		final View modify_view = inflater.inflate(R.layout.modify_view,null);
		
		new AlertDialog.Builder(this)
		.setTitle("Modify Sign In Time")
		.setView(modify_view)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				EditText timeView = (EditText)modify_view.findViewById(R.id.editTextTime);
				UpdateStartTime(id,timeView.getText().toString());
			}
		})
		.setNegativeButton("Cancel",null)
		.show();
		
	}
	
	public void ModifyEnd(final String id)
	{
		LayoutInflater inflater = LayoutInflater.from(this);
		final View modify_view = inflater.inflate(R.layout.modify_view,null);
		
		new AlertDialog.Builder(this)
		.setTitle("Modify Sign Out Time")
		.setView(modify_view)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				EditText timeView = (EditText)modify_view.findViewById(R.id.editTextTime);
				UpdateEndTime(id,timeView.getText().toString());
			}
		})
		.setNegativeButton("Cancel",null)
		.show();
	}
	
	public boolean DeleteItem(String id) 
	{
		boolean ret = mDB.deleteData(id);
		return ret;
	}
	public void ProcessDeleteItem(final String id)
	{
		new AlertDialog.Builder(this)
		.setTitle("Notice")
		.setIcon(R.drawable.alert_dialog_icon)
		.setMessage("Please Confirm to Delete!")
		.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				if (DeleteItem(id))
				{
					UpdateAdapter();
					ShowMsg("The selected item has been deleted!");
				}
			}
		})
		.setNegativeButton("Cancel",null)
		.show();
	}
	
	public void UpdateStartTime(String id, String startTime)
	{
   		Cursor cur = mDB.fetchData(id);
   		int count = cur.getCount();
   		
   		if (count != 0)		//Have this record in database
   		{
   			int ColumEnd = cur.getColumnIndex("t_end");
   			int ColumWeekday = cur.getColumnIndex("weekday");
   			String endBak = cur.getString(ColumEnd);
   			int weekdayBak = cur.getInt(ColumWeekday);
   
   			boolean ret = mDB.updateData(id, startTime, endBak, weekdayBak, "");
   			if (!ret) 	//if update database not success, then return
   			{
   				ShowMsg("Modify Sign In time failed!");
   			}
   			
   			UpdateAdapter();		//update list view
   		}		
	}
	
	public void UpdateEndTime(String id, String endTime)
	{
  		Cursor cur = mDB.fetchData(id);
   		int count = cur.getCount();
   		
   		if (count != 0)		//Have this record in database
   		{
   			int ColumStart = cur.getColumnIndex("t_start");
   			int ColumWeekday = cur.getColumnIndex("weekday");
   			String StartBak = cur.getString(ColumStart);
   			int weekdayBak = cur.getInt(ColumWeekday);
   
   			boolean ret = mDB.updateData(id, StartBak, endTime, weekdayBak, "");
   			if (!ret) 	//if update database not success, then return
   			{
   				ShowMsg("Modify Sign Out time failed!");
   			}
   			
   			UpdateAdapter();	//update list view

   		}
	}
	
	
	
}