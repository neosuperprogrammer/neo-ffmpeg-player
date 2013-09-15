/*
 * Main functions of BasicPlayer
 * 2011-2011 Jaebong Lee (novaever@gmail.com)
 *
 * BasicPlayer is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <jni.h>
#include "avcodec.h"
#include "avformat.h"
#include "swscale.h"
#include "BasicPlayer.h"
#include "BasicPlayerJni.h"
#include "mySDL.h"


//#define MAX_AUDIOQ_SIZE (5 * 16 * 1024)
#define MAX_AUDIOQ_SIZE (5 * 256 * 1024)
#define MAX_VIDEOQ_SIZE (5 * 256 * 1024)

//#define VIDEO_PICTURE_QUEUE_SIZE 1
#define VIDEO_PICTURE_QUEUE_SIZE 2
#define DEFAULT_AV_SYNC_TYPE AV_SYNC_AUDIO_MASTER

#define AV_SYNC_THRESHOLD 0.01
#define AV_NOSYNC_THRESHOLD 10.0
#define AUDIO_DIFF_AVG_NB 20
#define SDL_AUDIO_BUFFER_SIZE 1024

#define SAMPLE_CORRECTION_PERCENT_MAX 10


typedef struct PacketQueue {
	AVPacketList 	*first_pkt, *last_pkt;
	int 			nb_packets;
	int 			size;
	SDL_mutex 	*mutex;
	SDL_cond 	*cond;
} PacketQueue;

typedef struct VideoPicture {
	AVFrame 	*bmp;
	uint8_t 		*buffer;		
	int			pictureSize;
	int 			width, height; /* source height & width */
	int 			allocated;
 	double 		pts;
} VideoPicture;

typedef struct VideoState {
	AVFormatContext 	*pFormatCtx;
	int             		videoStream, audioStream;

	int             		av_sync_type;
	double          		external_clock; /* external clock base */
	int64_t         		external_clock_time;

	int             		seek_req;
	int             		seek_flags;
	int64_t         		seek_pos;

	double          		audio_clock;

	
	AVStream        	*audio_st;
	PacketQueue     	audioq;
	DECLARE_ALIGNED(16, uint8_t, audio_buf[(AVCODEC_MAX_AUDIO_FRAME_SIZE * 3) / 2]);
	unsigned int    	audio_buf_size;
	unsigned int    	audio_buf_index;
	AVPacket        	audio_pkt;
	uint8_t         		*audio_pkt_data;
	int             		audio_pkt_size;

	int             		audio_hw_buf_size;  
	double          		audio_diff_cum; /* used for AV difference average computation */
	double          		audio_diff_avg_coef;
	double          		audio_diff_threshold;
	int             		audio_diff_avg_count;
	double          		frame_timer;
	double          		frame_last_pts;
	double          		frame_last_delay;
	double          		video_clock; ///<pts of last decoded frame / predicted pts of next decoded frame
	double          		video_current_pts; ///<current displayed pts (different from video_clock if frame fifos are used)
	int64_t         		video_current_pts_time;  ///<time (av_gettime) at which we updated video_current_pts - used to have running video pts
	
	AVStream        	*video_st;
	PacketQueue     	videoq;
	
	VideoPicture    	pictq[VIDEO_PICTURE_QUEUE_SIZE];
	int             		pictq_size, pictq_rindex, pictq_windex;
	SDL_mutex       	*pictq_mutex;
	SDL_cond        	*pictq_cond;

	char            		filename[1024];
	int             		quit;
} VideoState;

enum {
	AV_SYNC_AUDIO_MASTER,
	AV_SYNC_VIDEO_MASTER,
	AV_SYNC_EXTERNAL_MASTER,
};



extern JNIEnv *g_Env;
extern jobject g_thiz;
extern JNIEnv *g_AudioEnv;
extern jobject g_Audiothiz;
extern JNIEnv *g_VideoEnv;
extern jobject g_Videothiz;

/* Since we only have one decoding thread, the Big Struct
   can be global in case we need it. */
VideoState *global_video_state;
AVPacket flush_pkt;

void packet_queue_init(PacketQueue *q) {
	memset(q, 0, sizeof(PacketQueue));
	q->mutex = SDL_CreateMutex();
	q->cond = SDL_CreateCond();
}

int packet_queue_put(PacketQueue *q, AVPacket *pkt) 
{
	AVPacketList *pkt1;
	if(pkt != &flush_pkt && av_dup_packet(pkt) < 0) {
		return -1;
	}
	pkt1 = av_malloc(sizeof(AVPacketList));
	if (!pkt1)
		return -1;
	pkt1->pkt = *pkt;
	pkt1->next = NULL;
  
  
	SDL_LockMutex(q->mutex);
  
	if (!q->last_pkt)
		q->first_pkt = pkt1;
	else
		q->last_pkt->next = pkt1;
	q->last_pkt = pkt1;
	q->nb_packets++;
	q->size += pkt1->pkt.size;
	SDL_CondSignal(q->cond);
  
	SDL_UnlockMutex(q->mutex);
	return 0;
}

static int packet_queue_get(PacketQueue *q, AVPacket *pkt, int block)
{
	AVPacketList *pkt1;
	int ret;
  
	SDL_LockMutex(q->mutex);
  
	for(;;) {
    
		if(global_video_state->quit) {
			ret = -1;
			LOGE("packet_queue_get, quit flag is set, return -1!!!");	
			break;
		}

		pkt1 = q->first_pkt;
		if (pkt1) {
			q->first_pkt = pkt1->next;
			if (!q->first_pkt)
				q->last_pkt = NULL;
			q->nb_packets--;
			q->size -= pkt1->pkt.size;
			*pkt = pkt1->pkt;
			av_free(pkt1);
			ret = 1;
			break;
		} 
		else if (!block) {
			ret = 0;
			break;
		} 
		else {
			LOGE("packet_queue_get, queue is empty, wait!!!");	
			SDL_CondWait(q->cond, q->mutex);
			LOGE("packet_queue_get, wait ended!!!");	
		}
	}
	SDL_UnlockMutex(q->mutex);
	return ret;
}

