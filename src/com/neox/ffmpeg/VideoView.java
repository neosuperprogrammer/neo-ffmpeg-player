package com.neox.ffmpeg;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

public class VideoView extends View {
    private static final String LOG_TAG = VideoView.class.getSimpleName();
    
    
    public interface VideoPanel {
    	public void setPos(int curr, int total);
    	public void setPlay();
    	public void setPause();
    }
    
	private Bitmap mBitmap;
	private boolean initialized;
	private FFmpegCodec ffmpeg;
	private Handler mHandler;
	private Config bitmapConfig;

	private Context mContext;

	private boolean adjustRatio = false;

	private Paint mPaint;
	
    private VideoView(Context context) {
        super(context);
        mContext = context;
        Log.d("ffmpeg", "MoviePlayView()");
        initialized = false;
        bitmapConfig = Bitmap.Config.RGB_565;
        
        mPaint = new Paint();
//        mPaint.setAntiAlias(true);
//       mPaint.setStrokeWidth(6);
        mPaint.setTextSize(30);
//        mPaint.setTextAlign(Paint.Align.RIGHT);
        mPaint.setColor(Color.BLUE);
    }
    
        
    public VideoView(Context context, FFmpegCodec ffmpeg) {
		this(context);
		setFFmpegCodec(ffmpeg);
		mHandler = new Handler(context.getMainLooper());
	}

	public void setFFmpegCodec(FFmpegCodec codec) {
    	ffmpeg = codec;
    	mBitmap = Bitmap.createBitmap(ffmpeg.getWidth(), ffmpeg.getHeight(), bitmapConfig);
    	ffmpeg.setVideoView(this);
    	initialized = true;
    }
	
	Runnable refreshRunnable = new Runnable() {
		@Override
		public void run() {
			if(initialized) {
//				Log.i("ffmpeg", "refreshRunnable is called, invalidate!!!");
	   	        if(mVideoPanel != null) {
	   	        	mVideoPanel.setPos((int)ffmpeg.getCurrentPosition(), (int)ffmpeg.getDuration());
	   	        }
//	   	        if(mCanvas != null) {
//	   	        	onDraw(mCanvas);
//	   	        }
	    		ffmpeg.refreshVideo(mBitmap);
	   	        
//				invalidate();
			}
			else {
				Log.e("ffmpeg", "refreshRunnable is called, not initialized!!!");
				scheduleRefresh(100, 1);
			}
		}
	};
	
	Runnable timerRunnable = new Runnable() {
		@Override
		public void run() {
			if(initialized) {
//				Log.i("ffmpeg", "timerRunnable is called, refresh!!!");
				ffmpeg.refreshVideo(mBitmap);
			}
			else {
				Log.e("ffmpeg", "timerRunnable is called, not initialized!!!");
				scheduleRefresh(100, 1);
			}
		}
	};
	private VideoPanel mVideoPanel;
	private Canvas mCanvas;
	
	public void scheduleRefresh(int delay, int invalidate) {
//		Log.i("ffmpeg", "refreshVideo is called with delay [" + delay + "]ms");
		mHandler.removeCallbacks(refreshRunnable);
		mHandler.removeCallbacks(timerRunnable);
		mHandler.postDelayed(invalidate==1?refreshRunnable:timerRunnable, delay);
	}
	
	public static int getScreenWidth(Context ctx) {
		int width = 0;
		Display display = ((WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		width = display.getWidth();
//		LogUtil.w(LOG_TAG, "getScreenWidth = " + width);
		return width;
	}

	public static int getScreenHeight(Context ctx) {
		int height = 0;
		Display display = ((WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		height = display.getHeight();
//		LogUtil.w(LOG_TAG, "getScreenWidth = " + height);
		return height;
	}
	
    @Override
    protected void onDraw(Canvas canvas) {
    	mCanvas = canvas;
    	if(initialized) {
    		
//    		Log.i("ffmpeg", "draw!!!, width[" + mBitmap.getWidth() + "], height[" + mBitmap.getHeight() + "]");
    		if(ffmpeg.getWidth() != mBitmap.getWidth() || ffmpeg.getHeight() != mBitmap.getHeight()) {
    			mBitmap.recycle();
    			mBitmap = Bitmap.createBitmap(ffmpeg.getWidth(), ffmpeg.getHeight(), bitmapConfig);
    		}

//    		ffmpeg.refreshVideo(mBitmap);
    		
    		float aspectRatio = (float)mBitmap.getWidth() / (float)mBitmap.getHeight();
    		int screenWidth = getScreenWidth(mContext);
    		int screenHeight = getScreenHeight(mContext); 

    		int width = (int) (screenWidth);
    		int height = (int) screenHeight;
    		int left = 0;
    		int top = 0;
    		
    		if(adjustRatio) {
	    		height = (int) screenHeight;
	    		width = (int) (height * aspectRatio);
//	    		LogUtil.i(LOG_TAG, "bitmap width[" + mBitmap.getWidth() + ", bitmap heigth : " + mBitmap.getHeight() 
//	    				+ ", ratio[" + aspectRatio + "], width[" + width + "], height[" + height);
	    		if(width > screenWidth) {
	    			width = (int) screenWidth;
	    			height = (int) (width / aspectRatio);
	    		}
	    		left = (screenWidth - width) / 2;
	    		top = (screenHeight - height) / 2;
    		}
    		
//    		LogUtil.i(LOG_TAG, "ratio[" + aspectRatio + ", left[" + left + "], top[" + top + "], width[" + width + "], height[" + height);
   	        canvas.drawBitmap(mBitmap, new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight()), 
   					new Rect(left, top, width, height), null);
//   	        canvas.drawText(ffmpeg.getCurrTime() + "sec", 10, 200, mPaint);
   	        
   	        invalidate();
    	}
    }


	public void setVideoPanel(VideoPanel videoPanel) {
		mVideoPanel = videoPanel;
	}

}
