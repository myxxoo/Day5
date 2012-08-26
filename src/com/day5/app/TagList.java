package com.day5.app;

import com.day5.utils.Constant;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class TagList extends Activity{
	private ListView tagListView;
	private MyAdapter adapter;
	private String[] tagListCn,tagListEn;
	private LayoutInflater inflater;
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
		tagListCn = getResources().getStringArray(R.array.taglist_cn);
		tagListEn = getResources().getStringArray(R.array.taglist_en);
		adapter = new MyAdapter(this, R.layout.taglist_item);
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
				Tab.pagerAdapter.notifyDataSetChanged();
				Tab.viewPager.setCurrentItem(1);
			}
		});
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
			if(convertView == null){
				convertView = inflater.inflate(R.layout.taglist_item, null);
			}
			TextView text = (TextView)convertView.findViewById(R.id.taglist_item_text);
			text.setText(tagListCn[position]);
			return convertView;
		}
	}
}
