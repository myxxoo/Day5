package com.day5.app;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.day5.others.apis.UpYun;
import com.day5.utils.Constant;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class Loading extends Activity{
	private ImageView progress;
	private Resources resources;
	private Intent intent;
	private boolean loadFinish = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading);
		initData();
		initView();
	}
	
	private void initData(){
		resources = getResources();
		Constant.UPYUN_INFO.put("address", resources.getStringArray(R.array.address)[0]);
		Constant.UPYUN_INFO.put("zoomName", resources.getStringArray(R.array.zoom_name)[0]);
		Constant.UP_YUN = new UpYun(resources.getStringArray(R.array.bucketname)[0], resources.getStringArray(R.array.username)[0], resources.getStringArray(R.array.password)[0]);
		loadThread.start();
		
		//计时跳转
		intent = new Intent(this,Tab.class);
		Timer timer = new Timer();  
        TimerTask task = new TimerTask(){  
           @Override  
           public void run(){  
	        	if(!loadFinish){
	        		Constant.CACHE_DATA.clear();
	        	}
	            startActivity(intent); //执行  
	            finish();  
           }  
        };  
        timer.schedule(task, 3300); //自动跳转
	}
	
	private void initView(){
		progress = (ImageView)findViewById(R.id.about_progress);
		progress.setBackgroundResource(R.anim.loading_animation);
		progress.post(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				AnimationDrawable ad = (AnimationDrawable) progress.getBackground();
				ad.start();
			}
		});
		
	}
	
	private void loadData(String path){
		/// 读取目录
		for(int i=0;i<500;i++){
			Constant.CACHE_DATA.add("http://img.pconline.com.cn/images/upload/upc/tx/photoblog/1208/27/c1/13214601_13214601_1346005151075_mthumb.jpg");
			Constant.CACHE_DATA.add("http://image165-c.poco.cn/mypoco/myphoto/20110914/22/119696201109142231233162131778455_006.jpg");
		}
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
	
	Thread loadThread = new Thread(){
		public void run() {
			loadData(Constant.PATH);
			loadFinish = true;
		};
	};
}
