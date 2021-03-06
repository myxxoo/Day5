package com.day5.lazylist;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.day5.app.R;
import com.day5.utils.Constant;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.sax.StartElementListener;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

public class ImageLoader {
    
    MemoryCache memoryCache=new MemoryCache();
    FileCache fileCache;
    private Map<ImageView, String> imageViews=Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    ExecutorService executorService; 
    private Animation rotate;
    private LinearInterpolator interpolator;
    private boolean zoomAble = true;
    private Context activity;
    private long imgByteSize = 0;
    
	public ImageLoader(Context context){
        fileCache=new FileCache(context);
        executorService=Executors.newFixedThreadPool(5);
        rotate =  AnimationUtils.loadAnimation(context, R.drawable.animation_rotate);
        interpolator = new LinearInterpolator();
        rotate.setInterpolator(interpolator);
        activity = context;
    }
    
    final int stub_id=R.drawable.loading;
    public void DisplayImage(String url, ImageView imageView)
    {
        imageViews.put(imageView, url);
        Bitmap bitmap=memoryCache.get(url);
        if(bitmap!=null){
        	imageView.clearAnimation();
            imageView.setImageBitmap(bitmap);
        }
        else
        {	
        	imageView.setImageResource(stub_id);
        	imageView.startAnimation(rotate);
            queuePhoto(url, imageView);
        }
    }
    
    public void setImage(String url, ImageView imageView)
    {
    	imageViews.put(imageView, url);
        Bitmap bitmap=getBitmap(url);
        if(bitmap!=null){
            imageView.setImageBitmap(bitmap);
            memoryCache.put(url, bitmap);
            if(!zoomAble){
            	Intent intent = new Intent("android.intent.action.IMG_LOAD_FINISH");
                activity.sendBroadcast(intent);
            }
        }
        else
        {	
            queuePhoto(url, imageView);
        }
    }
        
    public void setBitmapWallPaper(String url){
    	Bitmap bitmap = memoryCache.get(url);
    	try {
			activity.setWallpaper(bitmap);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    
    public void downloadPic(String url){
    	Bitmap bitmap=memoryCache.get(url);
		try {
			File file = new File(android.os.Environment.getExternalStorageDirectory(),Constant.DIRECTORY_DOWNLOAD+url.hashCode()+".jpg");
	    	FileOutputStream bos = new FileOutputStream(file);
	    	bitmap.compress(CompressFormat.JPEG, 100, bos);
	    	bos.flush();
	    	bos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			Log.i("Day5", "文件未找到，地址可能失效");
		} catch (IOException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			Log.i("Day5", "文件未找到，地址可能失效");
		}
    }
    
    public Bitmap getMemoryBitmap(String url){
    	return memoryCache.get(url);
    }
    
    public long getImgByteSize() {
		return imgByteSize;
	}
    
    private void queuePhoto(String url, ImageView imageView)
    {
        PhotoToLoad p=new PhotoToLoad(url, imageView);
        executorService.submit(new PhotosLoader(p));
    }
    
    private Bitmap getBitmap(String url) 
    {
        File f=fileCache.getFile(url);
        
        //from SD cache
        Bitmap b = decodeFile(f);
        if(b!=null){
        	imgByteSize = f.length();
            return b;
        }
        //from web
        try {
            Bitmap bitmap=null;
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);
            InputStream is=conn.getInputStream();
            OutputStream os = new FileOutputStream(f);
            Utils.CopyStream(is, os);
            os.close();
            bitmap = decodeFile(f);
            imgByteSize = f.length();
            return bitmap;
        } catch (Exception ex){
//           ex.printStackTrace();
        	Log.i("Day5", "文件未找到，地址可能失效");
           return null;
        }
    }

    //decodes image and scales it to reduce memory consumption
    private Bitmap decodeFile(File f){
        try {
            //decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f),null,o);
            
            //Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE = Constant.SCREEN_WIDTH;
            int width_tmp=o.outWidth, height_tmp=o.outHeight;
            int scale=1;
            while(true && zoomAble){
                if(width_tmp<REQUIRED_SIZE)
                    break;
                width_tmp/=2;
                height_tmp/=2;
                scale*=2;
            }
            
            //decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize=scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {}
        return null;
    }
    
    //Task for the queue
    private class PhotoToLoad
    {
        public String url;
        public ImageView imageView;
        public PhotoToLoad(String u, ImageView i){
            url=u; 
            imageView=i;
        }
    }
    
    class PhotosLoader implements Runnable {
        PhotoToLoad photoToLoad;
        PhotosLoader(PhotoToLoad photoToLoad){
            this.photoToLoad=photoToLoad;
        }
        
        @Override
        public void run() {
            if(imageViewReused(photoToLoad))
                return;
            Bitmap bmp=getBitmap(photoToLoad.url);
            memoryCache.put(photoToLoad.url, bmp);
            if(imageViewReused(photoToLoad))
                return;
            BitmapDisplayer bd=new BitmapDisplayer(bmp, photoToLoad);
            Activity a=(Activity)photoToLoad.imageView.getContext();
            a.runOnUiThread(bd);
        }
    }
    
    boolean imageViewReused(PhotoToLoad photoToLoad){
        String tag=imageViews.get(photoToLoad.imageView);
        if(tag==null || !tag.equals(photoToLoad.url))
            return true;
        return false;
    }
    
    //Used to display bitmap in the UI thread
    class BitmapDisplayer implements Runnable
    {
        Bitmap bitmap;
        PhotoToLoad photoToLoad;
        public BitmapDisplayer(Bitmap b, PhotoToLoad p){bitmap=b;photoToLoad=p;}
        public void run()
        {
            if(imageViewReused(photoToLoad))
                return;
            if(bitmap!=null){
            	photoToLoad.imageView.clearAnimation();
                photoToLoad.imageView.setImageBitmap(bitmap);
                if(!zoomAble){
                	Intent intent = new Intent("android.intent.action.IMG_LOAD_FINISH");
                    activity.sendBroadcast(intent);
                }
            }else{
                photoToLoad.imageView.setImageResource(stub_id);
            }
        }
    }
    
    public boolean isZoomAble() {
		return zoomAble;
	}

	public void setZoomAble(boolean zoomAble) {
		this.zoomAble = zoomAble;
	}
	
	public void memoryCacheClear(){
		memoryCache.clear();
	}
	
    public void clearCache() {
        memoryCache.clear();
        fileCache.clear();
    }

}