static void packet_queue_flush(PacketQueue *q) 
{
	AVPacketList *pkt, *pkt1;

	SDL_LockMutex(q->mutex);
	for(pkt = q->first_pkt; pkt != NULL; pkt = pkt1) {
		pkt1 = pkt->next;
		av_free_packet(&pkt->pkt);
		av_freep(&pkt);
	}
	q->last_pkt = NULL;
	q->first_pkt = NULL;
	q->nb_packets = 0;
	q->size = 0;
	SDL_UnlockMutex(q->mutex);
}


int video_display(VideoPicture *vp, jobject jbitmap) 
{
	void *pixels;

	if(!vp->bmp) {
		LOGE("video_display : vp->bmp is null!!!");
		return -1;
	}
	
	if ( AndroidBitmap_lockPixels(g_Env, jbitmap, &pixels) < 0) {
		LOGE("jni-AndroidBitmap_lockPixels failed!!!");
		return -1;
	}

	memcpy(pixels, vp->bmp->data[0], vp->pictureSize);
//	memcpy(pixels, vp->buffer, vp->pictureSize);
//	memcpy(pixels, gFrameRGB->data[0], gPictureSize);

	AndroidBitmap_unlockPixels(g_Env, jbitmap);

	return 0;
}

double get_audio_clock(VideoState *is) {
	double pts;
	int hw_buf_size, bytes_per_sec, n;

	pts = is->audio_clock; /* maintained in the audio thread */
	hw_buf_size = is->audio_buf_size - is->audio_buf_index;
	bytes_per_sec = 0;
	n = is->audio_st->codec->channels * 2;
	if(is->audio_st) {
		bytes_per_sec = is->audio_st->codec->sample_rate * n;
	}
	if(bytes_per_sec) {
		pts -= (double)hw_buf_size / bytes_per_sec;
	}
	return pts;
}

double get_video_clock(VideoState *is) {
	double delta;

	delta = (av_gettime() - is->video_current_pts_time) / 1000000.0;
	return is->video_current_pts + delta;
}

double get_external_clock(VideoState *is) {
	return av_gettime() / 1000000.0;
}

double get_master_clock(VideoState *is) {
	if(is->av_sync_type == AV_SYNC_VIDEO_MASTER) {
		return get_video_clock(is);
	} else if(is->av_sync_type == AV_SYNC_AUDIO_MASTER) {
		return get_audio_clock(is);
	} else {
		return get_external_clock(is);
	}
}

/* schedule a video refresh in 'delay' ms */
static void schedule_refresh(VideoState *is, int delay, int invalidate) 
{
//  SDL_AddTimer(delay, sdl_refresh_timer_cb, is);
	JNIEnv *env = g_VideoEnv;
	jobject obj = g_Videothiz;
	jclass cls = 0;
	jmethodID mid = 0;

	if ((*env)->PushLocalFrame(env, 16) < 0)
		return;

	cls = (*env)->GetObjectClass(env, obj); 
	if (cls)  {
		mid = (*env)->GetMethodID(env, cls, "setVideoDisplayTimer", "(III)V");
		if (mid) {
			int64_t curr_time = (int64_t)(get_master_clock(is) * AV_TIME_BASE);
			curr_time= av_rescale_q(curr_time, AV_TIME_BASE_Q, is->video_st->time_base);
			int curr_time_int = (int)(curr_time / AV_TIME_BASE);
//			LOGE("current time [%d]", curr_time);
//			LOGE("current time int [%d]", curr_time_int);
			(*env)->CallObjectMethod(env, obj, mid, delay, invalidate, curr_time_int);

			// 에뮬레이터에서는 아래 문장 넣어야 정상동작함. 에뮬 버그로 보임.
//			(*g_Env)->DeleteGlobalRef(g_env, aVCard); // JNI 버그인지 모르겠으나 Global Ref Table에도 추가된다. 그래서 명시적으로 Delete 해준다.
		}
		else {
			LOGE("GetMethodID() failed.");
		}	
	}
	else {
		LOGE("GetObjectClass() failed.");
	}	

	(*env)->PopLocalFrame(env, NULL);
}

/*
int getPicture(jobject jbitmap) 
{

	VideoState *is = global_video_state;
	if(is == NULL) {
		LOGE("getPicture : global_video_state is null, return!!");
		return -1;
	}
	VideoPicture *vp;
	double actual_delay, delay, sync_threshold, ref_clock, diff;
	if(is->video_st) {
		if(is->pictq_size == 0) {
			LOGE("getPicture : queue size is 0, refresh again!!!");
//			schedule_refresh(is, 100, 0);
			return -1;
		} 
		else {
			vp = &is->pictq[is->pictq_rindex];

			is->video_current_pts = vp->pts;
			is->video_current_pts_time = av_gettime();

			delay = vp->pts - is->frame_last_pts; // the pts from last time 
			if(delay <= 0 || delay >= 1.0) {
				// if incorrect delay, use previous one 
				delay = is->frame_last_delay;
			}
			// save for next time 
			is->frame_last_delay = delay;
			is->frame_last_pts = vp->pts;

			// update delay to sync to audio if not master source 
			if(is->av_sync_type != AV_SYNC_VIDEO_MASTER) {
				ref_clock = get_master_clock(is);
				diff = vp->pts - ref_clock;

				// Skip or repeat the frame. Take delay into account
				// FFPlay still doesn't "know if this is the best guess." 
				sync_threshold = (delay > AV_SYNC_THRESHOLD) ? delay : AV_SYNC_THRESHOLD;
				if(fabs(diff) < AV_NOSYNC_THRESHOLD) {
					if(diff <= -sync_threshold) {
						delay = 0;
					} 
					else if(diff >= sync_threshold) {
						delay = 2 * delay;
					}
				}
			}

			is->frame_timer += delay;
			// computer the REAL delay 
			actual_delay = is->frame_timer - (av_gettime() / 1000000.0);
			if(actual_delay < 0.010) {
				// Really it should skip the picture instead 
				actual_delay = 0.010;
			}
//			schedule_refresh(is, (int)(actual_delay * 1000 + 0.5), 1);

			// show the picture! 
			video_display(vp, jbitmap);

			// update queue for next picture! 
			if(++is->pictq_rindex == VIDEO_PICTURE_QUEUE_SIZE) {
				is->pictq_rindex = 0;
			}
			SDL_LockMutex(is->pictq_mutex);
			is->pictq_size--;
			SDL_CondSignal(is->pictq_cond);
			SDL_UnlockMutex(is->pictq_mutex);
			return 0;

			
		}
	} 
	else {
		LOGE("getPicture : is->video_st is null, refresh again!!!");
//		schedule_refresh(is, 100, 0);
		return -1;
	}
}
*/


