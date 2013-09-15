package com.neox.ffmpeg;

public interface MediaControl {
	boolean start();
	void changeSize();
	void setMedia(final boolean isAfterPauseAndStart);
	void pause();
	long getDuration();
	long getCurrentPosition();
	void seekTo(int pos);
	boolean isPlaying();
	int getBufferPercentage();
	boolean canPause();
	boolean canSeekBackward();
	boolean canSeekForward();
	boolean canPlay();
	boolean prev();
	boolean next();
	void seeking();
	void seekingEnd();
}
