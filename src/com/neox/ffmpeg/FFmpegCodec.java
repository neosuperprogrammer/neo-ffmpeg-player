package com.neox.ffmpeg;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.util.Log;

public class FFmpegCodec {
	private static final String LOG_TAG = FFmpegCodec.class.getSimpleName();

	public interface VideoDecodeEndListener {
		public void decodeEnded();
	}
	
	private Thread mPacketReaderThread;
	private Thread mAudioDecodeThread;
	private Thread mVideoDecodeThread;
	private VideoDecodeEndListener mListener;
	private boolean initialized;
	private VideoView mVideoView;
	private Context mContext;
	private Handler mHandler;
	private boolean isPaused;
	private long mCurrTime;
	
	private static AudioTrack track;	
	
	private static FFmpegCodec thiz = null;
	
	public static FFmpegCodec getInstance(Context context) {
		if(thiz  == null) {
			thiz = new FFmpegCodec(context);
		}
		return thiz;
	}

	private FFmpegCodec(Context context) {
		mContext = context;
		mHandler = new Handler(context.getMainLooper());
		initialized = false;
		isPaused = false;
		initAudio();
		initialized = true;
	};
	
	private void initAudio() {
		int bufSize = AudioTrack.getMinBufferSize(48000,
				AudioFormat.CHANNEL_CONFIGURATION_STEREO,
				AudioFormat.ENCODING_PCM_16BIT);

		track = new AudioTrack(AudioManager.STREAM_MUSIC, 48000,
				AudioFormat.CHANNEL_CONFIGURATION_STEREO,
				AudioFormat.ENCODING_PCM_16BIT, bufSize, AudioTrack.MODE_STREAM);
//		track.setStereoVolume(0.1f, 0.1f);
//		track.setStereoVolume(0.0f, 0.0f);
	}
	
	public boolean openVideo(String path) {
		if(jniOpenVideo(path)<0) {
			return false;
		}
		return true;
	}

	public void setVideoDecodeEndListener(VideoDecodeEndListener listender) {
		mListener = listender;
	}
	
	public void startDecodeThread() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
	    		Log.e("ffmpeg", "<<<<<<<<<<<<<<<< Decode Thread Start!!!!!>>>>>>>>>>>>>>>>>>>>");
				jniDecode();
	    		Log.e("ffmpeg", "<<<<<<<<<<<<<<<< Decode Thread Ended!!!!!>>>>>>>>>>>>>>>>>>>>");
	    		if(mListener!=null) {
	    			mListener.decodeEnded();
	    		}
			}
		}).start();
	}

	public void startVideoThread() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
	    		Log.e("ffmpeg", "<<<<<<<<<<<<<<<< Video Thread Start!!!!!>>>>>>>>>>>>>>>>>>>>");
				jniVideoThread();
	    		Log.e("ffmpeg", "<<<<<<<<<<<<<<<< Video Thread Ended!!!!!>>>>>>>>>>>>>>>>>>>>");
			}
		}).start();
	}
	
	public void startAudioThread() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
	    		Log.e("ffmpeg", "<<<<<<<<<<<<<<<< Audio Thread Start!!!!!>>>>>>>>>>>>>>>>>>>>");
				jniAudioThread();
	    		Log.e("ffmpeg", "<<<<<<<<<<<<<<<< Audio Thread Ended!!!!!>>>>>>>>>>>>>>>>>>>>");
			}
		}).start();
	}
	
	
	
	// called from jni
	public void playAudioFrame(final byte[] audioData, final int size) {
//		Log.e("ffmpeg", "playAudioFrame - [" + size + "]");
		// android.util.Log.v("ROHAUPT", "RAH Playing");
		if (track.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
			Log.e("ffmpeg", "track play state [" + track.getPlayState() + "]");
			track.play();
		}
		
		if(isPaused) {
			synchronized (this) {
				try {
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
					Log.e("ffmpeg", e.getMessage());
				}
			}
		}
		
//		Log.e("ffmpeg", "track write start!!!");
		track.write(audioData, 0, size);
//		Log.e("ffmpeg", "track write end!!!");
	}	
	
	public int getWidth() {
		return jniGetVideoWidth();
	}

	public int getHeight() {
		return jniGetVideoHeight();
	}
	
	
	public void closeVideo() {
		jniCloseVideo();
	}

	public void setVideoView(VideoView videoView) {
		mVideoView = videoView;
	}
	
//	public void setVideoDisplayTimer(int delay, int invalidate) {
//		setVideoDisplayTimer(delay, invalidate, -1);
//	}
	
	// called from jnij
	public void setVideoDisplayTimer(int delay, int invalidate, int currTime) {
//		if(invalidate == 0) { 
//			String str = String.format("setVideoDisplayTimer() called, delay[%d]ms, invalidate[%d]", delay, invalidate);
//			Log.e("ffmpeg", str);
//		}
//		LogUtil.e(LOG_TAG, "curr time : " + currTime);
//		String str = String.format("setVideoDisplayTimer() called, delay[%d]ms, invalidate[%d]", delay, invalidate);
//		Log.e("ffmpeg", str);
		if(currTime != -1) {
			mCurrTime = currTime;
		}
		if(!isPaused) {
			mVideoView.scheduleRefresh(delay, invalidate);
		}
	}
	
	public long getCurrTime() {
		return mCurrTime;
	}
	
	public void refreshVideo(Bitmap bitmap) {
		jniRefreshVideo(bitmap);
	}

	public boolean isPaused() {
		return isPaused;
	}
	
	public void toggle() {
		if(isPaused) {
			resume();
		}
		else {
			pause();
		}
	}

	private void pause() {
		isPaused = true;
		if (track.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
			track.pause();
		}
	}

	private void resume() {
		isPaused = false;
		if (track.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
			track.play();
		}
		synchronized (this) {
			this.notifyAll();
		}
		setVideoDisplayTimer(10, 1, -1);
	}

	public void seek(int incr) {
		jniStreamSeek(incr);
	}
	
	public void absSeek(int pos) {
		jniStreamAbsSeek(pos / 1000);
	}
	
	public void close() {
		jniClose();
	}

	public long getCurrentPosition() {
		return jniGetCurrentTime() * 1000;
	}

	public long getDuration() {
		return jniGetDuration() * 1000;
	}
	
	static {
        System.loadLibrary("basicplayer");
    }
	
	public native int jniOpenVideo(String filePath);
	public native int jniCloseVideo();
	
	public native int jniDecode();
	
	
	public native int jniGetVideoWidth();
	public native int jniGetVideoHeight();

	public native int jniGetCurrentTime();
	public native int jniGetDuration();
	
	public native void jniVideoThread();
	public native void jniAudioThread();


	public native void jniRefreshVideo(Bitmap bitmap);
	
	public native void jniStreamSeek(int incr);
	public native void jniStreamAbsSeek(int incr);
	public native void jniClose();



}
