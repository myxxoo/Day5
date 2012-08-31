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
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class Pic extends Activity implements OnTouchListener{
	private Intent intent;
	private String url,zoomUrl;
	private ImageView imgView;
	private Button paperBtn,downBtn;
	
//	private ImageLoader imageLoader;
	private final int IMG_LOAD_FINISH = 10;
	private final int SET_WALLPEPER_FINISH = 11;
	
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
	
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case IMG_LOAD_FINISH:
//				imageLoader.setZoomAble(false);
//				imageLoader.DisplayImage(url, imgView);
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
//		imageLoader = new ImageLoader(this);
	}
	
	private void initView(){
		imgView = (ImageView)findViewById(R.id.pic_img);
//		imageLoader.DisplayImage(zoomUrl, imgView);
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
//				imageLoader.downloadPic(url);
				Toast.makeText(Pic.this, R.string.download_succss, Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		}
	};
	
	/**
     * 触屏监听
     */
    public boolean onTouch(View v, MotionEvent event) {

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
//		imageLoader.memoryCacheClear();
		super.onDestroy();
	};
	
	private class SetWallPaper extends Thread{
		@Override
		public void run() {
			// TODO Auto-generated method stub
//			imageLoader.setBitmapWallPaper(url);
			handler.sendEmptyMessage(SET_WALLPEPER_FINISH);
		}
	}

	
}
