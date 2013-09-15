package com.neox.ffmpeg;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.NinePatch;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

public class MediaProgress extends FrameLayout {
	protected static final String LOG_TAG = MediaProgress.class.getSimpleName();
	public static final int		PROGRESSTYPE_PLAYBAR = 0x000001;
	public static final int		PROGRESSTYPE_VOLUME = PROGRESSTYPE_PLAYBAR + 0x000001;
	protected static final int PROGRESS_COMPLETED = 0;
	
	private Bitmap 			IMG_PROGRESS_ITEM;
	private NinePatch 		IMG_PROGRESS_BACK_FOCUS;
	private NinePatch 		IMG_PROGRESS_BACK_DIMMED;

	
	/* 터치 영역을 조정해야 하는 이유가 생길지도 모르니 추가*/
	private View			mParentView = null;	
	
	@SuppressWarnings("unused")
	private Context			mCtx;
	private long 			mPos;
	private float 			mPosPercent;
	
	private int				mXGap;
	private int 			mYGap;
	private int 			mBgHight;
	
	private float			mPercent;
	private boolean			mIsLoadScreen = false;
		
	private MediaProgressControl mProgressCtl = null;
	private int mType;	
	private static int mWidth;
	
		
	private boolean drawImg(Canvas canvas, long pos) {		
		if(pos <= 0) {
			if(!mIsLoadScreen && getWidth() > 0) {
				mPos = (long) (getWidth() * mPercent);
				mIsLoadScreen = true;
			} else pos = 0;
		}
		
		// 딤드 이미지를 먼저 그림
		IMG_PROGRESS_BACK_DIMMED.draw(canvas, new Rect( mXGap ,mYGap, getWidth() - mXGap , mYGap + mBgHight), null);
		
		// 프로그래스가 진행된 상태에서는 포커스 이미지를 그림.
		if((getWidth() - pos) > mXGap && pos > mXGap) {
			IMG_PROGRESS_BACK_FOCUS.draw(canvas, new Rect( mXGap ,mYGap, (int) pos, mYGap + mBgHight), null);
			if(mType == PROGRESSTYPE_VOLUME) {
				//LogUtil.d(LOG_TAG, "[1] pos = " + pos);
			}
		}
		else if((getWidth() - pos) <= mXGap ) {
			IMG_PROGRESS_BACK_FOCUS.draw(canvas, new Rect( mXGap ,mYGap, getWidth() - mXGap, mYGap + mBgHight), null);
			if(mType == PROGRESSTYPE_VOLUME) {
				//LogUtil.d(LOG_TAG, "[2] getWidth() - mXGap = " + (getWidth() - mXGap));
			}
		}
		
		// 프로그래스 버튼 이미지를 그림.
		if(pos < mXGap)
			canvas.drawBitmap(IMG_PROGRESS_ITEM, 0, 0, null);
		else if(pos > getWidth() - mXGap )
			canvas.drawBitmap(IMG_PROGRESS_ITEM, getWidth() - mXGap - mXGap, 0, null);
		else 
			canvas.drawBitmap(IMG_PROGRESS_ITEM, pos - mXGap, 0 , null);
		
		return false;		
	}

	@Override
	protected void onDraw(Canvas canvas){
//		if(mParentView == null) {
//			mParentView = (View) this.getParent();
//			mParentView.setOnTouchListener(mThouchListener);
//		}
		
		drawImg(canvas, mPos);		
	}

