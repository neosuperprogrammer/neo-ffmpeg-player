/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.neox.ffmpeg;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

/**
 * This helper class download images from the Internet and binds those with the provided ImageView.
 *
 * <p>It requires the INTERNET permission, which should be added to your application's manifest
 * file.</p>
 *
 * A local cache of downloaded images is maintained internally to improve performance.
 */
public class ImageDownloader {
	private static final String LOG_TAG = ImageDownloader.class.getSimpleName();
	
	private ScaleType scaleType;
	private ProgressListener listener;
	private String cacheKeyPrefix;
	private boolean bUseCache = true;

//	private long maxSize;
//	private String downloadUrl;
	
	// 싸이월드를 위해 추가
	private IImgDownloader	mInterface;
	
	//////////////////////////////////////////////////////////////////////////////
	private Context mContext;

	//////////////////////////////////////////////////////////////////////////////
	public interface ProgressListener {
	    public void onError( final String url );
	}
	
	
	public ImageDownloader(Context context) {
		super();
		scaleType = null;
		listener = null;
		cacheKeyPrefix = null;
		bUseCache = true;
		mContext = context;
//		maxSize = 800 * 480 * 2;
	}
	
	public void destroy() {
		listener = null;
		mInterface = null;
		clearCache();
	}


	/**
	 * Download the specified image from the Internet and binds it to the provided ImageView. The
	 * binding is immediate if the image is found in the cache and will be done asynchronously
	 * otherwise. A null bitmap will be associated to the ImageView if an error occurs.
	 *
	 * @param url The URL of the image to download.
	 * @param imageView The ImageView to bind the downloaded image to.
	 */
	public void download(final long id, final String path, final ImageView imageView) {
		resetPurgeTimer();
		final Bitmap bitmap = getBitmapFromCache(path);

		setScale(imageView);
		
		if (bitmap == null) {
			forceDownload(id, path, imageView);
		} else {
			cancelPotentialDownload(path, imageView);
			imageView.setImageBitmap(bitmap);
			imageView.setBackgroundColor(Color.TRANSPARENT);
			imageView.setTag("bitmap");
		}
	}
	
	public void setEventListener(ProgressListener listener) {
		this.listener = listener;
	}
	
	public void setScaleType(ImageView.ScaleType scaletype) {
		this.scaleType = scaletype;
	}
	
	private void setScale(final ImageView imageView) {
		if (scaleType != null) {
			imageView.setScaleType(scaleType);
		}
	}
	
	private void error(String url) {
		if (listener != null) {
			listener.onError(url);
		}
	}
	
	/**
	 * Same as download but the image is always downloaded and the cache is not used.
	 * Kept private at the moment as its interest is not clear.
	 */
	private void forceDownload(final long id, final String path, final ImageView imageView) {
		// State sanity: url is guaranteed to never be null in DownloadedDrawable and cache keys.
		if (path == null) {
			imageView.setImageDrawable(null);
			return;
		}

		if (cancelPotentialDownload(path, imageView)) {
			final BitmapDownloaderTask task = new BitmapDownloaderTask(mContext, imageView, id);
			final DownloadedDrawable downloadedDrawable = new DownloadedDrawable(task);
			imageView.setImageDrawable(downloadedDrawable);
			task.putFirst(task, path);
		}
	}
	
	/**
	 * Returns true if the current download has been canceled or if there was no download in
	 * progress on this image view.
	 * Returns false if the download in progress deals with the same url. The download is not
	 * stopped in that case.
	 */
	private static boolean cancelPotentialDownload(final String url, final ImageView imageView) {
		final BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

		if (bitmapDownloaderTask != null) {
			final String bitmapUrl = bitmapDownloaderTask.url;
			if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
				bitmapDownloaderTask.cancel(true);
			} else {
				// The same URL is already being downloaded.
				return false;
			}
		}
		return true;
	}

	/**
	 * @param imageView Any imageView
	 * @return Retrieve the currently active download task (if any) associated with this imageView.
	 * null if there is no such task.
	 */
	private static BitmapDownloaderTask getBitmapDownloaderTask(final ImageView imageView) {
		if (imageView != null) {
			final Drawable drawable = imageView.getDrawable();
			if (drawable instanceof DownloadedDrawable) {
				final DownloadedDrawable downloadedDrawable = (DownloadedDrawable)drawable;
				return downloadedDrawable.getBitmapDownloaderTask();
			}
		}
		return null;
	}
	
