package com.neox.ffmpeg;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.neox.ffmpeg.VideoView.VideoPanel;

public class VideoPlayerActivity extends Activity implements MediaControl, VideoPanel {
	private static final String LOG_TAG = VideoPlayerActivity.class.getSimpleName();

	private final int			CMD_SHOW_CAPTION 	= 0x000001;
	private final int			CMD_SHOW_CONTROL 	= CMD_SHOW_CAPTION + 0x000001;
	private final int			CMD_HIDE_CONTROL 	= CMD_SHOW_CONTROL + 0x000001;
	private final int			CMD_PLAY_START 		= CMD_HIDE_CONTROL + 0x000001;
	private final int			CMD_SHOW_DONGURI 	= CMD_PLAY_START + 0x000001;
	private final int			CMD_HIDE_DONGURI 	= CMD_SHOW_DONGURI + 0x000001;
	private final int			ORDER_REFRESH_VOLUME= CMD_HIDE_DONGURI + 0x000001;
	private final int			ORDER_RESUME_PLAY 	= ORDER_REFRESH_VOLUME + 0x000001;
	
	private FFmpegCodec ffmpeg;
	private float prevX;
	private float prevY;

	private TextView mTitleText;

	private TextView mCaptionView;

	private com.neox.ffmpeg.VideoPlayTime mPlayTime;

	private FrameLayout mVideoLayOut;

	private FrameLayout mTitleLayout;

	private MediaControlBar mCtlBar;

	private FrameLayout mCtlLayOut;
	
	static Handler _handler = new Handler();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.e("ffmpeg", "FFmpegBasicActivity onCreate()");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.videoplayer);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); 
        
        Intent intent = getIntent();
		String path = intent.getStringExtra("path");
		String title = intent.getStringExtra("title");
		LogUtil.e(LOG_TAG, "path : " + path + ", title : " + title);

        
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
        
//        VideoView videoView = new VideoView(getApplicationContext(), ffmpeg);
//        setContentView(videoView);
        setWidget();
        
        mTitleText.setText(title);

		ffmpeg.startDecodeThread();
		ffmpeg.startVideoThread();
		ffmpeg.startAudioThread();
		ffmpeg.setVideoDisplayTimer(100, 0, -1);
		
    }
    
	private void setWidget(){
		mTitleText = (TextView)findViewById(R.id.player_text_title);
		mCaptionView = (TextView)findViewById(R.id.player_text_caption);
		
		mPlayTime = new VideoPlayTime(this);
		mCtlBar = new MediaControlBar(this);
		mCtlBar.setType(MediaControlBar.CONTROLBAR_TYPE_VIDEO);
		
		mCtlBar.setMediaPlayer(this);		
		mCtlBar.setSoundEffectsEnabled(false);
		
		mVideoLayOut = ((FrameLayout) findViewById(R.id.player_videoplayer));
        VideoView videoView = new VideoView(getApplicationContext(), ffmpeg);
		mVideoLayOut.addView(videoView);
		videoView.setVideoPanel(this);
		
//		mVideoPlayer = new VideoPlayer(mVideoLayOut.getContext());
//		mVideoLayOut.addView(mVideoPlayer);
//		mVideoLayOut.setSoundEffectsEnabled(false);
		
//		mVideoPlayer.initVideoPlayer(mVideoLayOut.getContext());
//		mVideoPlayer.setOnListener(mPreparedListener);
//		mVideoPlayer.setOnListener(mCompletListener);	
//		mVideoPlayer.setOnListener(mErrorListener);
//		mVideoPlayer.setState(VideoPlayer.STATE_IDLE, VideoPlayer.STATE_IDLE);
		
//		long curTime = mVideoPlayer.getCurrentPosition();
//		mPlayTime.setTime(curTime, mVideoPlayer.getDuration());
//		mPlayTime.setPlayer(this);
//		mPlayTime.setProgressPos(curTime);
//		mPlayTime.setSoundEffectsEnabled(false);
//		
//		LogUtil.d(LOG_TAG, "duration = " + mVideoPlayer.getDuration());

		long curTime = ffmpeg.getCurrentPosition();
		mPlayTime.setTime(curTime, ffmpeg.getDuration());
		mPlayTime.setPlayer(this);
		mPlayTime.setProgressPos(curTime);
		mPlayTime.setSoundEffectsEnabled(false);
		
		LogUtil.d(LOG_TAG, "duration = " + ffmpeg.getDuration());
		
		mTitleLayout = ((FrameLayout)findViewById(R.id.player_title));
		((FrameLayout)findViewById(R.id.player_playtime)).addView(mPlayTime);
		mCtlLayOut = ((FrameLayout)findViewById(R.id.player_controlbar));
		mCtlLayOut.addView(mCtlBar);
		mCtlLayOut.setSoundEffectsEnabled(false);
		
//		mDongri = (MediaDongri)findViewById(R.id.player_dongri);
		
		mIsShowCtl = true;
		
		mVideoLayOut.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showCtl(!mIsShowCtl);
			}
		});
	}
	private boolean mIsShowCtl;
    
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case CMD_SHOW_CONTROL:
					showCtl(true);
					break;
				case CMD_HIDE_CONTROL:
					showCtl(false);
//					showDongri(false);
					break;
