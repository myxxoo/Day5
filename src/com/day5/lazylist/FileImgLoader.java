package com.day5.lazylist;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.day5.utils.Constant;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

public class FileImgLoader {
	private Context context;
	private MemoryCache cache;
	public FileImgLoader(Context c){
		context = c;
		cache = new MemoryCache();
	}
	
	public void setImageBitmap(String path,ImageView imageView){
//	        imageViews.put(imageView, url);
	        Bitmap bitmap=cache.get(path);
	        if(bitmap!=null){
	            imageView.setImageBitmap(bitmap);
	        }
	        else
	        {	
	        	bitmap = decodeFile(new File(Constant.DIRECTORY_SDCARD+Constant.DIRECTORY_DOWNLOAD+path));
	            imageView.setImageBitmap(bitmap);
	            cache.put(path, bitmap);
	        }
	}
	
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
            while(true){
                if(width_tmp<REQUIRED_SIZE)
                    break;
                width_tmp/=2;
                height_tmp/=2;
                scale*=2;
            }
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize=scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {}
        return null;
    }
	
	public void clear(){
		cache.clear();
	}
	
	
}
