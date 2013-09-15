package com.neox.ffmpeg;

public class Util {

	public static String timeToString(long time) {
		long tmpTime = time / 1000;
		long sec = tmpTime % 60;
		long min = (tmpTime / 60) % 60;
		long hour = (tmpTime / 60) / 60;
		
		String hourStr = "";
		String minStr = "";
		String secStr = "";
		
		String timeStr = "";
		
		if(min < 10)
			minStr = "0" + Integer.toString((int)min);
		else 
			minStr = Integer.toString((int)min);
		
		if(sec < 10)
			secStr = "0" + Integer.toString((int)sec);
		else 
			secStr = Integer.toString((int)sec);
		
		if(hour > 0) {
			hourStr = Integer.toString((int)hour);
			timeStr = hourStr + ":";
		}
		
		timeStr += minStr;
		timeStr += ":";
		timeStr += secStr;
		
		return timeStr;
	}	
}
