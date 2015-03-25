package com.mrfu.photochoice;

import android.app.Application;

/**
 * @author Mr.Fu
 * 2015-3-25 上午10:43:25
 */
public class PhotoApplication extends Application {
	private static PhotoApplication sInstance;

	public static PhotoApplication getInstance() {
		return sInstance;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		sInstance = this;
	}
}
