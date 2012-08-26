package com.day5.app;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.day5.lazylist.ImageLoader;
import com.day5.lazylist.MemoryCache;
import com.day5.utils.Constant;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class Pic extends Activity{
	private Intent intent;
	private String url,zoomUrl;
	private ImageView imgView;
	private Button paperBtn,downBtn;
	
	private ImageLoader imageLoader;
	private final int IMG_LOAD_FINISH = 10;
	private final int SET_WALLPEPER_FINISH = 11;
	
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case IMG_LOAD_FINISH:
				imageLoader.setZoomAble(false);
				imageLoader.DisplayImage(url, imgView);
				break;
			case SET_WALLPEPER_FINISH:
				Toast.makeText(Pic.this, R.string.set_success, Toast.LENGTH_SHORT).show();
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
		setContentView(R.layout.pic);
		initData();
		initView();
		loadImg.start();
	}
	
	private void initData(){
		intent = getIntent();
		url = intent.getStringExtra("url");
		zoomUrl = url+Constant.UPYUN_INFO.get("zoomName");
		imageLoader = new ImageLoader(this);
	}
	
	private void initView(){
		imgView = (ImageView)findViewById(R.id.pic_img);
		imageLoader.DisplayImage(zoomUrl, imgView);
		paperBtn = (Button)findViewById(R.id.pic_set);
		downBtn = (Button)findViewById(R.id.pic_down);
		
		paperBtn.setOnClickListener(click);
		downBtn.setOnClickListener(click);
	}
	
	Thread loadImg = new Thread(){
		public void run() {
			handler.sendEmptyMessage(IMG_LOAD_FINISH);
		};
	};
	
	View.OnClickListener click = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.pic_set:	
				new SetWallPaper().start();
				break;
			case R.id.pic_down:
				imageLoader.downloadPic(url);
				Toast.makeText(Pic.this, R.string.download_succss, Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		}
	};
	
	
	
	protected void onDestroy() {
		imageLoader.memoryCacheClear();
		super.onDestroy();
	};
	
	private class SetWallPaper extends Thread{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			imageLoader.setBitmapWallPaper(url);
			handler.sendEmptyMessage(SET_WALLPEPER_FINISH);
		}
	}
}