	private void init(Context context) {
		mCtx = context;
		setLayoutParams(new LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.FILL_PARENT));
		this.setOnTouchListener(mThouchListener);
	}
	
	public void setProgressType(int type) {
		mType = type;		
		switch(type) {
			case PROGRESSTYPE_PLAYBAR:
				IMG_PROGRESS_ITEM = BitmapFactory.decodeResource(getResources(), R.drawable.player_bar_process_btn);
				IMG_PROGRESS_BACK_FOCUS = new NinePatch(BitmapFactory.decodeResource(getResources(), R.drawable.player_bar_process_bar), 
						BitmapFactory.decodeResource(getResources(), R.drawable.player_bar_process_bar).getNinePatchChunk(), 
						"");
				IMG_PROGRESS_BACK_DIMMED = new NinePatch(BitmapFactory.decodeResource(getResources(), R.drawable.player_bar_process_bg), 
						BitmapFactory.decodeResource(getResources(), R.drawable.player_bar_process_bg).getNinePatchChunk(), 
						"");
				mBgHight = 11;
				break;
			case PROGRESSTYPE_VOLUME:
				IMG_PROGRESS_ITEM = BitmapFactory.decodeResource(getResources(), R.drawable.player_sound_process_btn);
				IMG_PROGRESS_BACK_FOCUS = new NinePatch(BitmapFactory.decodeResource(getResources(), R.drawable.player_sound_process_bar), 
						BitmapFactory.decodeResource(getResources(), R.drawable.player_sound_process_bar).getNinePatchChunk(), 
						"");
				IMG_PROGRESS_BACK_DIMMED = new NinePatch(BitmapFactory.decodeResource(getResources(), R.drawable.player_sound_process_bg), 
						BitmapFactory.decodeResource(getResources(), R.drawable.player_sound_process_bg).getNinePatchChunk(), 
						"");
				mBgHight = 13;
				break;
		}
		
		mXGap = (IMG_PROGRESS_ITEM.getWidth() / 2);
		mYGap = (IMG_PROGRESS_ITEM.getHeight() - mBgHight) / 2;
		
		setWillNotDraw(false);
	}
	
	public void setMediaProgressCtl(MediaProgressControl mpc) {
		mProgressCtl = mpc;
	}

	public void setProgressPos(float percent) {				
		if(getWidth() != 0) {
			mPos = (long) (getWidth() * percent);
			mWidth = getWidth();
		}
		else {
			mPos = (long) (mWidth * percent);			
		}
				
//		if(mType == PROGRESSTYPE_VOLUME) {
//			mWidth = TcloudPreferences.getInstance().loadProgressWidth(mCtx);
//			if(mWidth < getWidth()) {
//				mWidth = getWidth();		
//				TcloudPreferences.getInstance().saveProgressWidth(mCtx, mWidth);
//			}
//			mPos = (long) (mWidth * percent);		
//			LogUtil.d(LOG_TAG, "percent = " + percent + ", mPos = " + mPos + ", mWidth = " + mWidth);
//		}
			
//		invalidate();
	}
	
	public float setProgressPos(int pos) {
		mPos = pos;
		
		invalidate();
		
		return (float) ((float)pos / (float) getWidth());
	}

	private OnTouchListener mThouchListener = new OnTouchListener(){

		public boolean onTouch(View v, MotionEvent event) {
			//LogUtil.d(LOG_TAG, "action : " + event.getAction());
			if(event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
				if(mProgressCtl != null) {
					mProgressCtl.moveProgressStart();
				}
				
				final int x = (int) event.getX();
				
				mPosPercent = setProgressPos(x);
				if(mProgressCtl != null) {
					mProgressCtl.moveProgress(mPosPercent);
				}
				
				Message msg = mHandler.obtainMessage(PROGRESS_COMPLETED);				
				mHandler.removeMessages(PROGRESS_COMPLETED);
			 	mHandler.sendMessageDelayed(msg, 500);
			
			}
			else if(event.getAction() == MotionEvent.ACTION_UP) {
				mHandler.removeMessages(PROGRESS_COMPLETED);
				if(mProgressCtl != null) {
					mProgressCtl.moveProgressEnd(mPosPercent);
				}
			}
			
			return true;
		}
	};
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case PROGRESS_COMPLETED:						
					if(mProgressCtl != null) {						
						mProgressCtl.moveProgressEnd(mPosPercent);
					}
					break;
			}
		}
	};
		
	public MediaProgress(Context context) {
		super(context);
		init(context);
	}
	
	public MediaProgress(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public MediaProgress(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	public interface MediaProgressControl {
		void moveProgress(float percent);
		void moveProgressStart();
		void moveProgressEnd(float percent);
	}
	
	public static int getProgressWidth() {
		return mWidth;
	}
}
