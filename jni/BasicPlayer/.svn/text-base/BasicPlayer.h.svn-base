/*
 * Main functions of BasicPlayer
 * 2011-2011 Jaebong Lee (novaever@gmail.com)
 *
 * BasicPlayer is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

#ifndef __BASIC_PLAYER_H__
#define __BASIC_PLAYER_H__


int openVideo(const char *filePath);

void decode();

int getWidth();
int getHeight();

int getDuration();
int getCurrentTime();

int videoThread();

int getPicture(jobject jbitmap);

void video_refresh_timer(jobject jbitmap);

void streamSeek(int pos);
void streamAbsSeek(int pos);

void closeVideo();
void exitFFmpeg();

#endif
