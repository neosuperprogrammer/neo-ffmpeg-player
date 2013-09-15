package com.neox.ffmpeg;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class FFmpegBasicActivity extends Activity {
	private static final String LOG_TAG = FFmpegBasicActivity.class.getSimpleName();
	
	private FFmpegCodec ffmpeg;
	private float prevX;
	private float prevY;
	
	static Handler _handler = new Handler();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.e("ffmpeg", "FFmpegBasicActivity onCreate()");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); 
        super.onCreate(savedInstanceState);
        
        Intent intent = getIntent();
		String path = intent.getStringExtra("path");
		LogUtil.e(LOG_TAG, "path : " + path);

        
        ffmpeg = FFmpegCodec.getInstance(getApplicationContext());
        
        if(!ffmpeg.openVideo(path)) {
        	Log.e("ffmpeg", "ffmpeg.openVideo Failed [" + path + "]");
        	Toast.makeText(getApplicationContext(), "동영상 열기가 실패했습니다.", Toast.LENGTH_LONG).show();
        	finish();
        	return;
        }
        
        ffmpeg.setVideoDecodeEndListener(new FFmpegCodec.VideoDecodeEndListener() {
			
			@Override
			public void decodeEnded() {
				Log.e("ffmpeg", "Video Decode Ended, finish Activity!!!!!!");
				_handler.postDelayed(new Runnable() {
					@Override
					public void run() {
//						ffmpeg.closeVideo();
						ffmpeg.close();
						finish();
					}
				}, 100);
				
			}
		});
        
        VideoView videoView = new VideoView(getApplicationContext(), ffmpeg);
        setContentView(videoView);

		ffmpeg.startDecodeThread();
		ffmpeg.startVideoThread();
		ffmpeg.startAudioThread();
		ffmpeg.setVideoDisplayTimer(100, 0, -1);
		
    }

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();
		if(event.getAction() == MotionEvent.ACTION_DOWN) {
			prevX = x;
			prevY = y;
		}
		else if(event.getAction() == MotionEvent.ACTION_UP) {
			int delta = (int) (x - prevX); 
			if(Math.abs(delta) < 20) {
				ffmpeg.toggle();
			}
			else {
				if(!ffmpeg.isPaused()) {
					if(delta > 0) {
						ffmpeg.seek(10);
					}
					else {
						ffmpeg.seek(-10);
					}
				}
			}
		}
		return super.onTouchEvent(event);
	}

	@Override
	protected void onResume() {
		Log.e("ffmpeg", "FFmpegBasicActivity onResume()");
		super.onResume();
	}

	@Override
	protected void onPause() {
		Log.e("ffmpeg", "FFmpegBasicActivity onPause()");
		super.onPause();
	}

	@Override
	public void onBackPressed() {
		Log.e("ffmpeg", "FFmpegBasicActivity onBackPressed()");
		ffmpeg.closeVideo();
//		super.onBackPressed();
	}

	@Override
	protected void onDestroy() {
		Log.e("ffmpeg", "FFmpegBasicActivity onDestroy()");
		super.onDestroy();
	}
	
}