//	Bitmap downloadBitmap(final String url) {
//		// AndroidHttpClient is not allowed to be used from the main thread
//		final HttpClient client = AndroidHttpClient.newInstance("Android");
//
//		HttpGet getRequest;
//		try {
//			getRequest = new HttpGet(url);
//		}
//		catch (IllegalArgumentException e) {
//			LogUtil.e(LOG_TAG, "IllegalArgumentException, " + e.getMessage());
//			e.printStackTrace();
//			return null;
//		}
//
//		try {
//			final HttpResponse response = client.execute(getRequest);
//			final int statusCode = response.getStatusLine().getStatusCode();
//			if (statusCode != HttpStatus.SC_OK) {
//				LogUtil.w(LOG_TAG, "Error " + statusCode + " while retrieving bitmap from " + url);
//				return null;
//			}
//
//			final HttpEntity entity = response.getEntity();
//			if (entity != null) {
//				InputStream inputStream = null;
//				try {
//					inputStream = entity.getContent();
//					// Bug on slow connections, fixed in future release.
//					final FlushedInputStream is = new FlushedInputStream(inputStream);
//					
//					Bitmap bmp = null;
//					
//					if (mAdjustInSampleSize) {
//						byte[] array = Util.inputStreamToBytes(is);
//						
//						BitmapFactory.Options option = new BitmapFactory.Options();
//						option.inJustDecodeBounds = true;
//
//						BitmapFactory.decodeByteArray(array, 0, array.length, option);
//
//						int sampleSize = 1;
//
//						int w = option.outWidth;
//						int h = option.outHeight;
//						while ((w / 2) >= mWantsWidth && (h / 2) >= mWantsHeight) {
//							sampleSize *= 2;
//							w = w / 2;
//							h = h / 2;
//						}
//
//						option.inJustDecodeBounds = false;
//						option.inSampleSize = sampleSize;
//
//						bmp = BitmapFactory.decodeByteArray(array, 0, array.length, option);
//						
//						if (bmp != null) {
//							LogUtil.w(LOG_TAG, "sampleSize: " + sampleSize + " w: " + bmp.getWidth() + " h: " + bmp.getHeight());
//						}
//					}
//					else {
//						try {
//							bmp = BitmapFactory.decodeStream(is);
//						}
//						catch (OutOfMemoryError e) {
//							LogUtil.e(LOG_TAG, "Out Of Memory Error !!!");
//							e.printStackTrace();
//						}
//					}
//					
//					if (bmp == null) {
//						LogUtil.w(LOG_TAG, "error while decodeStream from " + url);
//					}
//					
//					return bmp;
//				}
//				finally {
//					if (inputStream != null) {
//						inputStream.close();
//					}
//					entity.consumeContent();
//				}
//			}
//		}
//		catch (final IOException e) {
//			getRequest.abort();
//			LogUtil.w(LOG_TAG, "I/O error while retrieving bitmap from " + url, e);
//		}
//		catch (final IllegalStateException e) {
//			getRequest.abort();
//			LogUtil.w(LOG_TAG, "Incorrect URL: " + url);
//		}
//		catch (final Exception e) {
//			getRequest.abort();
//			LogUtil.w(LOG_TAG, "Error while retrieving bitmap from " + url, e);
//		}
//		finally {
//			if ((client instanceof AndroidHttpClient)) {
//				((AndroidHttpClient) client).close();
//			}
//		}
//		return null;
//	}
	
	/*
	 * An InputStream that skips the exact number of bytes provided, unless it reaches EOF.
	 */
	static class FlushedInputStream extends FilterInputStream {
		public FlushedInputStream(final InputStream inputStream) {
			super(inputStream);
		}

		@Override
		public long skip(final long n) throws IOException {
			long totalBytesSkipped = 0L;
			while (totalBytesSkipped < n) {
				long bytesSkipped = in.skip(n - totalBytesSkipped);
				if (bytesSkipped == 0L) {
					final int b = read();
					if (b < 0) {
						break;  // we reached EOF
					} else {
						bytesSkipped = 1; // we read one byte
					}
				}
				totalBytesSkipped += bytesSkipped;
			}
			return totalBytesSkipped;
		}
	}

	class Item {
		BitmapDownloaderTask task;
		String url;

		Item(final BitmapDownloaderTask task, final String url) {
			this.task = task;
			this.url = url;
		}
	}

	private static final LinkedBlockingDeque<Item> sDeque = new LinkedBlockingDeque<Item>();
	
	/**
	 * The actual AsyncTask that will asynchronously download the image.
	 */
	class BitmapDownloaderTask extends CustAsyncTask<String, Void, Bitmap> {
		private long id;
		private String url;
		private String path;
		private Context mContext;
		
		private final WeakReference<ImageView> imageViewReference;

		public BitmapDownloaderTask(Context context, final ImageView imageView, final long id) {
			imageViewReference = new WeakReference<ImageView>(imageView);
			this.id = id;
			this.path = null;			
			this.mContext = context;
		}

		/**
		 * Actual download method.
		 */
		@Override
		protected Bitmap doInBackground(final String... params) {
			url = params[0];
			Bitmap bmp = null;
			
			if (id != 0) {
				bmp = MediaUtil.getVideoThumbnail(mContext, id);
			}
			else if (path != null) {
				bmp = MediaUtil.getVideoThumbnail(mContext, path);
			}
			else {
//				bmp = downloadBitmap(url);
			}
			
			return bmp;
		}

		/**
		 * Once the image is downloaded, associates it to the imageView
		 */
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			
			if (isCancelled()) {
				LogUtil.v(LOG_TAG, "isCancelled");
				bitmap = null;
				if (mInterface != null) {
					mInterface.onDownloadError();
				}
			}
			else {
				if (bitmap != null) {

					if (bUseCache) {
//						LogUtil.i(LOG_TAG, "add to cache [" + url + "]");
						addBitmapToCache(url, bitmap);
					}
					else {
						addBitmapToSoftCache(url, bitmap);
					}

					if (imageViewReference != null) {
						final ImageView imageView = imageViewReference.get();
						final BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
						// Change bitmap only if this process is still associated with it
						// Or if we don't use any bitmap to task association (NO_DOWNLOADED_DRAWABLE mode)
						if (this == bitmapDownloaderTask) {
							imageView.setImageBitmap(bitmap);
							imageView.setBackgroundColor(Color.TRANSPARENT);
							imageView.setTag("bitmap");
						}
					}

					if (mInterface != null) {
						if (bitmap != null && bitmap.getHeight() > 1) 
							mInterface.onDownloadComplete();
						else
							mInterface.onDownloadError();
					}
				}
				else {
					LogUtil.e(LOG_TAG, "bitmap is null.");
					final ImageView imageView = imageViewReference.get();
					final BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
					// Change bitmap only if this process is still associated with it
					// Or if we don't use any bitmap to task association (NO_DOWNLOADED_DRAWABLE mode)
					if (this == bitmapDownloaderTask) {
						imageView.setImageResource(R.drawable.icon_default_video);
					}
					error(url);
					if (mInterface != null) {
						mInterface.onDownloadError();
					}
					

				}
			}

			if (!sDeque.isEmpty()) {
				dispatchTask();
			}
			super.onPostExecute(null);
		}

		@Override
		protected void onCancelled() {
			if (!sDeque.isEmpty()) {
				dispatchTask();
			}
			super.onCancelled();
		}
		
		public void putFirst(BitmapDownloaderTask task, String url) {
			final Item item = new Item(task, url);
			sDeque.addFirst(item);
			dispatchTask();
		}
		
		private void dispatchTask() {
			Item obj;
			while (!sDeque.isEmpty() && getWorkQueueSize() <= 0) {
				obj = sDeque.pop();
				obj.task.execute(obj.url);
			}
		}
	}

	/**
	 * A fake Drawable that will be attached to the imageView while the download is in progress.
	 *
	 * <p>Contains a reference to the actual download task, so that a download task can be stopped
	 * if a new binding is required, and makes sure that only the last started download process can
	 * bind its result, independently of the download finish order.</p>
	 */
	static class DownloadedDrawable extends ColorDrawable {
		private final WeakReference<BitmapDownloaderTask> bitmapDownloaderTaskReference;

		public DownloadedDrawable(final BitmapDownloaderTask bitmapDownloaderTask) {
			super(Color.TRANSPARENT);
			bitmapDownloaderTaskReference =
				new WeakReference<BitmapDownloaderTask>(bitmapDownloaderTask);
		}

		public BitmapDownloaderTask getBitmapDownloaderTask() {
			return bitmapDownloaderTaskReference.get();
		}
	}

	/*
	* Cache-related fields and methods.
	* 
	* We use a hard and a soft cache. A soft reference cache is too aggressively cleared by the
	* Garbage Collector.
	*/

	private static final int HARD_CACHE_CAPACITY = 100; //200;
	private static final int HARD_CACHE_CAPACITY_SMALL = 50; //100;
