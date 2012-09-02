package com.day5.app;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.day5.app.Grids.MSGReceiver;
import com.day5.lazylist.FileCache;
import com.day5.utils.Constant;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Setting extends Activity{
	private ListView listView;
	private ArrayList<Map<String,Integer>> data = new ArrayList<Map<String,Integer>>();
	private MyAdapter adapter;
	private LayoutInflater inflater;
	private final int CLEAR_FINISH = 11;
	private final int NOTIFY_DATA_SET_CHANGED = 10;
	
//	public static boolean CACHE_CLEAN = false; 
	private MSGReceiver receiver = new MSGReceiver();
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case NOTIFY_DATA_SET_CHANGED:
				adapter.notifyDataSetChanged();
				break;
			case CLEAR_FINISH:
				adapter.notifyDataSetChanged();
				Toast.makeText(Setting.this, R.string.clear_cache_finish, Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		};
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);
		initData();
		initView();
	}
	
	private void initData(){
		inflater = getLayoutInflater();
		putData();
		adapter = new MyAdapter(this, R.layout.setting_item);
		registerReceiver(receiver, new IntentFilter("android.intent.action.CACHE_IMG"));
	}
	private void initView(){
		listView = (ListView)findViewById(R.id.setting_list);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pisition,
					long arg3) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				switch (pisition) {
				case 0:
					intent.setClass(Setting.this, ManagePic.class);
					startActivity(intent);
					break;
				case 1:
					new CleanThread().start();
					break;
				case 2:
					about();
					break;
				default:
					break;
				}
			}
		});
	}
	
	private void putData(){
		Map<String,Integer> map  = new HashMap<String, Integer>();
		map.put("text", R.string.manage_pic);
		map.put("icon", R.drawable.setting);
		data.add(map);
		
		File f = new File(Constant.DIRECTORY_SDCARD+Constant.DIRECTORY_CACHE);
		if(f.list()==null || f.list().length == 0){
			map  = new HashMap<String, Integer>();
			map.put("text", R.string.cache_clean);
			map.put("icon", R.drawable.clear_ok);
			data.add(map);
		}else{
			map  = new HashMap<String, Integer>();
			map.put("text", R.string.clear_cache);
			map.put("icon", R.drawable.clear);
			data.add(map);
		}
		
		map  = new HashMap<String, Integer>();
		map.put("text", R.string.about);
		map.put("icon", R.drawable.about);
		data.add(map);
	}
	
	private void clearCache(){
		FileCache f = new FileCache(Setting.this);
		f.clear();
	}
	
	private void about(){
		//显示一个dialog
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		super.onKeyDown(keyCode, event);
		return false;
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		unregisterReceiver(receiver);
		super.onDestroy();
	}
	
	private class CleanThread extends Thread{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Map<String,Integer> map  = new HashMap<String, Integer>();
			map.put("text", R.string.cleaning);
			map.put("icon", data.get(1).get("icon"));
			data.set(1, map);
			handler.sendEmptyMessage(NOTIFY_DATA_SET_CHANGED);
			clearCache();
			try {
				sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			map  = new HashMap<String, Integer>();
			map.put("text", R.string.cache_clean);
			map.put("icon", R.drawable.clear_ok);
			data.set(1, map);
			handler.sendEmptyMessage(CLEAR_FINISH);
		}
	}
	private class MyAdapter extends ArrayAdapter<Map<String,Integer>>{
		private int id;
		public MyAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
			id = textViewResourceId;
			// TODO Auto-generated constructor stub
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return data.size();
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if(convertView == null){
				convertView = inflater.inflate(id, null);
			}
			TextView text = (TextView)convertView.findViewById(R.id.setting_item_text);
			ImageView icon = (ImageView)convertView.findViewById(R.id.setting_item_icon);
			text.setText(data.get(position).get("text"));
			icon.setImageResource(data.get(position).get("icon"));
			return convertView;
		}
		
	}
	
	public class MSGReceiver extends BroadcastReceiver{

    	@Override
    	public void onReceive(Context context, Intent intent) {
    		// TODO Auto-generated method stub
    		String action = intent.getAction();
    		if("android.intent.action.CACHE_IMG".equals(action)){
    			Map<String,Integer> map  = new HashMap<String, Integer>();
    			map.put("text", R.string.clear_cache);
    			map.put("icon", R.drawable.clear);
    			data.set(1, map);
    			handler.sendEmptyMessage(NOTIFY_DATA_SET_CHANGED);
    		}
    	}

    }
}