void video_refresh_timer(jobject jbitmap) 
{
	VideoState *is = global_video_state;
	if(is == NULL) {
		LOGE("video_refresh_timer : global_video_state is null, return!!");
		return;
	}
	VideoPicture *vp;
	double actual_delay, delay, sync_threshold, ref_clock, diff;
	if(is->video_st) {
//RETRY:		
		if(is->quit) {
			LOGE("video_refresh_timer : quit is set, return!!!");
			return;
		}
		if(is->pictq_size == 0) {
			LOGI("video_refresh_timer : queue size is 0, refresh again!!!");
			schedule_refresh(is, 10, 0);
			return;
		} 
		else {
//			LOGE("picture queue rindex[%d]", is->pictq_rindex);
			vp = &is->pictq[is->pictq_rindex];

			is->video_current_pts = vp->pts;
			is->video_current_pts_time = av_gettime();

			delay = vp->pts - is->frame_last_pts; /* the pts from last time */
			if(delay <= 0 || delay >= 1.0) {
				/* if incorrect delay, use previous one */
				delay = is->frame_last_delay;
			}
			/* save for next time */
			is->frame_last_delay = delay;
			is->frame_last_pts = vp->pts;

			/* update delay to sync to audio if not master source */
			if(is->av_sync_type != AV_SYNC_VIDEO_MASTER) {
				ref_clock = get_master_clock(is);
				diff = vp->pts - ref_clock;

				/* Skip or repeat the frame. Take delay into account
				FFPlay still doesn't "know if this is the best guess." */
				sync_threshold = (delay > AV_SYNC_THRESHOLD) ? delay : AV_SYNC_THRESHOLD;
				if(fabs(diff) < AV_NOSYNC_THRESHOLD) {
					if(diff <= -sync_threshold) {
						delay = 0;
					} 
					else if(diff >= sync_threshold) {
						delay = 2 * delay;
					}
				}
			}

			is->frame_timer += delay;
			/* computer the REAL delay */
			actual_delay = is->frame_timer - (av_gettime() / 1000000.0);
			if(actual_delay < 0.010) {
				/* Really it should skip the picture instead */
				actual_delay = 0.010;

/*
				if(is->pictq_size > 1) {
					LOGE(">>>>>>>>>>>>>>>>>>> skip this frame!!! , queue size[%d]>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>", is->pictq_size);
					
					if(++is->pictq_rindex == VIDEO_PICTURE_QUEUE_SIZE) {
						is->pictq_rindex = 0;
					}
					SDL_LockMutex(is->pictq_mutex);
					is->pictq_size--;
					SDL_CondSignal(is->pictq_cond);
					SDL_UnlockMutex(is->pictq_mutex);
					goto RETRY;
				}
*/				
			}
			schedule_refresh(is, (int)(actual_delay * 1000 + 0.5), 1);

			/* show the picture! */
			video_display(vp, jbitmap);

			/* update queue for next picture! */
			if(++is->pictq_rindex == VIDEO_PICTURE_QUEUE_SIZE) {
				is->pictq_rindex = 0;
			}
			SDL_LockMutex(is->pictq_mutex);
			is->pictq_size--;
			SDL_CondSignal(is->pictq_cond);
			SDL_UnlockMutex(is->pictq_mutex);
			return;



			
		}
	} 
	else {
		LOGE("video_refresh_timer : is->video_st is null, refresh again!!!");
		schedule_refresh(is, 100, 0);
		return;
	}
}

void alloc_picture(VideoState *is, int dstPixFmt) 
{
	LOGE("alloc_picture entered, queue write index [%d]!!!", is->pictq_windex);
	
	VideoPicture *vp;

	vp = &is->pictq[is->pictq_windex];
	if(vp->bmp) {
		LOGI("alloc_picture, remove previous vp->bmp!!!");
		// we already have one make another, bigger/smaller
		if(vp->buffer != NULL) {
			LOGI("alloc_picture, free previous vp->buffer!!!");
			av_free(vp->buffer);				
		}
		av_freep(vp->bmp);
	}
	// Allocate a place to put our YUV image on that screen
	vp->bmp = avcodec_alloc_frame();
	if (vp->bmp == NULL) {
		LOGE("alloc_picture : avcodec_alloc_frame failed, return!!!");	
		exit(1);
	}

	
	vp->pictureSize = avpicture_get_size(dstPixFmt, is->video_st->codec->width, is->video_st->codec->height);

//	LOGI("width[%d], height[%d], pic size[%d]", is->video_st->codec->width, is->video_st->codec->height, vp->pictureSize);
	
	vp->buffer = (uint8_t *)av_malloc(vp->pictureSize * sizeof(uint8_t));
	avpicture_fill((AVPicture*)vp->bmp, vp->buffer, dstPixFmt, is->video_st->codec->width, is->video_st->codec->height);

	vp->width = is->video_st->codec->width;
	vp->height = is->video_st->codec->height;

	vp->allocated = 1;

//	SDL_LockMutex(is->pictq_mutex);
//	vp->allocated = 1;
//	SDL_CondSignal(is->pictq_cond);
//	SDL_UnlockMutex(is->pictq_mutex);

}

