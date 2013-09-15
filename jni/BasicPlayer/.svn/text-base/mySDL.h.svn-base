#ifndef __MY_SDL_H__
#define __MY_SDL_H__

#include <sys/time.h>
#include <unistd.h>
#include <errno.h>
#include <pthread.h>


#define	SDL_INIT_TIMER		0x00000001
#define SDL_INIT_AUDIO		0x00000010
#define SDL_INIT_VIDEO		0x00000020
#define SDL_INIT_CDROM		0x00000100
#define SDL_INIT_JOYSTICK	0x00000200
#define SDL_INIT_NOPARACHUTE	0x00100000	/**< Don't catch fatal signals */
#define SDL_INIT_EVENTTHREAD	0x01000000	/**< Not supported on all OS's */
#define SDL_INIT_EVERYTHING	0x0000FFFF

#define SDL_malloc	malloc
#define SDL_calloc	calloc
#define SDL_free	free


/** The SDL mutex structure, defined in SDL_mutex.c */
struct SDL_mutex;
typedef struct SDL_mutex SDL_mutex;

/** The SDL condition variable structure, defined in SDL_cond.c */
struct SDL_cond;
typedef struct SDL_cond SDL_cond;


typedef enum {
	SDL_FALSE = 0,
	SDL_TRUE  = 1
} SDL_bool;

typedef int8_t		Sint8;
typedef uint8_t		Uint8;
typedef int16_t		Sint16;
typedef uint16_t	Uint16;
typedef int32_t		Sint32;
typedef uint32_t	Uint32;


int SDL_Init();

void SDL_Quit(void);

void SDL_SetError (const char *fmt, ...);

SDL_mutex *SDL_CreateMutex (void);
void SDL_DestroyMutex(SDL_mutex *mutex);

#define SDL_LockMutex(m)	SDL_mutexP(m)
/** Lock the mutex
 *  @return 0, or -1 on error
 */
int SDL_mutexP(SDL_mutex *mutex);

#define SDL_UnlockMutex(m)	SDL_mutexV(m)
/** Unlock the mutex
 *  @return 0, or -1 on error
 *
 *  It is an error to unlock a mutex that has not been locked by
 *  the current thread, and doing so results in undefined behavior.
 */
int SDL_mutexV(SDL_mutex *mutex);


SDL_cond * SDL_CreateCond(void);
void SDL_DestroyCond(SDL_cond *cond);
int SDL_CondSignal(SDL_cond *cond);
int SDL_CondBroadcast(SDL_cond *cond);
int SDL_CondWait(SDL_cond *cond, SDL_mutex *mutex);
Uint32 SDL_GetTicks (void);


void SDL_Delay (Uint32 ms);

#endif
