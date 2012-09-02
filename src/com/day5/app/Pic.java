package com.day5.app;


import java.io.File;
import java.util.Calendar;

import com.day5.lazylist.ImageLoader;
import com.day5.utils.Constant;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.Toast;

public class Pic extends Activity implements OnTouchListener{
	private Intent intent;
	private String url,name;
	private ImageView imgView;
	private Button paperBtn,downBtn;
	private ImageButton infoBtn;
	
	private ImageLoader imageLoader;
	private final int IMG_LOAD_FINISH = 10;
	private final int SET_WALLPEPER_FINISH = 11;
	private final int SHOW_INFO_DIALOG = 12;
	private final int DOWNLOAD_IMG = 13;
	
	Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();
    Bitmap bitmap;

    float minScaleR;
    static final float MAX_SCALE = 4f;

    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    PointF prev = new PointF();
    PointF mid = new PointF();
    float dist = 1f;
    
    private AlertDialog dialog;
    private GestureDetector gestureDetector;
    private MSGReceiver receiver = new MSGReceiver();
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case IMG_LOAD_FINISH:
				imageLoader.setImage(url, imgView);
				break;
			case SET_WALLPEPER_FINISH:
				Toast.makeText(Pic.this, R.string.set_success, Toast.LENGTH_SHORT).show();
				break;
			case SHOW_INFO_DIALOG:
				buildDialog();
				dialog.show();
				break;
			case DOWNLOAD_IMG:
				imageLoader.downloadPic(url);
				Toast.makeText(Pic.this, R.string.download_succss, Toast.LENGTH_SHORT).show();
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
		name = intent.getStringExtra("name");
		imageLoader = new ImageLoader(this);
		registerReceiver(receiver, new IntentFilter("android.intent.action.IMG_LOAD_FINISH"));
		gestureDetector = new GestureDetector(this,new OnDoubleClick());
	}
	
	private void initView(){
		imgView = (ImageView)findViewById(R.id.pic_img);
		bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image_loading);
		imgView.setImageBitmap(bitmap);
		paperBtn = (Button)findViewById(R.id.pic_set);
		downBtn = (Button)findViewById(R.id.pic_down);
		infoBtn = (ImageButton)findViewById(R.id.pic_info);
		
		imgView.setOnTouchListener(this);
		imgView.setImageMatrix(matrix);
		paperBtn.setOnClickListener(click);
		downBtn.setOnClickListener(click);
		infoBtn.setOnClickListener(click);
		infoBtn.setVisibility(View.GONE);
		minZoom();
		center();
	}
	
	Thread loadImg = new Thread(){
		public void run() {
			handler.sendEmptyMessage(IMG_LOAD_FINISH);
			imageLoader.setZoomAble(false);
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
				handler.sendEmptyMessage(DOWNLOAD_IMG);
				break;
			case R.id.pic_info:
				handler.sendEmptyMessage(SHOW_INFO_DIALOG);
				break;
			default:
				break;
			}
		}
	};
	
	private void buildDialog(){
		Builder b = new Builder(this);
		b.setTitle(R.string.detailed_information);
		String[] list = new String[3];
		list[0] = getString(R.string.type)+": "+"JPEG";
		String unit = "";
		long length = imageLoader.getImgByteSize();
		if(length>1024){
			length = length/1024;
			unit = "KB";
		}
		if(length>1024){ 
			length = length/1024;
			unit = "MB";
		}
		list[1] = getString(R.string.size)+": "+length+unit;
		list[2] = getString(R.string.measurement)+": "+bitmap.getWidth()+" x "+bitmap.getHeight();
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
	
	long getSizeInBytes(Bitmap bitmap) {
        if(bitmap==null)
            return 0;
        return bitmap.getRowBytes() * bitmap.getHeight();
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
        imgView.setImageMatrix(matrix);
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
            if (p[0] < minScaleR) {
                matrix.setScale(minScaleR, minScaleR);
            }
            if (p[0] > MAX_SCALE) {
                matrix.set(savedMatrix);
            }
        }
        center();
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
                deltaY = imgView.getHeight() - rect.bottom;
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
	
	
	protected void onDestroy() {
		imageLoader.memoryCacheClear();
		bitmap.recycle();
		unregisterReceiver(receiver);
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
	
    public class MSGReceiver extends BroadcastReceiver{

    	@Override
    	public void onReceive(Context context, Intent intent) {
    		// TODO Auto-generated method stub
    		String action = intent.getAction();
    		if("android.intent.action.IMG_LOAD_FINISH".equals(action)){
    			bitmap = imageLoader.getMemoryBitmap(url);
    			matrix.setScale(1.0f, 1.0f);
    			minZoom();
    			center();
    			imgView.setImageMatrix(matrix);
    			infoBtn.setVisibility(View.VISIBLE);
    		}
    	}

    }
    
    private class OnDoubleClick extends GestureDetector.SimpleOnGestureListener{
    	@Override
    	public boolean onDoubleTap(MotionEvent e) {
    		// TODO Auto-generated method stub
    		float p[] = new float[9];
            matrix.getValues(p);
    		if(p[0] > minScaleR){
    			 matrix.setScale(minScaleR, minScaleR);
    		}else{
    			matrix.setScale(1.0f, 1.0f);
    		}
    		center();
    		return super.onDoubleTap(e);
    	}
    	@Override
    	public boolean onSingleTapUp(MotionEvent e) {
    		// TODO Auto-generated method stub
    		return super.onSingleTapUp(e);
    	}
    }

	
}