int queue_picture(VideoState *is, AVFrame *pFrame, double pts) 
{
	VideoPicture *vp;
	int dst_pix_fmt = PIX_FMT_RGB565LE;
//	int dst_pix_fmt = PIX_FMT_RGBA;
	
	AVPicture pict;

	static struct SwsContext *img_convert_ctx;


//	LOGE("queue_picture : is->pictq_size [%d]", is->pictq_size);
	/* wait until we have space for a new pic */
	SDL_LockMutex(is->pictq_mutex);
	while(is->pictq_size >= VIDEO_PICTURE_QUEUE_SIZE && !is->quit) {
//		LOGE("queue_picture : queue size is big, wait!!!");
		SDL_CondWait(is->pictq_cond, is->pictq_mutex);
//		LOGE("queue_picture : queue size is big, wait end!!!");
	}
	SDL_UnlockMutex(is->pictq_mutex);

	if(is->quit) {
		LOGE("queue_picture : quit is set, return!!!");
		return -1;
	}

	  // windex is set to 0 initially
	  vp = &is->pictq[is->pictq_windex];

	/* allocate or resize the buffer! */
	
	if(!vp->bmp || vp->width != is->video_st->codec->width || vp->height != is->video_st->codec->height) {
		vp->allocated = 0;
		alloc_picture(is, dst_pix_fmt);
  	}
	  /* We have a place to put our picture on the queue */
	  /* If we are skipping a frame, do we set this to null 
	     but still return vp->allocated = 1? */


	if(vp->bmp) {
		if(img_convert_ctx == NULL) {
			int w = is->video_st->codec->width;
			int h = is->video_st->codec->height;
			img_convert_ctx = sws_getContext(w, h, is->video_st->codec->pix_fmt, w, h, dst_pix_fmt, SWS_BICUBIC, NULL, NULL, NULL);
			if(img_convert_ctx == NULL) {
				LOGE("queue_picture : Cannot initialize the conversion context!!");
				exit(1);
			}
		}

		
//		LOGI("queue_picture : src frame line size[%d], dst frame line size [%d]!!", pFrame->linesize[0], vp->bmp->linesize[0]);
//		LOGE("try to convert start");
		sws_scale(img_convert_ctx, pFrame->data, pFrame->linesize, 0, is->video_st->codec->height, vp->bmp->data, vp->bmp->linesize);
//		LOGE("try to convert end");

		vp->pts = pts;

		/* now we inform our display thread that we have a pic ready */
		if(++is->pictq_windex == VIDEO_PICTURE_QUEUE_SIZE) {
			is->pictq_windex = 0;
		}
		SDL_LockMutex(is->pictq_mutex);
		is->pictq_size++;
		SDL_UnlockMutex(is->pictq_mutex);
//		LOGI("queue_picture : end queue size[%d]!!!", is->pictq_size);
	}

	return 0;
}


double synchronize_video(VideoState *is, AVFrame *src_frame, double pts) 
{
	double frame_delay;

	if(pts != 0) {
		/* if we have pts, set video clock to it */
		is->video_clock = pts;
	} 
	else {
		/* if we aren't given a pts, set it to the clock */
		pts = is->video_clock;
	}
	/* update the video clock */
	frame_delay = av_q2d(is->video_st->codec->time_base);
	/* if we are repeating a frame, adjust clock accordingly */
	frame_delay += src_frame->repeat_pict * (frame_delay * 0.5);
	is->video_clock += frame_delay;
	return pts;
}

uint64_t global_video_pkt_pts = AV_NOPTS_VALUE;

/* These are called whenever we allocate a frame
 * buffer. We use this to store the global_pts in
 * a frame at the time it is allocated.
 */
int our_get_buffer(struct AVCodecContext *c, AVFrame *pic) 
{
//	LOGI("our_get_buffer is called!!!");
	int ret = avcodec_default_get_buffer(c, pic);
	uint64_t *pts = av_malloc(sizeof(uint64_t));
	*pts = global_video_pkt_pts;
	pic->opaque = pts;
	return ret;
}

void our_release_buffer(struct AVCodecContext *c, AVFrame *pic) 
{
//	LOGI("our_release_buffer is called!!!");
	if(pic) {
		av_freep(&pic->opaque);
	}
	avcodec_default_release_buffer(c, pic);
}

int videoThread() 
{
	VideoState *is = global_video_state;
	AVPacket pkt1, *packet = &pkt1;
	int len1, frameFinished;
	AVFrame *pFrame;
	double pts;

	pFrame = avcodec_alloc_frame();

	for(;;) {
		if(packet_queue_get(&is->videoq, packet, 1) < 0) {
			LOGE("videoThread : packet_queue_get failed, return!!!");
			// means we quit getting packets
			break;
		}
		if(packet->data == flush_pkt.data) {
			avcodec_flush_buffers(is->video_st->codec);
			LOGE("videoThread : flush!!!");
			continue;
		}
		pts = 0;

		// Save global pts to be stored in pFrame
		global_video_pkt_pts = packet->pts;
		// Decode video frame
//		LOGE("try to decode start");
		len1 = avcodec_decode_video2(is->video_st->codec, pFrame, &frameFinished, packet);
//		LOGE("try to decode end");
		if(packet->dts == AV_NOPTS_VALUE && pFrame->opaque && *(uint64_t*)pFrame->opaque != AV_NOPTS_VALUE) {
			pts = *(uint64_t *)pFrame->opaque;
		} else if(packet->dts != AV_NOPTS_VALUE) {
			pts = packet->dts;
		} else {
			pts = 0;
		}
		pts *= av_q2d(is->video_st->time_base);


		// Did we get a video frame?
		if(frameFinished) {
			pts = synchronize_video(is, pFrame, pts);
			if(queue_picture(is, pFrame, pts) < 0) {
				LOGE("videoThread : queue_picture failed, return!!!");
				break;
			}

		}
		av_free_packet(packet);
	}
	av_free(pFrame);
	return 0;
}   

