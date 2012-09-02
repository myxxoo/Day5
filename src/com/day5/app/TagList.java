package com.day5.app;

import java.util.HashMap;

import com.day5.utils.Constant;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class TagList extends Activity{
	private ListView tagListView;
	private MyAdapter adapter;
	private String[] tagListCn,tagListEn;
	private LayoutInflater inflater;
	private HashMap<String,Integer> tagMap = new HashMap<String, Integer>();
	
	private int[] iconList ={R.drawable.tag_1,R.drawable.tag_2,R.drawable.tag_3,R.drawable.tag_4,R.drawable.tag_5,R.drawable.tag_6,R.drawable.tag_7,
			R.drawable.tag_8,R.drawable.tag_9,R.drawable.tag_10,R.drawable.tag_11,R.drawable.tag_12,R.drawable.tag_13};
	private AssetManager asset;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.taglist);
		initData();
		initView();
	}
	
	private void initData(){
		inflater = getLayoutInflater();
		asset = getAssets();
		tagListCn = getResources().getStringArray(R.array.taglist_cn);
		tagListEn = getResources().getStringArray(R.array.taglist_en);
		adapter = new MyAdapter(this, R.layout.taglist_item);
		putIcons();
	}
	
	private void initView(){
		tagListView = (ListView)findViewById(R.id.taglist);
		tagListView.setAdapter(adapter);
		tagListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				Constant.PATH = tagListEn[position];
				Intent i = new Intent("android.intent.action.IMAGE_TAG_CHANGE");
				sendBroadcast(i);
				Tab.viewPager.setCurrentItem(1);
			}
		});
	}
	
	private void putIcons(){
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		super.onKeyDown(keyCode, event);
		return false;
	}
	
	private class MyAdapter extends ArrayAdapter<String>{

		public MyAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
			// TODO Auto-generated constructor stub
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return tagListCn.length;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder holder;
			if(convertView == null){
				convertView = inflater.inflate(R.layout.taglist_item, null);
				holder = new ViewHolder();
				holder.text = (TextView)convertView.findViewById(R.id.taglist_item_text);
				holder.icon = (ImageView)convertView.findViewById(R.id.taglist_item_icon);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder)convertView.getTag();
			}
			holder.text.setText(tagListCn[position]);
			holder.icon.setImageResource(iconList[position]);
			return convertView;
		}
		
	}
	static class ViewHolder{
		TextView text;
		ImageView icon;
	}
}
