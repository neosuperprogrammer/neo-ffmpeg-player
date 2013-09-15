#include "mySDL_timer.h"
#include "BasicPlayerJni.h"

/* #define DEBUG_TIMERS */

int SDL_timer_started = 0;
int SDL_timer_running = 0;

/* Data used for a thread-based timer */
static int SDL_timer_threaded = 0;


struct _SDL_TimerID {
	Uint32 interval;
	SDL_NewTimerCallback cb;
	void *param;
	Uint32 last_alarm;
	struct _SDL_TimerID *next;
};

static SDL_TimerID SDL_timers = NULL;
static SDL_mutex *SDL_timer_mutex;
static volatile SDL_bool list_changed = SDL_FALSE;

int SDL_TimerInit(void)
{
	int retval;

	retval = 0;
	if ( SDL_timer_started ) {
		SDL_TimerQuit();
	}
	if ( ! SDL_timer_threaded ) {
		retval = SDL_SYS_TimerInit();
	}
	if ( SDL_timer_threaded ) {
		SDL_timer_mutex = SDL_CreateMutex();
	}
	if ( retval == 0 ) {
		SDL_timer_started = 1;
	}
	return(retval);
}

void SDL_TimerQuit(void)
{
	SDL_SetTimer(0, NULL);
	if ( SDL_timer_threaded < 2 ) {
		SDL_SYS_TimerQuit();
	}
	if ( SDL_timer_threaded ) {
		SDL_DestroyMutex(SDL_timer_mutex);
		SDL_timer_mutex = NULL;
	}
	SDL_timer_started = 0;
	SDL_timer_threaded = 0;
}

static SDL_TimerID SDL_AddTimerInternal(Uint32 interval, SDL_NewTimerCallback callback, void *param)
{
	SDL_TimerID t;
	t = (SDL_TimerID) SDL_malloc(sizeof(struct _SDL_TimerID));
	if ( t ) {
		t->interval = ROUND_RESOLUTION(interval);
		t->cb = callback;
		t->param = param;
		t->last_alarm = SDL_GetTicks();
		t->next = SDL_timers;
		SDL_timers = t;
		++SDL_timer_running;
		list_changed = SDL_TRUE;
	}
#ifdef DEBUG_TIMERS
	printf("SDL_AddTimer(%d) = %08x num_timers = %d\n", interval, (Uint32)t, SDL_timer_running);
#endif
	return t;
}

int SDL_SYS_StartTimer(void)
{
	SDL_SetError("Internal logic error: Linux uses threaded timer");
	return(-1);
}


int SDL_SetTimer(Uint32 ms, SDL_TimerCallback callback)
{
	int retval;

	retval = 0;

	if ( SDL_timer_threaded ) {
		SDL_mutexP(SDL_timer_mutex);
	}
	if ( SDL_timer_running ) {	/* Stop any currently running timer */
		if ( SDL_timer_threaded ) {
			while ( SDL_timers ) {
				SDL_TimerID freeme = SDL_timers;
				SDL_timers = SDL_timers->next;
				SDL_free(freeme);
			}
			SDL_timer_running = 0;
			list_changed = SDL_TRUE;
		} else {
			SDL_SYS_StopTimer();
			SDL_timer_running = 0;
		}
	}
	if ( ms ) {
		if ( SDL_timer_threaded ) {
			if ( SDL_AddTimerInternal(ms, callback_wrapper, (void *)callback) == NULL ) {
				retval = -1;
			}
		} else {
			SDL_timer_running = 1;
			SDL_alarm_interval = ms;
			SDL_alarm_callback = callback;
			retval = SDL_SYS_StartTimer();
		}
	}
	if ( SDL_timer_threaded ) {
		SDL_mutexV(SDL_timer_mutex);
	}

	return retval;

}

