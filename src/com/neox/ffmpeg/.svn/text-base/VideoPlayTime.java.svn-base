package com.neox.ffmpeg;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.neox.ffmpeg.MediaProgress.MediaProgressControl;

public class VideoPlayTime extends FrameLayout implements MediaProgressControl {
	private static final String LOG_TAG = VideoPlayTime.class.getSimpleName();
	
	
	public final static int FADE_IN = 1;
	public final static int FADE_OUT = FADE_IN + 1;
	public final static int SHOW_UI = FADE_OUT + 1;
	public final static int SHOW_PROGRESS = SHOW_UI + 1;
	public final static int PLAY_START = SHOW_PROGRESS + 1;
	
	protected Context		mCtx;
	protected View			mContentView;
	protected MediaProgress	mProgress;

	protected TextView		mCurTime;
	protected TextView		mTotalTime;
	
	protected MediaControl mPlayer;
	
	private long			mTotalDuration;
	
	private boolean 		mDragging = false;
	private boolean 		mShowing = true;
	
	public interface PlayTimeControl {
		int getDuration();
		int getCurrentPosition();
		void seekTo(int pos);
		boolean isPlaying();
		int getBufferPercentage();
		boolean canPlay();
	}
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case FADE_IN:
					// View를 표시 한다.
					sendCommandDelayed(SHOW_UI, 0);
					break;
				case FADE_OUT:
					// 동작을 멈춘다. 					
					break;
				case SHOW_UI:
					setTotalTime(mPlayer.getDuration());
					setCurTime(mPlayer.getCurrentPosition());
					sendCommandDelayed(SHOW_PROGRESS,0);
					break;
				case SHOW_PROGRESS:
					if(mPlayer.getDuration() != 0) {
						// 보여지고 있는 경우 계속 그려야 한다.
						if (!mDragging) {
							setCurTime(mPlayer.getCurrentPosition());
//							sendCommandDelayed(SHOW_PROGRESS, 200);
						}
					}
					break;
				case PLAY_START:
					sendCommandDelayed(SHOW_PROGRESS, 0);
					break;
			}
		}
	};
	
	public void setPlayer(MediaControl player){
		mPlayer = player;
	}
	
	public boolean show(){
		show(0);
		return false;
	}
	
	public boolean show(int msc){
		sendCommandDelayed(FADE_IN, msc);
		return false;
	}
	
	public boolean hide(){
		hide(0);
		return false;
	}
	
	public boolean hide(int msc){
		sendCommandDelayed(FADE_OUT, msc);
		return false;
	}
	
	private boolean drawProgress(float percent){
		sendCommandDelayed(SHOW_PROGRESS, 0);
		return false;
	}
	
	public void sendCommandDelayed(int commandType, int milliSeconds) { // 진행중인 Order은 삭제한다.
		mHandler.removeMessages(commandType);
		
		Message msg = mHandler.obtainMessage(commandType);
		mHandler.sendMessageDelayed(msg, milliSeconds);
	}
	
	public VideoPlayTime(Context context) {
		super(context);
		init(context);
	}
	
	public VideoPlayTime(Context context, AttributeSet attrSet) {
		super(context, attrSet);
		init(context);
	}
	
	public VideoPlayTime(Context context, AttributeSet attrSet, int defStyle) {
		super(context, attrSet,defStyle);
		init(context);
	}
	
	public void init(Context context) {
		mCtx = context;
		LayoutInflater ll = LayoutInflater.from(context);
		mContentView = ll.inflate(R.layout.videoplaytime, null);
		
		mCurTime = (TextView) mContentView.findViewById(R.id.playtime_text_curtime);
		mTotalTime = (TextView) mContentView.findViewById(R.id.playtime_text_totaltime);
		
		mProgress = new MediaProgress(mCtx);
		((FrameLayout)(mContentView.findViewById(R.id.mediactl_progress))).addView(mProgress);
		mProgress.setMediaProgressCtl(this);
		
		mProgress.setProgressType(MediaProgress.PROGRESSTYPE_PLAYBAR);
		
		addView(mContentView);
	}	
	
	public void setTime(long currTime, long totalTime) {
//		LogUtil.e(LOG_TAG, "curr time : " + currTime + ", total time : " + totalTime);
		setCurTime(currTime);
		setTotalTime(totalTime);
	}
	
	public void setCurTime(long time) {
		
		if(time >= mTotalDuration) {
			String timeStr = Util.timeToString(mTotalDuration);
			mCurTime.setText(timeStr);			
		} 
		else {		
			String timeStr = Util.timeToString(time);
			mCurTime.setText(timeStr);
		}
		if(!mDragging) {
			setProgressPos(time);
		}
	}

	public void setTotalTime(long time) {
		if(time >= 0) { 
			String timeStr = Util.timeToString(time);
			mTotalTime.setText(timeStr);
			mTotalDuration = time;
		}
	}
	
	public void setProgressPos(long time) {
		float percent = (float)((float)time / (float)mTotalDuration);
		if(mProgress != null && !mDragging) {			
			mProgress.setProgressPos(percent);
		}
	}
	
	public void updateUi(long curTime) {
		setCurTime(curTime);
		if(!mDragging) {
			setProgressPos(curTime);		
		}
	}
	
	public boolean isDragging() {
		return mDragging;
	}
	
	public void setShowingState(boolean isShow) {
		mShowing = isShow;		
	}
	
	@Override
	public void moveProgress(float percent) {
		mPlayer.seeking();
	}
	
	@Override
	public void moveProgressStart() {
		mDragging = true;		
	}
	
	@Override
	public void moveProgressEnd(float percent) {
		mDragging = false;
		LogUtil.e(LOG_TAG, "percent = " + percent);
		
		if(mPlayer != null && mShowing) {
			int movePos = (int)((float)mPlayer.getDuration() * percent);
			LogUtil.e(LOG_TAG,"duration = " + mPlayer.getDuration() + ", movePos = " + movePos);
			mPlayer.seekTo(movePos);
			drawProgress(percent);
		}
		else {
			drawProgress(percent);
		}
	}	
}
