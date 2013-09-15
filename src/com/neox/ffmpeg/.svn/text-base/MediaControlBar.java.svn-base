package com.neox.ffmpeg;

import android.content.Context;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.neox.ffmpeg.MediaProgress.MediaProgressControl;

public class MediaControlBar extends FrameLayout implements MediaProgressControl {
	
	/** VIDEO용 ToolBar */
	public final static int CONTROLBAR_TYPE_VIDEO			= 		0x000001;
	/** << >> 버튼클릭시 이동할 seek Time */
//	private final static int SEEK_TIME_UNIT					= 		10000;
	
	private static final int FADE_OUT = 1;
	private static final int FADE_IN = FADE_OUT + 1;
	private static final int SHOW_UI = FADE_IN + 1;
	private static final String LOG_TAG = MediaControlBar.class.getSimpleName();
	
	private MediaControl mPlayer;	
	private Context 	mCtx;		
	private View 		mContentView;
	private View 		mAnchor;
	
	private ImageButton 	mPauseButton;
	private ImageButton 	mPlayButton;
	private ImageButton 	mFfwdButton;
	private ImageButton 	mRewButton;
	private ImageButton 	mFullSizeButton;
	
	private MediaProgress	mProgress;
	private boolean 		mShowing;
	private boolean 		isShowView = true;
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case FADE_OUT:
					hide();
					break;
				case SHOW_UI:										
					// 플레이버튼을 업데이트 와 프로그레스를 그린다.
					updatePlayTimeAndPausePlay();
//					TcloudPreferences.getInstance().saveProgressWidth(mCtx, MediaProgress.getProgressWidth());
					break;
			}
		}
	};
	
	private ImageButton.OnClickListener mClickListener = new ImageButton.OnClickListener(){
		@Override
		public void onClick(View v) {
			if(!isShowing()) {
				LogUtil.v(LOG_TAG, "not showing contolbar");
				return;
			}
			switch(v.getId()) {
				case R.id.mediactl_btn_play:
					LogUtil.e(LOG_TAG, "play button");
					mPlayer.pause();
//					if(!mPlayer.start()) {						
//						mPlayer.setMedia(false);
//					}
					break;
				case R.id.mediactl_btn_pause:
					LogUtil.e(LOG_TAG, "pause button");
					mPlayer.pause();
					break;
//				case R.id.mediactl_btn_prev:
//					mPlayer.prev();
//					break;
//				case R.id.mediactl_btn_next:
//					mPlayer.next();
//					break;
				case R.id.mediactl_btn_fullsize:
					mPlayer.changeSize();
					break;
			}
		}		
	};	

	public MediaControlBar(Context context) {
		super(context);
		mCtx = context;
	}
	
	public MediaControlBar(Context context, AttributeSet attrSet) {
		super(context, attrSet);
		mCtx = context;
	}

	public MediaControlBar(Context context, AttributeSet attrSet,int defStyle) {
		super(context, attrSet, defStyle);
		mCtx = context;
	}
	
	public void setType(int toolbarType){
		LayoutInflater ll = LayoutInflater.from(mCtx);
		
		switch(toolbarType) {
			case CONTROLBAR_TYPE_VIDEO:
				mContentView = ll.inflate(R.layout.mediactl_video, null);
				mFullSizeButton = (ImageButton) mContentView.findViewById(R.id.mediactl_btn_fullsize);
				mFullSizeButton.setOnClickListener(mClickListener);
				break;
		}
		
		mFfwdButton = (ImageButton) mContentView.findViewById(R.id.mediactl_btn_next);
		mRewButton = (ImageButton) mContentView.findViewById(R.id.mediactl_btn_prev);
		mPauseButton = (ImageButton) mContentView.findViewById(R.id.mediactl_btn_pause);
		mPlayButton = (ImageButton) mContentView.findViewById(R.id.mediactl_btn_play);
				
		mFfwdButton.setOnClickListener(mClickListener);
		mRewButton.setOnClickListener(mClickListener);
		mPauseButton.setOnClickListener(mClickListener);
		mPlayButton.setOnClickListener(mClickListener);
		mFfwdButton.setSoundEffectsEnabled(false);
		mRewButton.setSoundEffectsEnabled(false);
		mPauseButton.setSoundEffectsEnabled(false);
		mPlayButton.setSoundEffectsEnabled(false);
		
		mProgress = new MediaProgress(mCtx);
		mProgress.setProgressType(MediaProgress.PROGRESSTYPE_VOLUME);
		((FrameLayout) mContentView.findViewById(R.id.mediactl_progress)).addView(mProgress);
		mProgress.setMediaProgressCtl(this);
		addView(mContentView);
		mProgress.setSoundEffectsEnabled(false);
		mContentView.setSoundEffectsEnabled(false);
		
		Message msg = mHandler.obtainMessage(SHOW_UI);
		mHandler.sendMessageDelayed(msg, 1000);
	}

	public void setMediaPlayer(MediaControl player) {
		mPlayer = player;
		//updatePausePlay();
	}

	@Override
	public void moveProgress(float percent) {
		// 볼륨 조절
		calculateVolumeAndSetMediaVol(percent);
		if(mPlayer != null) {
			mPlayer.seeking();
		}
	}	

	@Override
	public void moveProgressStart() {		
	};

	@Override
	public void moveProgressEnd(float percent) {
		LogUtil.d(LOG_TAG, "isPlaying = " + mPlayer.isPlaying());
		calculateVolumeAndSetMediaVol(percent);
		if(mPlayer != null) {
			mPlayer.seekingEnd();
		}
	};
		
	/**	 
	 * 미디어 볼륨을 설정	 
	 */
	public static void setMediaVolume(Context context, int valueLevel) {
		((AudioManager)(context.getSystemService(Context.AUDIO_SERVICE))).setStreamVolume(AudioManager.STREAM_MUSIC, valueLevel , 0);
	}
	
	/**
	 * 현재 미디어 볼륨값을 가져옴	 
	 */
	public static int getMediaVolume(Context context) {
		return ((AudioManager)(context.getSystemService(Context.AUDIO_SERVICE))).getStreamVolume(AudioManager.STREAM_MUSIC);
	}
	
	private void calculateVolumeAndSetMediaVol(float percent) {
		int maxVolume = ((AudioManager)(mCtx.getSystemService(Context.AUDIO_SERVICE))).getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		float volumeUnit = (float)100 / (float)maxVolume; 
		int volumeLevel = (int) ( ( percent * 100 ) / volumeUnit );		
		
		if(volumeLevel > maxVolume) {
			volumeLevel = maxVolume;
			percent = 100;
		}
		else {			
			percent = (int) ((int)(float)volumeLevel * volumeUnit);
		}
		LogUtil.d(LOG_TAG, "maxVolume = " + maxVolume + "volumeLevel = " + volumeLevel + ", percent = " + percent);
		
		setMediaVolume(mCtx, volumeLevel);
		
		mProgress.setProgressPos(percent / 100);
	}
	
	public void updatePlayTimeAndPausePlay() {
		int maxVolume = ((AudioManager)(mCtx.getSystemService(Context.AUDIO_SERVICE))).getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		float volumeUnit = (float)100 / (float)maxVolume; 
		int volumeLevel = getMediaVolume(mCtx);
		float volumePercent = (int) ((int)(float)volumeLevel * volumeUnit);
		
		//약간에 보정이 필요해서 수정함 by jcpark
		float percent = volumePercent / 100;
		if(percent > 0.9 && percent < 1) {
			percent = 0.9f;
		}
		
		mProgress.setProgressPos(percent);
		
		LogUtil.d(LOG_TAG, "volumeLevel = " + volumeLevel + ", volumePercent = " + volumePercent);
		
		if (mPlayButton == null || mPauseButton == null) {
			return;
		}
		
		if (mPlayer != null && mPlayer.isPlaying()) {
			// pause 이미지로 변경
			mPlayButton.setVisibility(ImageButton.INVISIBLE);
			mPauseButton.setVisibility(ImageButton.VISIBLE);
		} else {
			// play 이미지로 변경
			 mPlayButton.setVisibility(ImageButton.VISIBLE);
			mPauseButton.setVisibility(ImageButton.INVISIBLE);
		}
	}	
	
	public void show(int timeout) {
		if (!mShowing && mAnchor != null) {
			//setProgress();
			if (mPauseButton != null) 
				mPauseButton.requestFocus();
			
			disableUnsupportedButtons();
			int [] anchorpos = new int[2];
			mAnchor.getLocationOnScreen(anchorpos);
				
			WindowManager.LayoutParams p = new WindowManager.LayoutParams();
			p.gravity = Gravity.TOP;
			p.width = mAnchor.getWidth();
			p.height = LayoutParams.WRAP_CONTENT;
			p.x = 0;
			p.y = anchorpos[1] + mAnchor.getHeight() - p.height;
			p.format = PixelFormat.TRANSLUCENT;
			p.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
			p.flags |= WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
			p.token = null;
			p.windowAnimations = 0; // android.R.style.DropDownAnimationDown;
			//mWindowManager.addView(mDecor, p);
			mShowing = true;
		}
		updatePlayTimeAndPausePlay();
		
		Message msg = mHandler.obtainMessage(FADE_OUT);
		if (timeout != 0) {
			mHandler.removeMessages(FADE_OUT);
		 	mHandler.sendMessageDelayed(msg, timeout);
		}
	}
	
	private void disableUnsupportedButtons() {
		try {
			if (mPlayButton != null && !mPlayer.canPlay())
				mPlayButton.setEnabled(false);
			if (mPauseButton != null && !mPlayer.canPause())
				mPauseButton.setEnabled(false);
			if (mRewButton != null && !mPlayer.canSeekBackward()) 
				mRewButton.setEnabled(false);
			if (mFfwdButton != null && !mPlayer.canSeekForward()) 
				mFfwdButton.setEnabled(false);
			if (mFullSizeButton != null && !mPlayer.isPlaying()) 
				mFullSizeButton.setEnabled(false);			
		} 
		catch (IncompatibleClassChangeError e) {
			e.printStackTrace();
		}
	}
	
	public boolean isShowing(){
		return isShowView;
	}
	
	public void setShowState(boolean state) {
		isShowView = state;
	}

	public void show() {
		show(0);
	};

	public void hide() {
		
	};

	public void setAnchorView(View view) {
	};
	
}
