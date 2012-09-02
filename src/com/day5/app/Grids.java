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
import android.os.Handler;
import android.view.KeyEvent;
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
//	private Button moreButton;
	private MyAdapter adapter;
	private ImageLoader imageLoader;
	private Animation animationIn;
	private Animation animationOut;
	private Animation animationDown;
	
	private ArrayList<String> data = new ArrayList<String>();
	private Resources resources;
	
	private int count = 0;
	private final int DEFAULT_SHOW_COUNT = 18;
	private int showCount = DEFAULT_SHOW_COUNT;
	private final int NOTIFY_GRID = 10;
	private int prvPosition = 0;
	private boolean loading = false;
	private LayoutParams params;
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case NOTIFY_GRID:
				adapter.notifyDataSetChanged();
				loading = false;
				Intent intent = new Intent("android.intent.action.CACHE_IMG");
				sendBroadcast(intent);
				break;

			default:
				break;
			}
		};
	};
	private MSGReceiver receiver = new MSGReceiver();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.grids);
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
		if(Constant.CACHE_DATA.size() == 0){
			loadData(Constant.PATH);
		}else{
			data = (ArrayList<String>) Constant.CACHE_DATA.clone();
			Constant.CACHE_DATA.clear();
			count = data.size();
		}
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
//		moreButton = (Button)findViewById(R.id.more_button);
		gridView = (GridView)findViewById(R.id.gridview);
		gridView.setAdapter(adapter);
		
		gridView.setOnScrollListener(scrollListener);
		gridView.setOnItemClickListener(itemClick);
//		moreButton.setOnClickListener(click);
	}
	
	private void loadData(String path){
		/// 读取目录
		data.clear();
		for(int i=0;i<500;i++){
			data.add("http://img.pconline.com.cn/images/upload/upc/tx/photoblog/1208/27/c1/13214601_13214601_1346005151075_mthumb.jpg");
			data.add("http://image165-c.poco.cn/mypoco/myphoto/20110914/22/119696201109142231233162131778455_006.jpg");
		}
		count = data.size();
//		try {
//			List<UpYun.FolderItem> items = upyun.readDir(path);
//			for(int i=0;i<items.size();i++){
//				data.add(items.get(i).name);
//			}
//			count = data.size();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	OnScrollListener scrollListener = new OnScrollListener() {
		
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			// TODO Auto-generated method stub
			
			if (view.getLastVisiblePosition() > (view.getCount() - 3) && !loading) { 
				loading = true;
				showCount+=DEFAULT_SHOW_COUNT;
				handler.sendEmptyMessage(NOTIFY_GRID);
			}
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
//			intent.putExtra("url", Constant.UPYUN_INFO.get("address")+Constant.PATH+data.get(position));
			intent.putExtra("name", data.get(position));
			intent.putExtra("url", data.get(position));
			startActivity(intent);
		}
	};
	
//	View.OnClickListener click = new View.OnClickListener() {
//		
//		@Override
//		public void onClick(View v) {
//			// TODO Auto-generated method stub
//			moreButton.startAnimation(animationDown);
//			showCount+=showCount;
//			adapter.notifyDataSetChanged();
//		}
//	};
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		super.onKeyDown(keyCode, event);
		return false;
	}
	
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
//			imageLoader.DisplayImage(Constant.UPYUN_INFO.get("address")+Constant.PATH+data.get(position)+Constant.UPYUN_INFO.get("zoomName"), image);
			imageLoader.DisplayImage(data.get(position), image);
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
//    			gridView.setSelection(0);
    			gridView.scrollTo(0, 0);
    			adapter.notifyDataSetChanged();
//    			gridView.scrollTo(0, 0);
    		}
    	}

    }
	
}
