package com.day5.app;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import com.day5.utils.Constant;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.util.FloatMath;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class PicLocal extends Activity implements OnTouchListener{
	private ImageView imageView;
	private RelativeLayout toolsBar;
	private Button setBtn,deleteBtn;
	private ImageButton infoBtn,prvBtn,nextBtn;
	
	private Intent intent;
	private String path;
	private int position;
	private String[] fileList;
	
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();
    Bitmap bitmap;

    float minScaleR;// 最小缩放比例
    static final float MAX_SCALE = 4f;// 最大缩放比例

    static final int NONE = 0;// 初始状态
    static final int DRAG = 1;// 拖动
    static final int ZOOM = 2;// 缩放
    int mode = NONE;

    PointF prev = new PointF();
    PointF mid = new PointF();
    float dist = 1f;
	
    private AlertDialog dialog,warnDialog;
    private GestureDetector gestureDetector;
    
    private final int SHOW_INFO_DIALOG = 10;
    private final int DELETE_FILE = 11;
    private final int SET_WALLPAPER_FINISH = 12;
    private Handler handler = new Handler(){
    	public void handleMessage(android.os.Message msg) {
    		switch (msg.what) {
			case SHOW_INFO_DIALOG:
				buildDialog();
				dialog.show();
				break;
			case DELETE_FILE:
				deleteFile();
				Toast.makeText(PicLocal.this, R.string.delete_success, Toast.LENGTH_SHORT).show();
				break;
			case SET_WALLPAPER_FINISH:
				Toast.makeText(PicLocal.this, R.string.set_success, Toast.LENGTH_SHORT).show();
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
		setContentView(R.layout.pic_local);
		initData();
		initView();
	}
	
	private void initData(){
		intent = getIntent();
		path = intent.getStringExtra("path");
		fileList = intent.getStringArrayExtra("list");
		position = intent.getIntExtra("position", 0);
		gestureDetector = new GestureDetector(this,new OnDoubleClick());
	}
	
	private void initView(){
		imageView = (ImageView)findViewById(R.id.pic_local_img);
		bitmap = BitmapFactory.decodeFile(Constant.DIRECTORY_SDCARD+Constant.DIRECTORY_DOWNLOAD+path);
		imageView.setImageBitmap(bitmap);
		imageView.setOnTouchListener(this);
		minZoom();
        center();
        imageView.setImageMatrix(matrix);
        
        toolsBar = (RelativeLayout)findViewById(R.id.pic_local_tools_bar);
        setBtn = (Button)findViewById(R.id.pic_local_set);
        deleteBtn = (Button)findViewById(R.id.pic_local_delete);
        infoBtn = (ImageButton)findViewById(R.id.pic_local_info);
        prvBtn = (ImageButton)findViewById(R.id.pic_local_previous);
        nextBtn = (ImageButton)findViewById(R.id.pic_local_next);
        
        setBtn.setOnClickListener(click);
        deleteBtn.setOnClickListener(click);
        infoBtn.setOnClickListener(click);
        prvBtn.setOnClickListener(click);
        nextBtn.setOnClickListener(click);
        
        buildDialog();
        buildWarnDialog();
	}
	
	View.OnClickListener click = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.pic_local_previous:
				if(position == 0){
					return;
				}
				changePic(false);
				break;
			case R.id.pic_local_next:
				if(position == fileList.length-1){
					return;
				}
				changePic(true);
				break;
			case R.id.pic_local_info:
				handler.sendEmptyMessage(SHOW_INFO_DIALOG);
				break;
			case R.id.pic_local_delete:
				warnDialog.show();
				break;
			case R.id.pic_local_set:
				new Thread(){
					public void run() {
						setBitmapWallPaper();
						handler.sendEmptyMessage(SET_WALLPAPER_FINISH);
					};
				}.start();
				break;
			default:
				break;
			}
		}
	};
	
	private void changePic(boolean next){
		bitmap.recycle();
		if(next){
			bitmap = BitmapFactory.decodeFile(Constant.DIRECTORY_SDCARD+Constant.DIRECTORY_DOWNLOAD+fileList[++position]);
		}else{
			bitmap = BitmapFactory.decodeFile(Constant.DIRECTORY_SDCARD+Constant.DIRECTORY_DOWNLOAD+fileList[--position]);
		}
		imageView.setImageBitmap(bitmap);	
		matrix.setScale(1.0f, 1.0f);
		minZoom();
        center();
        imageView.setImageMatrix(matrix);
	}
	
	private void deleteFile(){
		File f = new File(Constant.DIRECTORY_SDCARD+Constant.DIRECTORY_DOWNLOAD+fileList[position]);
		bitmap.recycle();
		if(position == 0){
			String[] tmp = new String[fileList.length-1];
			for(int i=0;i<tmp.length;i++){
				tmp[i] = fileList[i+1];
			}
			fileList = tmp.clone();
			bitmap = BitmapFactory.decodeFile(Constant.DIRECTORY_SDCARD+Constant.DIRECTORY_DOWNLOAD+fileList[position]);
		}else{
			String[] tmp = new String[fileList.length-1];
			for(int i=0;i<position;i++){
				tmp[i] = fileList[i];
			}
			for(int i=position;i<fileList.length-1;i++){
				tmp[i] = fileList[i+1];
			}
			fileList = tmp.clone();
			bitmap = BitmapFactory.decodeFile(Constant.DIRECTORY_SDCARD+Constant.DIRECTORY_DOWNLOAD+fileList[--position]);
		}
		imageView.setImageBitmap(bitmap);
		matrix.setScale(1.0f, 1.0f);
		minZoom();
        center();
        imageView.setImageMatrix(matrix);
		f.delete();
	}
	
	private void buildWarnDialog(){
		Builder b = new Builder(this);
		b.setTitle(R.string.delete_pic);
		b.setMessage(R.string.delete_confirm);
		b.setPositiveButton(R.string.enter, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				handler.sendEmptyMessage(DELETE_FILE);
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
		warnDialog = b.create();
	}
	
	private void buildDialog(){
		Builder b = new Builder(this);
		b.setTitle(R.string.detailed_information);
		File f = new File(Constant.DIRECTORY_SDCARD+Constant.DIRECTORY_DOWNLOAD+fileList[position]);
		String[] list = new String[5];
		list[0] = getString(R.string.title)+": "+fileList[position];
		list[1] = getString(R.string.type)+": "+"JPEG";
		String unit = "";
		long length = f.length();
		if(length>1024){
			length = length/1024;
			unit = "KB";
		}
		if(length>1024){
			length = length/1024;
			unit = "MB";
		}
		list[2] = getString(R.string.size)+": "+(int)length+unit;
		list[3] = getString(R.string.location)+": "+Constant.DIRECTORY_SDCARD+Constant.DIRECTORY_DOWNLOAD;
		Calendar cal=Calendar.getInstance();  
		cal.setTimeInMillis(f.lastModified());  
		list[4] = getString(R.string.time)+": "+cal.getTime().toLocaleString();
		b.setItems(list, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
			}
		});
		b.setPositiveButton(R.string.enter, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		dialog = b.create();
	}
	
	private void setBitmapWallPaper(){
		try {
			setWallpaper(bitmap);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
     * 触屏监听
     */
    public boolean onTouch(View v, MotionEvent event) {
    	gestureDetector.onTouchEvent(event);
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
        // 主点按下
        case MotionEvent.ACTION_DOWN:
            savedMatrix.set(matrix);
            prev.set(event.getX(), event.getY());
            mode = DRAG;
            break;
        // 副点按下
        case MotionEvent.ACTION_POINTER_DOWN:
            dist = spacing(event);
            // 如果连续两点距离大于10，则判定为多点模式
            if (spacing(event) > 10f) {
                savedMatrix.set(matrix);
                midPoint(mid, event);
                mode = ZOOM;
            }
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_POINTER_UP:
            mode = NONE;
            break;
        case MotionEvent.ACTION_MOVE:
            if (mode == DRAG) {
                matrix.set(savedMatrix);
                matrix.postTranslate(event.getX() - prev.x, event.getY()
                        - prev.y);
            } else if (mode == ZOOM) {
                float newDist = spacing(event);
                if (newDist > 10f) {
                    matrix.set(savedMatrix);
                    float tScale = newDist / dist;
                    matrix.postScale(tScale, tScale, mid.x, mid.y);
                }
            }
            break;
        }
        imageView.setImageMatrix(matrix);
        CheckView();
        return true;
    }

    /**
     * 限制最大最小缩放比例，自动居中
     */
    private void CheckView() {
        float p[] = new float[9];
        matrix.getValues(p);
        if (mode == ZOOM) {
        	if(p[0] > minScaleR){
        		setPrvNextButtonVisible(false);
        	}
            if (p[0] < minScaleR) {
            	setPrvNextButtonVisible(true);
                matrix.setScale(minScaleR, minScaleR);
            }
            if (p[0] > MAX_SCALE) {
                matrix.set(savedMatrix);
            }
        }
        center();
    }
    
    private void  setPrvNextButtonVisible(boolean visible){
    	if(visible){
    		prvBtn.setVisibility(View.VISIBLE);
    		nextBtn.setVisibility(View.VISIBLE);
    	}else{
    		prvBtn.setVisibility(View.GONE);
    		nextBtn.setVisibility(View.GONE);
    	}
    }

    /**
     * 最小缩放比例，最大为100%
     */
    private void minZoom() {
        minScaleR = Math.min(
                (float) Constant.SCREEN_WIDTH / (float) bitmap.getWidth(),
                (float) Constant.SCREEN_HEIGHT / (float) bitmap.getHeight());
        if (minScaleR < 1.0) {
            matrix.postScale(minScaleR, minScaleR);
        }
    }

    private void center() {
        center(true, true);
    }

    /**
     * 横向、纵向居中
     */
    protected void center(boolean horizontal, boolean vertical) {

        Matrix m = new Matrix();
        m.set(matrix);
        RectF rect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
        m.mapRect(rect);

        float height = rect.height();
        float width = rect.width();

        float deltaX = 0, deltaY = 0;

        if (vertical) {
            // 图片小于屏幕大小，则居中显示。大于屏幕，上方留空则往上移，下方留空则往下移
            int screenHeight = Constant.SCREEN_HEIGHT;
            if (height < screenHeight) {
                deltaY = (screenHeight - height) / 2 - rect.top;
            } else if (rect.top > 0) {
                deltaY = -rect.top;
            } else if (rect.bottom < screenHeight) {
                deltaY = imageView.getHeight() - rect.bottom;
            }
        }

        if (horizontal) {
            int screenWidth = Constant.SCREEN_WIDTH;
            if (width < screenWidth) {
                deltaX = (screenWidth - width) / 2 - rect.left;
            } else if (rect.left > 0) {
                deltaX = -rect.left;
            } else if (rect.right < screenWidth) {
                deltaX = screenWidth - rect.right;
            }
        }
        matrix.postTranslate(deltaX, deltaY);
    }

    /**
     * 两点的距离
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    /**
     * 两点的中点
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }
    
    private class OnDoubleClick extends GestureDetector.SimpleOnGestureListener{
    	@Override
    	public boolean onDoubleTap(MotionEvent e) {
    		// TODO Auto-generated method stub
    		float p[] = new float[9];
            matrix.getValues(p);
    		if(p[0] > minScaleR){
    			setPrvNextButtonVisible(true);
    			 matrix.setScale(minScaleR, minScaleR);
    		}else{
    			setPrvNextButtonVisible(false);
    			matrix.setScale(1.0f, 1.0f);
    		}
//    		imageView.setImageMatrix(matrix);
    		center();
    		return super.onDoubleTap(e);
    	}
    	@Override
    	public boolean onSingleTapUp(MotionEvent e) {
    		// TODO Auto-generated method stub
//    		if(infoBtn.getVisibility() == View.VISIBLE){
//    			infoBtn.setVisibility(View.GONE);
//    		}else{
//    			infoBtn.setVisibility(View.VISIBLE);
//    		}
    		return super.onSingleTapUp(e);
    	}
    }
	
}