//				case CMD_PLAY_START:
//					setMedia(false);
//					if(mCtlBar != null) {
//						mCtlBar.updatePlayTimeAndPausePlay();
//					}
//					break;
//				case CMD_SHOW_DONGURI:
//					showDongri(true);
//					break;
//				case CMD_HIDE_DONGURI:
//					showDongri(false);
//					break;
//				case ORDER_REFRESH_VOLUME:
//					if(mCtlBar != null) {
//						mCtlBar.updatePlayTimeAndPausePlay();
//					}
//					break;				
//				case ORDER_RESUME_PLAY:
//					resumeProcess();
//					break;
			}
		}
	};	
	public void showCtl(boolean show) {
		mHandler.removeMessages(CMD_SHOW_CONTROL);
		mHandler.removeMessages(CMD_HIDE_CONTROL);
		
		if(show) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					mTitleLayout.setVisibility(View.VISIBLE);
					mCtlLayOut.setVisibility(View.VISIBLE);
					mCtlBar.setShowState(true);
					mPlayTime.setShowingState(true);
					
					
//					AlphaAnimation ani = new AlphaAnimation(0, 1);
//					ani.setDuration(1000);
//					ani.setFillAfter(true);
//					ani.setAnimationListener(new  Animation.AnimationListener() {
//						@Override
//						public void onAnimationStart(Animation animation) {							
//						}
//						
//						@Override
//						public void onAnimationRepeat(Animation animation) {
//						}
//						
//						@Override
//						public void onAnimationEnd(Animation animation) {
//							mTitleLayout.setVisibility(View.VISIBLE);
////							mCtlLayOut.setVisibility(View.VISIBLE);
////							mCtlBar.setShowState(true);
//							mPlayTime.setShowingState(true);
//						}
//					});
//					mTitleLayout.setAnimation(ani);
////					mCtlLayOut.startAnimation(ani);
//					
////					if(mVideoPlayer.isPlaying()) {
////						sendCommandDelayed(CMD_HIDE_CONTROL, 5000); 	// 5초후에 컨트롤 패드를 내린다.
////					}
				}
			});
		} 
		else {
			if(mIsShowCtl) {
				mHandler.post(new Runnable() {
					
					@Override
					public void run() {
						mTitleLayout.setVisibility(View.GONE);
						mCtlLayOut.setVisibility(View.GONE);
						mCtlBar.setShowState(false);
						mPlayTime.setShowingState(false);
						
//						AlphaAnimation ani = new AlphaAnimation(1, 0);
//						ani.setDuration(1000);
//						ani.setFillAfter(true);
//						ani.setAnimationListener(new  Animation.AnimationListener() {
//							@Override
//							public void onAnimationStart(Animation animation) {
//							}
//							
//							@Override
//							public void onAnimationRepeat(Animation animation) {
//							}
//							
//							@Override
//							public void onAnimationEnd(Animation animation) {
//								mTitleLayout.setVisibility(View.GONE);
////								mCtlLayOut.setVisibility(View.GONE);
////								mCtlBar.setShowState(false);
//								mPlayTime.setShowingState(false);
//							}
//						});
//						mTitleLayout.setAnimation(ani);
////						mCtlLayOut.startAnimation(ani);					
					}
				}); 
			}
		}
		mIsShowCtl = show;
	}
	
//	@Override
//	public boolean onTouchEvent(MotionEvent event) {
//		float x = event.getX();
//		float y = event.getY();
//		if(event.getAction() == MotionEvent.ACTION_DOWN) {
//			prevX = x;
//			prevY = y;
//		}
//		else if(event.getAction() == MotionEvent.ACTION_UP) {
//			int delta = (int) (x - prevX); 
//			if(Math.abs(delta) < 20) {
//				ffmpeg.toggle();
//			}
//			else {
//				if(!ffmpeg.isPaused()) {
//					if(delta > 0) {
//						ffmpeg.seek(10);
//					}
//					else {
//						ffmpeg.seek(-10);
//					}
//				}
//			}
//		}
//		return super.onTouchEvent(event);
//	}

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

	@Override
	public boolean start() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void changeSize() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMedia(boolean isAfterPauseAndStart) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		
		ffmpeg.toggle();
		
//		mVideoPlayer.pause();
//		videoPlayTimeHistory.put(mMetaDataList.get(mCurIdx).mUrl.toString(), mStartSyncTime);
//			
//		mStartSyncTime = mVideoPlayer.getCurrentPosition();
		mCtlBar.updatePlayTimeAndPausePlay();
//		mHandler.removeMessages(CMD_HIDE_CONTROL);		
		
	}

	@Override
	public long getDuration() {
		return ffmpeg.getDuration();
	}

	@Override
	public long getCurrentPosition() {
		return ffmpeg.getCurrentPosition();
	}

	@Override
	public void seekTo(int pos) {
		LogUtil.e(LOG_TAG, "seek to : " + pos);
		ffmpeg.absSeek(pos);
		
	}

	@Override
	public boolean isPlaying() {
		return !ffmpeg.isPaused();
	}

	@Override
	public int getBufferPercentage() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean canPause() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canSeekBackward() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canSeekForward() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canPlay() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean prev() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean next() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void seeking() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void seekingEnd() {
		// TODO Auto-generated method stub
		
	}

	
	// Video Panel Interface
	@Override
	public void setPos(int curr, int total) {
		mPlayTime.setTime(curr, total);
	}

	@Override
	public void setPlay() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPause() {
		// TODO Auto-generated method stub
		
	}
		
}
