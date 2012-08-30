package com.day5.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

public class PicLocal extends Activity{
	private ImageView imageView;
	private Intent intent;
	private String path;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pic_local);
		initData();
		initView();
	}
	
	private void initData(){
		intent = getIntent();
		path = intent.getStringExtra("path");
	}
	
	private void initView(){
		imageView = (ImageView)findViewById(R.id.pic_local_img);
		imageView.setImageBitmap(BitmapFactory.decodeFile(path));
	}
	
}