/*
static void pushAudioData(uint8_t *audioData, int audioSize)
{
	jobject obj = g_thiz;
	jclass cls = 0;
	jmethodID mid = 0;


//	if ((*g_Env)->PushLocalFrame(g_Env, 10) < 0)
//		return;

	cls = (*g_Env)->GetObjectClass(g_Env, obj); 
	if (cls)
	{
		mid = (*g_Env)->GetMethodID(g_Env, cls, "playAudioFrame", "([BI)V");
		if (mid)
		{
			jbyteArray jAudioBuffer;
			jbyte *jAudioArray;


			jAudioBuffer = (*g_Env)->NewByteArray(g_Env, audioSize);

			jAudioArray = (*g_Env)->GetByteArrayElements(g_Env, jAudioBuffer, NULL);
			memcpy(jAudioArray, audioData, audioSize); 

			(*g_Env)->ReleaseByteArrayElements(g_Env, jAudioBuffer, jAudioArray, 0);
			
			(*g_Env)->CallObjectMethod(g_Env, obj, mid, jAudioBuffer, audioSize);

			(*g_Env)->DeleteLocalRef(g_Env, jAudioBuffer);
			

			// 에뮬레이터에서는 아래 문장 넣어야 정상동작함. 에뮬 버그로 보임.
//			(*g_Env)->DeleteGlobalRef(g_Env, jAudioArray); // JNI 버그인지 모르겠으나 Global Ref Table에도 추가된다. 그래서 명시적으로 Delete 해준다.


			
//			(*g_Env)->DeleteLocalRef(g_Env, mid);

		}
		else {
			LOGE("GetMethodID() failed.");
		}	
		(*g_Env)->DeleteLocalRef(g_Env, cls);
	}
	else {
		LOGE("GetObjectClass() failed.");
	}	

//	(*g_Env)->PopLocalFrame(g_Env, NULL);
}
*/


void pushAudioData(uint8_t *audioData, int audioSize)
{
	jobject obj = g_Audiothiz;
	jclass cls = 0;
	jmethodID mid = 0;

	JNIEnv *env = g_AudioEnv;


	if ((*env)->PushLocalFrame(env, 16) < 0)
		return;

	cls = (*env)->GetObjectClass(env, obj); 
	
	if (cls) {
		mid = (*env)->GetMethodID(env, cls, "playAudioFrame", "([BI)V");
		if (mid) {
			jbyteArray jAudioBuffer;
			jbyte *jAudioArray;

			jAudioBuffer = (*env)->NewByteArray(env, audioSize);

			jAudioArray = (*env)->GetByteArrayElements(env, jAudioBuffer, NULL);
			memcpy(jAudioArray, audioData, audioSize); 

			(*env)->ReleaseByteArrayElements(env, jAudioBuffer, jAudioArray, 0);

			(*env)->CallObjectMethod(env, obj, mid, jAudioBuffer, audioSize);

//			(*g_Env)->DeleteLocalRef(g_Env, audioSize);

		}
		else {
			LOGE("GetMethodID() failed.");
		}	
//		(*g_Env)->DeleteLocalRef(g_Env, cls);
	}
	else {
		LOGE("GetObjectClass() failed.");
	}	

	(*env)->PopLocalFrame(env, NULL);
}

/* Add or subtract samples to get a better sync, return new
   audio buffer size */
int synchronize_audio(VideoState *is, short *samples, int samples_size, double pts) 
{
	int n;
	double ref_clock;

	n = 2 * is->audio_st->codec->channels;

	if(is->av_sync_type != AV_SYNC_AUDIO_MASTER) {
		double diff, avg_diff;
		int wanted_size, min_size, max_size, nb_samples;

		ref_clock = get_master_clock(is);
		diff = get_audio_clock(is) - ref_clock;
		if(diff < AV_NOSYNC_THRESHOLD) {
			// accumulate the diffs
			is->audio_diff_cum = diff + is->audio_diff_avg_coef
			* is->audio_diff_cum;
			if(is->audio_diff_avg_count < AUDIO_DIFF_AVG_NB) {
				is->audio_diff_avg_count++;
			} 
			else {
				avg_diff = is->audio_diff_cum * (1.0 - is->audio_diff_avg_coef);
				if(fabs(avg_diff) >= is->audio_diff_threshold) {
					wanted_size = samples_size + ((int)(diff * is->audio_st->codec->sample_rate) * n);
					min_size = samples_size * ((100 - SAMPLE_CORRECTION_PERCENT_MAX) / 100);
					max_size = samples_size * ((100 + SAMPLE_CORRECTION_PERCENT_MAX) / 100);
					if(wanted_size < min_size) {
						wanted_size = min_size;
					} 
					else if (wanted_size > max_size) {
						wanted_size = max_size;
					}
					if(wanted_size < samples_size) {
						/* remove samples */
						samples_size = wanted_size;
					} 
					else if(wanted_size > samples_size) {
						uint8_t *samples_end, *q;
						int nb;
						/* add samples by copying final sample*/
						nb = (samples_size - wanted_size);
						samples_end = (uint8_t *)samples + samples_size - n;
						q = samples_end + n;
						while(nb > 0) {
							memcpy(q, samples_end, n);
							q += n;
							nb -= n;
						}
						samples_size = wanted_size;
					}
				}
			}
		} 
		else {
			/* difference is TOO big; reset diff stuff */
			is->audio_diff_avg_count = 0;
			is->audio_diff_cum = 0;
		}
	}
	return samples_size;
}

