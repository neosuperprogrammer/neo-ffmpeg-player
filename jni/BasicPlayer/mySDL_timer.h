#ifndef __MY_SDL_TIMER_H__
#define __MY_SDL_TIMER_H__

#include <sys/time.h>
#include <unistd.h>
#include <errno.h>
#include <pthread.h>
#include "mySDL.h"


/** Definition of the timer ID type */
typedef struct _SDL_TimerID *SDL_TimerID;


/** Function prototype for the timer callback function */
typedef Uint32 (SDLCALL *SDL_TimerCallback)(Uint32 interval);
typedef Uint32 (SDLCALL *SDL_NewTimerCallback)(Uint32 interval, void *param);

int SDL_TimerInit(void);


#endif
