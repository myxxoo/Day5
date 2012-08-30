package com.day5.app;


import java.util.ArrayList;
import java.util.List;

import com.day5.utils.Constant;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class Tab extends Activity{
	private Resources resources;
	private LayoutInflater inflater;
	private LayoutParams paramsOfContainer;
	private ImageView bar0,bar1,bar2;
	private LinearLayout container;
	private LinearLayout settingLayout,mainLayout,taglistLayout;
	
	
	public static ViewPager viewPager;
	public static ArrayList<View> pagerList = new ArrayList<View>();
	public static MyPagerAdapter pagerAdapter;
	public static LocalActivityManager manager;
	public static Intent refreshIntent;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tab);
		initData();
		initTab();
		
		manager  = new LocalActivityManager(this, true);
		manager.dispatchCreate(savedInstanceState);
//		tabHost.setup(manager);
		
		initViewPager();
	}
	
	private void initData(){
		DisplayMetrics displaysMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics( displaysMetrics );
		Constant.SCREEN_WIDTH = displaysMetrics.widthPixels;
		Constant.SCREEN_HEIGHT = displaysMetrics.heightPixels;
		Constant.IMAGE_HEIGHT = Constant.SCREEN_WIDTH/2-30;
		
		resources = getResources();
		inflater = getLayoutInflater();
		refreshIntent = new Intent(Tab.this,Grids.class);
		paramsOfContainer = new LayoutParams(LayoutParams.FILL_PARENT,Constant.IMAGE_HEIGHT/3>150?150:Constant.IMAGE_HEIGHT/3);
	}
	
	private void initTab(){
		container = (LinearLayout)findViewById(R.id.tab_bar_container);
		settingLayout = (LinearLayout)findViewById(R.id.tab_setting);
		mainLayout = (LinearLayout)findViewById(R.id.tab_main);
		taglistLayout = (LinearLayout)findViewById(R.id.tab_taglist);
		
		container.setLayoutParams(paramsOfContainer);
		bar0 = (ImageView)findViewById(R.id.tab_setting_bar);
		bar1 = (ImageView)findViewById(R.id.tab_main_bar);
		bar2 = (ImageView)findViewById(R.id.tab_taglist_bar);
		settingLayout.setOnClickListener(click);
		mainLayout.setOnClickListener(click);
		taglistLayout.setOnClickListener(click);
		setCurrentBar(1);
	}
	
	private void initViewPager(){
		viewPager = (ViewPager)findViewById(R.id.viewpager);
		pagerAdapter = new MyPagerAdapter();
		pagerList.add(manager.startActivity("0", new Intent(Tab.this,Setting.class)).getDecorView());
		pagerList.add(manager.startActivity("1", new Intent(Tab.this,Grids.class)).getDecorView());
		pagerList.add(manager.startActivity("2", new Intent(Tab.this,TagList.class)).getDecorView());
		viewPager.setAdapter(pagerAdapter);
		viewPager.setCurrentItem(1);
		viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
	}
	
	View.OnClickListener click = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.tab_setting:
				setCurrentBar(0);
				viewPager.setCurrentItem(0);
				break;
			case R.id.tab_main:
				setCurrentBar(1);	
				viewPager.setCurrentItem(1);
				break;
			case R.id.tab_taglist:
				setCurrentBar(2);
				viewPager.setCurrentItem(2);
				break;
			default:
				break;
			}
		}
	};

	private void setCurrentBar(int i){
		if(i == 0){
			bar0.setBackgroundResource(R.drawable.tab_on);
			bar1.setBackgroundResource(R.drawable.tab_off);
			bar2.setBackgroundResource(R.drawable.tab_off);
		}else if(i == 1){
			bar0.setBackgroundResource(R.drawable.tab_off);
			bar1.setBackgroundResource(R.drawable.tab_on);
			bar2.setBackgroundResource(R.drawable.tab_off);
		}else if(i == 2){
			bar0.setBackgroundResource(R.drawable.tab_off);
			bar1.setBackgroundResource(R.drawable.tab_off);
			bar2.setBackgroundResource(R.drawable.tab_on);
		}
	}
	
	@Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	manager.dispatchResume(); 
    	super.onResume();
    }
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	manager.dispatchPause(isFinishing());
    	super.onPause();
    }
	
	/**
     * ViewPager适配器
     */
    public class MyPagerAdapter extends PagerAdapter {
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return pagerList.size();
		}
		
		@Override
		public void notifyDataSetChanged() {
			// TODO Auto-generated method stub
			super.notifyDataSetChanged();
			
		}
		
		@Override
		public int getItemPosition(Object object) {
			// TODO Auto-generated method stub
		    return POSITION_NONE;
		}

		@Override
		public void destroyItem(View container, int position, Object object) {
			// TODO Auto-generated method stub
			((ViewPager) container).removeView(pagerList.get(position));
		}

		@Override
		public Object instantiateItem(View container, int position) {
			// TODO Auto-generated method stub
			((ViewPager) container).addView(pagerList.get(position), 0);
			return pagerList.get(position);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			// TODO Auto-generated method stub
			return arg0 == arg1;
		}
			
    }
    
    public class MyOnPageChangeListener implements OnPageChangeListener {


        @Override
        public void onPageSelected(int arg0) {
                switch (arg0) {
                case 0:
                	setCurrentBar(0);
                	break;
                case 1:
                	setCurrentBar(1);
                    break;
                case 2:
                	setCurrentBar(2);
                    break;
                }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }
}