int audio_decode_frame(VideoState *is, uint8_t *audio_buf, int buf_size, double *pts_ptr) {
	int len1, data_size, n;
//	AVPacket *pkt = &is->audio_pkt;
	double pts;

//	is->audio_pkt_data = pkt->data;
//	is->audio_pkt_size = pkt->size;
	AVPacket packet;
	AVPacket *pkt = &packet;
	memset(pkt, 0, sizeof(AVPacket));
//	LOGE("audio_decode_frame : enter, packet size[%d], data[%d]!!!", pkt->size, pkt->data);	

	for(;;) {
//		while(is->audio_pkt_size > 0) {
		while(pkt->size > 0) {	
			data_size = buf_size;
//			LOGE("call avcodec_decode_audio3, packet size[%d]!!!", pkt->size);	
			len1 = avcodec_decode_audio3(is->audio_st->codec, (int16_t *)audio_buf, &data_size, pkt);
//			LOGE("call avcodec_decode_audio3, len1[%d], data_size[%d]!!!", len1, data_size);	
			
			if(len1 < 0) {
				/* if error, skip frame */
//				is->audio_pkt_size = 0;
				LOGE("avcodec_decode_audio3 error, skip frame!!!");	
				pkt->size = 0;
				break;
			}
//			is->audio_pkt_data += len1;
//			is->audio_pkt_size -= len1;
			pkt->data += len1;
			pkt->size -= len1;
			
			if(data_size <= 0) {
				/* No data yet, get more frames */
				continue;
			}
			pts = is->audio_clock;
			*pts_ptr = pts;
			n = 2 * is->audio_st->codec->channels;
			is->audio_clock += (double)data_size /(double)(n * is->audio_st->codec->sample_rate);

			/* We have data, return it and come back for more later */
//			LOGE("audio_decode_frame return!!!!!!");	
			return data_size;
		}
		if(pkt->data) {
//			LOGE("audio_decode_frame : try to av_free_packet!!!");
			av_free_packet(pkt);
		}

		if(is->quit) {
			LOGE("audio_decode_frame, quit flag is set, return -1!!!");	
			return -1;
		}
		/* next packet */
//		LOGE("audio_decode_frame : try to get next packet!!!");
		
		if(packet_queue_get(&is->audioq, pkt, 1) < 0) {
			LOGE("audio_decode_frame : packet_queue_get failed!!!");
			return -1;
		}
		else {
//			LOGE("audio_decode_frame : packet_queue_get siccess!!!");
		}	
		if(pkt->data == flush_pkt.data) {
			LOGE("audio_decode_frame : avcodec_flush_buffers!!!");
			avcodec_flush_buffers(is->audio_st->codec);
			continue;
		}
//		is->audio_pkt_data = pkt->data;
//		is->audio_pkt_size = pkt->size;
		/* if update, update the audio clock w/pts */
		if(pkt->pts != AV_NOPTS_VALUE) {
			is->audio_clock = av_q2d(is->audio_st->time_base)*pkt->pts;
		}
	}
}

int audioThread() 
{
	VideoState *is = global_video_state;
	int len1, audio_size;
	double pts;
	int ret = 0;

	AVPacket *pkt = &is->audio_pkt;
	pkt->size = 0;
	
	for(;;) {
		if(is->quit) {
			LOGE("audioThread : quit flag is set, return -1!!!");	
			ret = -1;
			break;
		}

//		decodeAudio(is);
		
		// We have already sent all our data; get more 
		int prevAudioSize;
		
		audio_size = audio_decode_frame(is, is->audio_buf, sizeof(is->audio_buf), &pts);
		if(audio_size < 0) {
			// If error, output silence 
			LOGE("decoded audio size is minus, output silence!!!");	
			is->audio_buf_size = 1024;
			memset(is->audio_buf, 0, is->audio_buf_size);
		} else {
			prevAudioSize = audio_size;
			audio_size = synchronize_audio(is, (int16_t *)is->audio_buf, audio_size, pts);
			if(audio_size != prevAudioSize) {
				LOGE("synchronize_audio made change, prevAuidoSize[%d], changedAudioSize[%d]", prevAudioSize, audio_size);
			}
			is->audio_buf_size = audio_size;
		}
		
//		LOGE("audioThread : loop");
//		SDL_Delay(100);
//		is->audio_buf_index = 0;
//		is->audio_buf_size = 4096;
		pushAudioData(is->audio_buf, is->audio_buf_size);
		
	}	
	return ret;
}


int stream_component_open(VideoState *is, int stream_index) 
{
	AVFormatContext *pFormatCtx = is->pFormatCtx;
	AVCodecContext *codecCtx;
	AVCodec *codec;
//	SDL_AudioSpec wanted_spec, spec;

	if(stream_index < 0 || stream_index >= pFormatCtx->nb_streams) {
		LOGE("stream_index is odd [%d]", stream_index);
		return -1;
	}

	// Get a pointer to the codec context for the video stream
	codecCtx = pFormatCtx->streams[stream_index]->codec;

/*	if(codecCtx->codec_type == AVMEDIA_TYPE_AUDIO) {
		// Set audio settings from codec info
		
		wanted_spec.freq = codecCtx->sample_rate;
		wanted_spec.format = AUDIO_S16SYS;
		wanted_spec.channels = codecCtx->channels;
		wanted_spec.silence = 0;
		wanted_spec.samples = SDL_AUDIO_BUFFER_SIZE;
		wanted_spec.callback = audio_callback;
		wanted_spec.userdata = is;

		if(SDL_OpenAudio(&wanted_spec, &spec) < 0) {
			  fprintf(stderr, "SDL_OpenAudio: %s\n", SDL_GetError());
			  return -1;
		}
	}
*/	
	codec = avcodec_find_decoder(codecCtx->codec_id);
	if(!codec || (avcodec_open(codecCtx, codec) < 0)) {
		if(codecCtx->codec_type == AVMEDIA_TYPE_AUDIO) 
		{
			LOGE("Unsupported audio codec!");
		}	
		else if(codecCtx->codec_type == AVMEDIA_TYPE_VIDEO) 
		{
			LOGE("Unsupported video codec!");
		}	
		return -1;
	}

	switch(codecCtx->codec_type) {
		case AVMEDIA_TYPE_AUDIO:
			is->audioStream = stream_index;
			is->audio_st = pFormatCtx->streams[stream_index];
			
			is->audio_buf_size = 0;
			is->audio_buf_index = 0;

			/* averaging filter for audio sync */
			is->audio_diff_avg_coef = exp(log(0.01 / AUDIO_DIFF_AVG_NB));
			is->audio_diff_avg_count = 0;
			/* Correct audio only if larger error than this */
			is->audio_diff_threshold = 2.0 * SDL_AUDIO_BUFFER_SIZE / codecCtx->sample_rate;


			LOGE("audio samplerate[%d], channels[%d], sample_format[%d]", codecCtx->sample_rate, codecCtx->channels, codecCtx->sample_fmt);

			
			memset(&is->audio_pkt, 0, sizeof(is->audio_pkt));
			packet_queue_init(&is->audioq);
			break;
		case AVMEDIA_TYPE_VIDEO:
			is->videoStream = stream_index;
			is->video_st = pFormatCtx->streams[stream_index];

			is->frame_timer = (double)av_gettime() / 1000000.0;
			is->frame_last_delay = 40e-3;
			is->video_current_pts_time = av_gettime();

			packet_queue_init(&is->videoq);
//			is->video_tid = SDL_CreateThread(video_thread, is);

			codecCtx->get_buffer = our_get_buffer;
			codecCtx->release_buffer = our_release_buffer;

			break;
		default:
			break;
	}
	return 0;
}