//	private static final int DELAY_BEFORE_PURGE = 60 * 60 * 1000; // in milliseconds

	// Hard cache, with a fixed maximum capacity and a life duration
	private static final HashMap<String, Bitmap> sHardBitmapCache =
		new LinkedHashMap<String, Bitmap>(getCacheCapacity() / 2, 0.75f, true) {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2994406034032240417L;

		@Override
		protected boolean removeEldestEntry(final Map.Entry<String, Bitmap> eldest) {
			if (size() > getCacheCapacity()) {
				// Entries push-out of hard reference cache are transferred to soft reference cache
				sSoftBitmapCache.put(eldest.getKey(), new SoftReference<Bitmap>(eldest.getValue()));
				return true;
			} else {
				return false;
			}
		}
	};
	
	// Soft cache for bitmaps kicked out of hard cache
	private final static ConcurrentHashMap<String, SoftReference<Bitmap>> sSoftBitmapCache =
		new ConcurrentHashMap<String, SoftReference<Bitmap>>(getCacheCapacity() / 2);

	private static int getCacheCapacity() {
		if(ModelUtil.isSmallMemoryModel()) {
			return HARD_CACHE_CAPACITY_SMALL;
		} else {
			return HARD_CACHE_CAPACITY;
		}
	}

//	private final Handler purgeHandler = new Handler();
//
//	private final Runnable purger = new Runnable() {
//		public void run() {
//			clearCache();
//		}
//	};

	/**
	 * Adds this bitmap to the cache.
	 * @param bitmap The newly downloaded bitmap.
	 */
	private void addBitmapToCache(final String url, final Bitmap bitmap) {
		if (bitmap != null) {
			final String key;
			if (cacheKeyPrefix != null) {
				key = cacheKeyPrefix + url;
			}
			else {
				key = url;
			}
			synchronized (sHardBitmapCache) {
//				LogUtil.e(LOG_TAG, "add to hard cache, key [" + key + "]");
				sHardBitmapCache.put(key, bitmap);
//				LogUtil.i(LOG_TAG, "hard size : " + sHardBitmapCache.size() + ",  soft size : " + sSoftBitmapCache.size());
			}
		}
	}
	
	private void addBitmapToSoftCache(final String url, final Bitmap bitmap) {
		if (bitmap != null) {
			final String key;
			if (cacheKeyPrefix != null) {
				key = cacheKeyPrefix + url;
			}
			else {
				key = url;
			}
			synchronized (sSoftBitmapCache) {
				sSoftBitmapCache.put(key,  new SoftReference<Bitmap>(bitmap));
			}
		}
	}

	/**
	 * @param url The URL of the image that will be retrieved from the cache.
	 * @return The cached bitmap or null if it was not found.
	 */
	private Bitmap getBitmapFromCache(final String url) {
		final String key;
		
		if (cacheKeyPrefix != null) {
			key = cacheKeyPrefix + url;
		}
		else {
			key = url;
		}
		
		if (!bUseCache) {
			return null;
		}
		
//		LogUtil.i(LOG_TAG , "try to get from cache, key [" + key + "]");
		
		// First try the hard reference cache
		synchronized (sHardBitmapCache) {
			final Bitmap bitmap = sHardBitmapCache.get(key);
			if (bitmap != null) {
//				LogUtil.i(LOG_TAG, "Bitmap found in hard cache.  key : " + key);
				// Bitmap found in hard cache
				// Move element to first position, so that it is removed last
				sHardBitmapCache.remove(key);
				sHardBitmapCache.put(key, bitmap);
				return bitmap;
			}
		}

		// Then try the soft reference cache
		final SoftReference<Bitmap> bitmapReference = sSoftBitmapCache.get(key);
		if (bitmapReference != null) {
			final Bitmap bitmap = bitmapReference.get();
			if (bitmap != null) {
				// Bitmap found in soft cache
//				LogUtil.i(LOG_TAG, "Bitmap found in soft cache.  key : " + key);
				return bitmap;
			} else {
				// Soft reference has been Garbage Collected
				LogUtil.d(LOG_TAG, "Soft reference has been Garbage Collected.  " + key);
				sSoftBitmapCache.remove(key);
			}
		}

		return null;
	}

	/**
	 * Clears the image cache used internally to improve performance. Note that for memory
	 * efficiency reasons, the cache will automatically be cleared after a certain inactivity delay.
	 */
	public static void clearCache() {
		LogUtil.i(LOG_TAG, "hard size : " + sHardBitmapCache.size() + ",  soft size : " + sSoftBitmapCache.size());
		synchronized (sHardBitmapCache) {
			recycleBitmapOfHardCache();
			sHardBitmapCache.clear();
		}
		synchronized (sSoftBitmapCache) {
			recycleBitmapOfSoftCache();
			sSoftBitmapCache.clear();
		}
	}
	
	private static void recycleBitmapOfHardCache() {
		Iterable<Entry<String,Bitmap>> set = sHardBitmapCache.entrySet();
		Iterator<java.util.Map.Entry<String, Bitmap>> iter = set.iterator();
		while (iter.hasNext()) {
			java.util.Map.Entry<String, Bitmap> entry = iter.next();
			Bitmap bmp = entry.getValue();
			if (bmp != null  && !bmp.isRecycled()) {
				bmp.recycle();
			}
		}
	}
	
	private static void recycleBitmapOfSoftCache() {
		Iterable<Entry<String,SoftReference<Bitmap>>> set = sSoftBitmapCache.entrySet();
		Iterator<java.util.Map.Entry<String, SoftReference<Bitmap>>> iter = set.iterator();
		while (iter.hasNext()) {
			java.util.Map.Entry<String, SoftReference<Bitmap>> entry = iter.next();
			Bitmap bmp = entry.getValue().get();
			if (bmp != null && !bmp.isRecycled()) {
				bmp.recycle();
			}
		}
	}

	/**
	 * Allow a new delay before the automatic cache clear is done.
	 */
	private void resetPurgeTimer() {
//		purgeHandler.removeCallbacks(purger);
//		purgeHandler.postDelayed(purger, DELAY_BEFORE_PURGE);
	}

	public void setInterface(IImgDownloader iface){
		mInterface = iface;
	}
	
	public interface IImgDownloader {
		void onDownloadComplete();
		void onDownloadError();
	}

	public void setCacheKeyPrefix(String prefix) {
		cacheKeyPrefix = prefix;
	}

	public void setUseCache(boolean bUse) {
		this.bUseCache = bUse;
	}

	public void clearSoftCache() {
		LogUtil.d(LOG_TAG, "hard size : " + sHardBitmapCache.size() + ",  soft size : " + sSoftBitmapCache.size());
		synchronized (sSoftBitmapCache) {			
			recycleBitmapOfSoftCache();			
			sSoftBitmapCache.clear();
		}		
	}
}

class MediaUtil {
	
	public static Bitmap getVideoThumbnail(Context context, long id) {
		final ContentResolver contentResolver = context.getContentResolver();
		Bitmap bmp = MediaStore.Video.Thumbnails.getThumbnail(contentResolver, id, 
														MediaStore.Video.Thumbnails.MICRO_KIND, null);
		return bmp;
	}
	
	public static Bitmap getVideoThumbnail(Context context, String path) {
		final ContentResolver contentResolver = context.getContentResolver();
		final String selection = MediaColumns.DATA + "=\"" + path + "\"";
		long id = 0;
		id = getVideoId(contentResolver, selection);
		Bitmap bmp = getVideoThumbnail(context, id);
		return bmp;
	}
	
	private static long getVideoId(final ContentResolver contentResolver, final String selection) {
		final Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
		final String[] proj = { BaseColumns._ID, MediaColumns.DATA };
		final Cursor cursor = contentResolver.query(uri, proj, selection, null, null);
		
		long id = 0;

		if (cursor != null) {
			if (cursor.moveToFirst()) {
				final int idCol = cursor.getColumnIndex(BaseColumns._ID);
				id = cursor.getLong(idCol);
			}
			cursor.close();
		}

		return id;
	}
}
