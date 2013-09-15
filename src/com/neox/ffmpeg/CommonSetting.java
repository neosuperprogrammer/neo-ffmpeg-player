/*
 * com.btb.tcloud.common.CommonSetting
 *
 * Created on 2010. 10. 4.
 * 
 * Copyright (c) 2002-2010 BTBSolution Co., Ltd. All Rights Reserved.
 */

package com.neox.ffmpeg;

/**
 * 
 * 
 * Create Date 2010. 10. 4.
 * 
 * @version 1.00 2010. 10. 4.
 * @since 1.00
 * @see
 * @author swnam (swnam@btbsolution.co.kr)
 * 
 *         Revision History who when what swnam 2010. 10. 4. 최초 작성
 */

public final class CommonSetting {

	/**
	 * 제품의 조건 : - android:debuggable="false" 추가
	 */
	public static final boolean IS_PRODUCT = true; 		// 제품인가?
	public static final boolean IS_EMUL = false; 		// Emulator로 테스트하는 경우(전화번호나  IMEI 값 고정)
	public static final boolean IS_KILL_PROCESS_WHEN_EXIT = true;

	public static class Log {
		// Log 출력 여부 (false 일 경우라도 w, e 메시지는 출력한다.)
		public static final boolean IS_SHOW = true;
		// Log Tag 통일
		public static final boolean IS_ONE_TAG = true;
		// 상세 메시지 출력
		public static final boolean IS_VERBOSE = true;
		// Log 파일 출력
		public static final boolean ENABLE_LOG_IN_FILE = false;
		// Speed Check Flag
		public static final boolean IS_SPEED_CHECK = false;
		// Memory Check Log
		public static final boolean IS_MEMORY_CHECK = false;
	}
}