int decode_interrupt_cb(void) {
	return (global_video_state && global_video_state->quit);
}

int prepareDecode(VideoState *is) 
{
	AVFormatContext *pFormatCtx;
//	AVPacket pkt1, *packet = &pkt1;

	int video_index = -1;
	int audio_index = -1;
	int i;

	is->videoStream=-1;
	is->audioStream=-1;

	global_video_state = is;
	// will interrupt blocking functions if we quit!
	url_set_interrupt_cb(decode_interrupt_cb);

	// Open video file
	if(av_open_input_file(&pFormatCtx, is->filename, NULL, 0, NULL)!=0) {
		LOGE("av_open_input_file failed!!![%s]", is->filename);	
		return -1; // Couldn't open file
	}	

	is->pFormatCtx = pFormatCtx;

	// Retrieve stream information
	if(av_find_stream_info(pFormatCtx)<0) {
		LOGE("av_find_stream_info failed.");	
		return -1; // Couldn't find stream information
	}

	// Dump information about file onto standard error
	dump_format(pFormatCtx, 0, is->filename, 0);

	// Find the first video stream

	for(i=0; i<pFormatCtx->nb_streams; i++) {
		if(pFormatCtx->streams[i]->codec->codec_type==AVMEDIA_TYPE_VIDEO && video_index < 0) {
			video_index=i;
			LOGE("video codec index [%d]", video_index);
		}
		if(pFormatCtx->streams[i]->codec->codec_type==AVMEDIA_TYPE_AUDIO && audio_index < 0) {
			audio_index=i;
			LOGE("audio codec index [%d]", audio_index);
		}
	}

	LOGE("duration[%lld]", pFormatCtx->duration);
	
	if(audio_index >= 0) {
		stream_component_open(is, audio_index);
	}
	if(video_index >= 0) {
		stream_component_open(is, video_index);
	}   

	if(is->videoStream < 0 || is->audioStream < 0) {
		LOGE("%s: could not open codecs", is->filename);
		return -1;
	}
	return 0;

}


int openVideo(const char *filePath)
{
	VideoState      *is;

	LOGE("file name [%s]", filePath);

	is = av_mallocz(sizeof(VideoState));

	// Register all formats and codecs
	avcodec_init();
	av_register_all();

	strncpy(is->filename, filePath, sizeof(is->filename) );

	is->pictq_mutex = SDL_CreateMutex();
	is->pictq_cond = SDL_CreateCond();

	is->av_sync_type = DEFAULT_AV_SYNC_TYPE;
	
	av_init_packet(&flush_pkt);
	flush_pkt.data = "FLUSH";

//	schedule_refresh(is, 40);
		
	return prepareDecode(is); 
}

void closeVideo()
{
	LOGE("closeVideo called, exit");

	if(global_video_state != NULL) {
		global_video_state->quit = 1;
	}	

//	SDL_Delay(100);
//	exit(0); // when didn't call exit, it doesn't work!!
/*
	if (gVideoBuffer != NULL) {
		av_free(gVideoBuffer);
		gVideoBuffer = NULL;
	}
	
	if (gFrame != NULL)
		av_freep(gFrame);
	if (gFrameRGB != NULL)
		av_freep(gFrameRGB);



	if (gVideoCodecCtx != NULL) {
		avcodec_close(gVideoCodecCtx);
		gVideoCodecCtx = NULL;
	}
	
	if (gAudioCodecCtx != NULL) {
		avcodec_close(gAudioCodecCtx);
		gAudioCodecCtx = NULL;
	}
	
	if (gFormatCtx != NULL) {
		av_close_input_file(gFormatCtx);
		gFormatCtx = NULL;
	}
*/
	LOGE("jni-closeMovie() ended!!!");


}

void exitFFmpeg()
{
	LOGE("close() called, exit");
	exit(0);
}


