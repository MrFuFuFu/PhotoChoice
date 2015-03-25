package com.mrfu.photochoice.lib;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Video.Thumbnails;

/**
 * Local Photos loader, using asynchronous parsing local images, 
 * singleton use getInstance () Gets NativeImageLoader instance call loadNativeImage () method to load local images, 
 * such as a picture of the tools loaded locally
 */
public class NativeImageLoader {
//	private LruCache<String, Bitmap> mMemoryCache;
	private static LruMemoryCache<String, Bitmap> mMemoryCache;
	private static NativeImageLoader mInstance = new NativeImageLoader();
	private ExecutorService mImageThreadPool = Executors.newFixedThreadPool(1);
	
	
	
	private NativeImageLoader(){
		if (mMemoryCache == null) {
			long maxMemory = Runtime.getRuntime().maxMemory();
			// Use 1/8 of the maximum available memory value as the size of the cache.
			int cacheSize = (int) (maxMemory / 16);
			mMemoryCache = new LruMemoryCache<String, Bitmap>(cacheSize) {
				/**
				 * Measure item size in bytes rather than units which is more
				 * practical for a bitmap cache
				 */
				@Override
				protected int sizeOf(String key, Bitmap bitmap) {
					return getBitmapSize(bitmap);
				}
			};
		}
	}

	public static int getBitmapSize(Bitmap bitmap) {
		if(bitmap != null){
			return bitmap.getRowBytes() * bitmap.getHeight();
		}
		return 0;
	}

	public static NativeImageLoader getInstance(){
		return mInstance;
	}
	
	
	/**
	 * Loading local images, the picture is not cropped
	 */
	public Bitmap loadNativeImage(final boolean isVideo, final String path, final NativeImageCallBack mCallBack){
		return this.loadNativeImage(isVideo, path, null, mCallBack);
	}
	
	public Bitmap loadNativeImage(final boolean isVideo, final String path, final Point mPoint, final NativeImageCallBack mCallBack){
		//First obtain memory Bitmap
		Bitmap bitmap = getBitmapFromMemCache(path);
		
		final Handler mHander = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				mCallBack.onImageLoader((Bitmap)msg.obj, path);
			}
			
		};
		
		//If the Bitmap is not in the cache memory, enable thread to load the local picture and added to mMemoryCache in Bitmap
		if(bitmap == null){
			mImageThreadPool.execute(new Runnable() {
				
				@Override
				public void run() {
					//First get the picture thumbnail
					Bitmap mBitmap = decodeThumbBitmapForFile(isVideo, path, mPoint == null ? 0: mPoint.x, mPoint == null ? 0: mPoint.y);
					Message msg = mHander.obtainMessage();
					msg.obj = mBitmap;
					mHander.sendMessage(msg);
					
					//The picture is added to the cache memory
					addBitmapToMemoryCache(path, mBitmap);
				}
			});
		}
		return bitmap;
		
	}

	
	
	/**
	 * Adding memory to cache Bitmap
	 */
	private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemCache(key) == null && bitmap != null) {
			mMemoryCache.put(key, bitmap);
		}
	}

	/**
	 * According to the memory key to get in the picture
	 * @param key
	 * @return
	 */
	private Bitmap getBitmapFromMemCache(String key) {
		return mMemoryCache.get(key);
	}
	
	/**
	 * According View (mainly ImageView) to get the width and height of the thumbnail pictures
	 * @param isVideo 
	 * @param path
	 * @param viewWidth
	 * @param viewHeight
	 * @return
	 */
	private Bitmap decodeThumbBitmapForFile(boolean isVideo, String path, int viewWidth, int viewHeight){
		if (isVideo) {
			Bitmap bitmap = null;
			bitmap = ThumbnailUtils.createVideoThumbnail(path, Thumbnails.FULL_SCREEN_KIND);
			//ThumbnailUtils use to generate the specified size of thumbnails
			bitmap = ThumbnailUtils.extractThumbnail(bitmap, viewWidth, viewHeight, ThumbnailUtils.OPTIONS_RECYCLE_INPUT); 
//			Log.i("MrFu", "time2 = " + System.currentTimeMillis());
			if (bitmap != null) {
				bitmap = Bitmap.createScaledBitmap(bitmap, viewWidth, viewHeight, false);
//				Log.i("MrFu", "time3 = " + System.currentTimeMillis());
				return bitmap;
			}
			return null;
		}else {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(path, options);
			options.inSampleSize = computeScale(options, viewWidth, viewHeight);
			
			//Set to false, analytic Bitmap object added to the memory
			options.inJustDecodeBounds = false;
			
			return BitmapFactory.decodeFile(path, options);
		}

	}
	
	
	/**
	 * According View  in width and height to calculate the Bitmap scaling. The default is not scaled
	 * @param options
	 * @param width
	 * @param height
	 */
	private int computeScale(BitmapFactory.Options options, int viewWidth, int viewHeight){
		int inSampleSize = 1;
		if(viewWidth == 0 || viewWidth == 0){
			return inSampleSize;
		}
		int bitmapWidth = options.outWidth;
		int bitmapHeight = options.outHeight;
		
		//If Bitmap width or height is greater than we set the width and height of the image View, the calculated scaling
		if(bitmapWidth > viewWidth || bitmapHeight > viewWidth){
			int widthScale = Math.round((float) bitmapWidth / (float) viewWidth);
			int heightScale = Math.round((float) bitmapHeight / (float) viewWidth);
			
			//In order to ensure that the picture is not scaled deformation, we take the smallest aspect ratio
			inSampleSize = widthScale < heightScale ? widthScale : heightScale;
		}
		return inSampleSize;
	}
	
	
	/**
	 *Loading local images callback interface
	 */
	public interface NativeImageCallBack{
		/**
		 * When the child thread finished loading local images, the Bitmap and pictures in this method callback path
		 */
		public void onImageLoader(Bitmap bitmap, String path);
	}
}
