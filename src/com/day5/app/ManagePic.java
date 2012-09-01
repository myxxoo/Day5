package com.day5.app;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import com.day5.utils.Constant;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;

public class ManagePic extends Activity{
	private GridView gridView;
	private LinearLayout warnView;
	private ImageButton editBtn;
	private Button cancleBtn,deleteBtn;
	private TextView toolsBarText;
	private LayoutInflater inflater;
	private MyAdapter adapter;
	private String[] data;
	private LayoutParams params;
	private boolean editting = false;
	private ArrayList<String> deleteList = new ArrayList<String>();
	private final int DELETE_FINISH = 10;
	private AlertDialog dialog;
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if(msg.what == DELETE_FINISH){
				loadFileList();
				adapter.notifyDataSetChanged();
				editBtn.setVisibility(View.VISIBLE);
				cancleBtn.setVisibility(View.GONE);
				deleteBtn.setVisibility(View.GONE);
				deleteBtn.setText(R.string.delete);
				toolsBarText.setText(R.string.manage);
				editting = false;
				Toast.makeText(ManagePic.this, R.string.delete_success, Toast.LENGTH_SHORT).show();
				if(dialog.isShowing()){
					dialog.dismiss();
				}
			}
		};
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manage_pic);
		initData();
		if(data != null && data.length != 0)initGridView();
		else initWarnView();
	}
	
	private void initData(){
		inflater = getLayoutInflater();
		loadFileList();
		params = new LayoutParams(LayoutParams.FILL_PARENT,Constant.IMAGE_HEIGHT);
		adapter = new MyAdapter(this, R.layout.imageview_local);
	}
	
	private void initGridView(){
		editBtn = (ImageButton)findViewById(R.id.manage_pic_edit);
		cancleBtn = (Button)findViewById(R.id.manage_pic_cancle);
		deleteBtn = (Button)findViewById(R.id.manage_pic_delete);
		toolsBarText = (TextView)findViewById(R.id.manage_pic_tools_bar_text);
		
		gridView = (GridView)findViewById(R.id.manage_pic_gridview);
		gridView.setAdapter(adapter);
		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,
					long arg3) {
				// TODO Auto-generated method stub
				if(!editting){
					Intent intent = new Intent(ManagePic.this,PicLocal.class);
					intent.putExtra("path", data[position]);
					intent.putExtra("position", position);
					intent.putExtra("list", data);
					startActivity(intent);
				}else{
					if(deleteList.indexOf(data[position]) == -1){
						v.findViewById(R.id.imageview_local_edit).setVisibility(View.VISIBLE);
						deleteList.add(data[position]);
						deleteBtn.setText(getString(R.string.delete)+"("+deleteList.size()+")");
					}else{
						v.findViewById(R.id.imageview_local_edit).setVisibility(View.GONE);
						deleteList.remove(data[position]);
						deleteBtn.setText(getString(R.string.delete)+"("+deleteList.size()+")");
					}
				}
			}
		});
		
		editBtn.setOnClickListener(click);
		cancleBtn.setOnClickListener(click);
		deleteBtn.setOnClickListener(click);
		buildDialog();
	}
	
	private void initWarnView(){
		gridView = (GridView)findViewById(R.id.manage_pic_gridview);
		gridView.setVisibility(View.GONE);
		warnView = (LinearLayout)findViewById(R.id.manage_pic_warn);
		warnView.setVisibility(View.VISIBLE);
	}
	
	private void loadFileList(){
		File f = new File(android.os.Environment.getExternalStorageDirectory(),Constant.DIRECTORY_DOWNLOAD);
		data = f.list(new ImageFilter());
		data = f.list();
	}
	
	View.OnClickListener click = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.manage_pic_edit:
				editBtn.setVisibility(View.GONE);
				cancleBtn.setVisibility(View.VISIBLE);
				deleteBtn.setVisibility(View.VISIBLE);
				toolsBarText.setText(R.string.delete_pic);
				editting = true;
				break;
			case R.id.manage_pic_cancle:
				editBtn.setVisibility(View.VISIBLE);
				cancleBtn.setVisibility(View.GONE);
				deleteBtn.setVisibility(View.GONE);
				deleteBtn.setText(R.string.delete);
				toolsBarText.setText(R.string.manage);
				deleteList.clear();
				adapter.notifyDataSetChanged();
				editting = false;
				break;
			case R.id.manage_pic_delete:
				if(deleteList.size() == 0)return;
				dialog.show();
				break;
			default:
				break;
			}
		}
	};
	
	private void buildDialog(){
		Builder b = new Builder(this);
		b.setTitle(R.string.delete_pic);
		b.setMessage(R.string.delete_confirm);
		b.setPositiveButton(R.string.enter, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				new DeleteThread().start();
				dialog.dismiss();
			}
		});
		
		b.setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		dialog = b.create();
	}
	
	
	private void releaseList(){
		
	}
	
	private class DeleteThread extends Thread{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			int size = deleteList.size();
			for(int i=0;i<size;i++){
				File f = new File(android.os.Environment.getExternalStorageDirectory(),Constant.DIRECTORY_DOWNLOAD+deleteList.get(i));
				f.delete();
			}
			handler.sendEmptyMessage(DELETE_FINISH);
		}
	}
	private class MyAdapter extends ArrayAdapter<String>{
		private int id;
		public MyAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
			id = textViewResourceId;
			// TODO Auto-generated constructor stub
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return data.length;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if(convertView == null){
				convertView = inflater.inflate(id, null);
				convertView.setMinimumHeight(Constant.IMAGE_HEIGHT);
			}
			ImageView  image = (ImageView)convertView.findViewById(R.id.imageview_local_item);
			image.setLayoutParams(params);
			View v = convertView.findViewById(R.id.imageview_local_edit);
			v.setLayoutParams(params);
			v.setVisibility(View.GONE);
			image.setImageBitmap(BitmapFactory.decodeFile(android.os.Environment.getExternalStorageDirectory()+"/"+Constant.DIRECTORY_DOWNLOAD+data[position]));
			return convertView;
		}
	}
	
	class ImageFilter implements FilenameFilter{
		 public boolean isImg(String filename){    
			    if (filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".jpeg")){   
			    	//把文件转成小写后看其后缀是否为.jpg
			      return true;    
			    }else{    
			      return false;    
			    }    
			  }  
	 
		@Override
		public boolean accept(File dir, String filename) {
			// TODO Auto-generated method stub
			return isImg(filename);
			//覆写accept方法
		}
	}
}
