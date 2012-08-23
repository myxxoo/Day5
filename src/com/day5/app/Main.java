package com.day5.app;


import java.util.ArrayList;
import java.util.HashMap;

import com.day5.lazylist.ImageLoader;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

public class Main extends Activity{
	private LayoutInflater inflater;
	private GridView gridView;
	private Button moreButton;
	private MyAdapter adapter;
	private ImageLoader imageLoader;
	
	private ArrayList<String> data = new ArrayList<String>();
	private Animation animationIn;
	private Animation animationOut;
	
	
	private int count = 0;
	private int prvPosition = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		initData();
		initView();
		
	}
	
	private void initData(){
		inflater = getLayoutInflater();
		imageLoader=new ImageLoader(this);
		animationIn = AnimationUtils.loadAnimation(this, R.drawable.animation_translate_in);
		animationOut = AnimationUtils.loadAnimation(this, R.drawable.animation_translate_out);
		animationIn.setFillAfter(true);
		animationOut.setFillAfter(true);
		for(int i=0;i<10;i++){
			data.add(mStrings[0]);
			data.add(mStrings[1]);
		}
		adapter = new MyAdapter();
	}
	
	private void initView(){
		moreButton = (Button)findViewById(R.id.more_button);
		gridView = (GridView)findViewById(R.id.gridview);
		gridView.setAdapter(adapter);
		gridView.setOnScrollListener(scrollListener);
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
	
	private class MyAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			count = data.size();
			return count;
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
			}
			ImageView  image = (ImageView)convertView.findViewById(R.id.imageview_item);
			imageLoader.DisplayImage(data.get(position), image);
			return convertView;
		}
		
	}
	
	private String[] mStrings={
            "http://www.eoeandroid.com/uc_server/data/avatar/000/62/48/abc.jpg",
            "http://www.eoeandroid.com/uc_server/data/avatar/000/09/20/48_avatar_middle.jpg"
    };
}
