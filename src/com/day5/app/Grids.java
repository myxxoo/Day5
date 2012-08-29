package com.day5.app;


import java.util.ArrayList;
import java.util.List;

import com.day5.lazylist.ImageLoader;
import com.day5.others.apis.UpYun;
import com.day5.utils.Constant;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;

public class Grids extends Activity{
	private LayoutInflater inflater;
	private GridView gridView;
	private Button moreButton;
	private MyAdapter adapter;
	private ImageLoader imageLoader;
	private Animation animationIn;
	private Animation animationOut;
	private Animation animationDown;
	
	private ArrayList<String> data = new ArrayList<String>();
	private Resources resources;
	
	private int count = 0;
	private int showCount = 12;
	private final int DEFAULT_SHOW_COUNT = 12;
	private int prvPosition = 0;
	private LayoutParams params;
	private UpYun upyun;
	private MSGReceiver receiver = new MSGReceiver();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		initData();
		initView();
		
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		initData();
		initView();
	}
	
	private void initData(){
		inflater = getLayoutInflater();
		imageLoader=new ImageLoader(this);
		params = new LayoutParams(LayoutParams.FILL_PARENT,Constant.IMAGE_HEIGHT);
		
		resources = getResources();
		Constant.UPYUN_INFO.put("address", resources.getStringArray(R.array.address)[0]);
		Constant.UPYUN_INFO.put("zoomName", resources.getStringArray(R.array.zoom_name)[0]);
		upyun = new UpYun(resources.getStringArray(R.array.bucketname)[0], resources.getStringArray(R.array.username)[0], resources.getStringArray(R.array.password)[0]);
		loadData(Constant.PATH);
		adapter = new MyAdapter();
		registerReceiver(receiver, new IntentFilter("android.intent.action.IMAGE_TAG_CHANGE"));
	}
	
	private void initView(){
		animationIn = AnimationUtils.loadAnimation(this, R.drawable.animation_translate_in);
		animationOut = AnimationUtils.loadAnimation(this, R.drawable.animation_translate_out);
		animationDown = AnimationUtils.loadAnimation(this, R.drawable.animation_translate_down);
		animationIn.setFillAfter(true);
		animationOut.setFillAfter(true);
		animationDown.setFillAfter(true);
		moreButton = (Button)findViewById(R.id.more_button);
		gridView = (GridView)findViewById(R.id.gridview);
		gridView.setAdapter(adapter);
		
		gridView.setOnScrollListener(scrollListener);
		gridView.setOnItemClickListener(itemClick);
		moreButton.setOnClickListener(click);
	}
	
	private void loadData(String path){
		/// 读取目录
		data.clear();
		try {
			List<UpYun.FolderItem> items = upyun.readDir(path);
			for(int i=0;i<items.size();i++){
				data.add(items.get(i).name);
			}
			count = data.size();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	OnScrollListener scrollListener = new OnScrollListener() {
		
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			// TODO Auto-generated method stub
			
			if (view.getLastVisiblePosition() == (view.getCount() - 1) ) { 
				if(prvPosition != count-1){
					moreButton.startAnimation(animationIn);
					moreButton.setVisibility(View.VISIBLE);
				}
			}else 
				if(prvPosition == count-1){
					moreButton.startAnimation(animationOut);
				}
			prvPosition = view.getLastVisiblePosition();
		}
		
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			// TODO Auto-generated method stub
			
		}
	};
	
	AdapterView.OnItemClickListener itemClick = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
			// TODO Auto-generated method stub
//			System.out.println(data.get(position));
			Intent intent = new Intent(Grids.this,Pic.class);
			intent.putExtra("url", Constant.UPYUN_INFO.get("address")+Constant.PATH+data.get(position));
			startActivity(intent);
		}
	};
	
	View.OnClickListener click = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			moreButton.startAnimation(animationDown);
			showCount+=20;
			adapter.notifyDataSetChanged();
		}
	};
	
	protected void onRestart() {
		super.onRestart();
		System.out.println("restart");
	};
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		System.out.println("resume");
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		unregisterReceiver(receiver);
		super.onDestroy();
	}
	
	private class MyAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			if(showCount>count){
				showCount = count;
			}
			return showCount;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if(convertView==null){
				convertView = inflater.inflate(R.layout.imageview, null);
				convertView.setMinimumHeight(Constant.IMAGE_HEIGHT);
			}
			ImageView  image = (ImageView)convertView.findViewById(R.id.imageview_item);
			image.setLayoutParams(params);
			imageLoader.DisplayImage(Constant.UPYUN_INFO.get("address")+Constant.PATH+data.get(position)+Constant.UPYUN_INFO.get("zoomName"), image);
			return convertView;
		}
		
	}
	
    public class MSGReceiver extends BroadcastReceiver{

    	@Override
    	public void onReceive(Context context, Intent intent) {
    		// TODO Auto-generated method stub
    		String action = intent.getAction();
    		if("android.intent.action.IMAGE_TAG_CHANGE".equals(action)){
    			loadData(Constant.PATH);
    			showCount = DEFAULT_SHOW_COUNT;
    			adapter.notifyDataSetChanged();
    		}
    	}

    }
	
}