void decode()
{
	VideoState *is = global_video_state;
	AVPacket pkt1, *packet = &pkt1;

	for(;;) {
		if(is->quit) {
			LOGE("jni - decode, quit is set, return!!!");	
		  	break;
		}

		// seek stuff goes here
	    	if(is->seek_req) {
			int stream_index= -1;
			int64_t seek_target = is->seek_pos;
			LOGE("video stream index[%d], audio stream index[%d]", is->videoStream, is->audioStream);
			

			if     (is->videoStream >= 0) stream_index = is->videoStream;
			else if(is->audioStream >= 0) stream_index = is->audioStream;

			LOGE("stream index[%d], seek_target[%lld]", stream_index, seek_target);

			if(stream_index>=0){
				seek_target= av_rescale_q(seek_target, AV_TIME_BASE_Q, is->pFormatCtx->streams[stream_index]->time_base);
			}
			LOGE("after rescale, seek_target[%lld]", seek_target);
			if(av_seek_frame(is->pFormatCtx, stream_index, seek_target, is->seek_flags) < 0) {

				if (is->pFormatCtx->iformat->read_seek) {
					LOGE("format specific");
				} else if(is->pFormatCtx->iformat->read_timestamp) {
					LOGE("frame_binary");
				} else {
					LOGE("generic");
				}

				LOGE("%s: error while seeking. target: %lld, stream_index: %d", is->pFormatCtx->filename, seek_target, stream_index);
			} 
			else {
				LOGE("%s: success seeking. target: %lld, stream_index: %d", is->pFormatCtx->filename, seek_target, stream_index);
				if(is->audioStream >= 0) {
					packet_queue_flush(&is->audioq);
					packet_queue_put(&is->audioq, &flush_pkt);
				}
				if(is->videoStream >= 0) {
					packet_queue_flush(&is->videoq);
					packet_queue_put(&is->videoq, &flush_pkt);
				}
			}
			is->seek_req = 0;
	    	}


		
		// seek stuff goes here
		if(is->audioq.size > MAX_AUDIOQ_SIZE ) {
//			LOGI("jni - audio queue size[%d] exceed max[%d] , sleep!!!", is->audioq.size, MAX_AUDIOQ_SIZE);	
//			LOGI("jni - video queue size[%d]!!!", is->videoq.size);	
			SDL_Delay(10);
			continue;
		}
		
		if(is->videoq.size > MAX_VIDEOQ_SIZE) {
//			LOGI("jni - video queue size exceed max[%d], sleep!!!", MAX_VIDEOQ_SIZE);	
			SDL_Delay(10);
			continue;
		}
		
		if(av_read_frame(is->pFormatCtx, packet) < 0) {
/*			
			if(url_ferror(is->pFormatCtx->pb) == 0) {
				LOGW("jni - no ferror, continue!!!");	
				SDL_Delay(1000); / no error; wait for user input 
				continue;
			} 
			else {
				LOGE("av_read_frame failed!!!!");	
				break;
			}
*/

			if(is->audioq.size > 0 || is->videoq.size > 0) {
				LOGW("av_read_frame failed, but queue is not empty, wait!!!");	
				LOGW("audio queue size[%d], video queue size[%d]!!!", is->audioq.size, is->videoq.size);	
				SDL_Delay(100); 
				continue;
			}
			else {
				LOGE("av_read_frame failed!!!!");	
				break;
			}
			
		}
		// Is this a packet from the video stream?
		if(packet->stream_index == is->videoStream) {
//			LOGI("jni - Video Packet");	
			packet_queue_put(&is->videoq, packet);
		} 
		else if(packet->stream_index == is->audioStream) {
//			LOGI("jni - Audio Packet");	
			packet_queue_put(&is->audioq, packet);
		} 
		else {
//			LOGE("jni - Unknown Packet, Stream Index[%d]", packet->stream_index);	
			av_free_packet(packet);
		}
	}
}

/*
void stream_seek(VideoState *is, int64_t pos, int rel) {
	if(!is->seek_req) {
		is->seek_pos = pos;
		is->seek_flags = rel < 0 ? AVSEEK_FLAG_BACKWARD : 0;
		is->seek_req = 1;
	}
}
*/
void streamSeek(int inc)
{

	VideoState *is = global_video_state;
	double pos;
//	double incr = (double)inc/1000000.0;
	double incr = (double)inc;
	if(is) {
		pos = get_master_clock(is);
		LOGE("streamSeek : origin pos[%f]", pos);
		pos += (double)incr;
		LOGE("streamSeek : incr[%f] seek pos[%f]", (double)incr, pos);
//		stream_seek(is, (int64_t)(pos * AV_TIME_BASE), incr);

 
		if(!is->seek_req) {
			long duration = (long) (is->pFormatCtx->duration / AV_TIME_BASE);
			if(pos > duration) {
				pos = duration;
			}
//			is->seek_pos = (int64_t)(pos * AV_TIME_BASE);
			is->seek_pos = (int64_t)(pos) * (int64_t)AV_TIME_BASE;
			is->seek_flags = incr < 0 ? AVSEEK_FLAG_BACKWARD : 0;
//			is->seek_flags = AVSEEK_FLAG_ANY;
			 
			is->seek_req = 1;
		}		
	}
}

void streamAbsSeek(int pos)
{
	VideoState *is = global_video_state;
	double curr = get_master_clock(is);
	int seek_flags = pos < curr ? AVSEEK_FLAG_BACKWARD : 0;
	if(is) {
		if(!is->seek_req) {
			long duration = (long) (is->pFormatCtx->duration / AV_TIME_BASE);
			if(pos > duration) {
				pos = duration;
			}
			
			is->seek_pos = (int64_t)(pos) * (int64_t)AV_TIME_BASE;
			is->seek_flags = seek_flags;
			is->seek_req = 1;
		}		
	}
}


int getWidth()
{
	VideoState *is = global_video_state;
	AVCodecContext *videoCodecContext = is->video_st->codec;
//	LOGE("video width [%d]", videoCodecContext->width);
	return videoCodecContext->width;

}

int getHeight()
{
	VideoState *is = global_video_state;
	AVCodecContext *videoCodecContext = is->video_st->codec;
//	LOGE("video height [%d]", videoCodecContext->height);
	return videoCodecContext->height;
}

int getDuration()
{
	VideoState *is = global_video_state;
	int64_t duration = is->pFormatCtx->duration;
//	LOGE("duration[%lld]", pFormatCtx->duration);
	int duration_int = (int) (duration / AV_TIME_BASE);
	return duration_int;
}

int getCurrentTime()
{
	VideoState *is = global_video_state;
	int curr_time_int = (int)get_master_clock(is);
//	int curr_time_int = (int)get_audio_clock(is);
	return curr_time_int;
}


