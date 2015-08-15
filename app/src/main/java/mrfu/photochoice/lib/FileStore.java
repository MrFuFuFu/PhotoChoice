package mrfu.photochoice.lib;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * @author Mr.Fu 2015-3-25 
 * 		am 10:36:24
 */
public class FileStore {
	public static String createNewCacheFile(Context context) {
		return createNewCacheFile(context, UUID.randomUUID().toString());
	}

	public static String createNewCacheFile(Context context, String fileName) {
		return createNewCacheFile(context, fileName, false);
	}

	public static String createNewCacheFile(Context context, String fileName, boolean addTmp) {
		synchronized (context) {
			boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
			String path = "";
			if (sdCardExist) {
				path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/mrfu_photo/important/";
			} else {
				path = context.getCacheDir().getAbsolutePath() + "/mrfu_photo/important/";
			}
			File fileDir = new File(path);
			if (!fileDir.exists()) {
				fileDir.mkdirs();
				File noScanFile = new File(path + ".nomedia");
				try {
					noScanFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
					clearFile(context);
				}
			}
			String name = MD5Util.md5(fileName);
			String pathName = path + name;
			if (addTmp) {
				pathName += ".tmp";
			}
			File file = new File(pathName);
			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return pathName;
		}
	}

	private static void clearFile(final Context context) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				File cacheDir = context.getApplicationContext().getCacheDir();
				deleteFiles(cacheDir);
				boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
				if (sdCardExist) {
					try {
						deleteFiles(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/mrfu_photo/important/"));
					} catch (Exception e) {
					}
				}
			}
		}).start();
	}

	// delete files
	public static void deleteFiles(File file) {
		try {
			if (file.exists()) {
				if (file.isFile()) {
					file.delete();
				} else if (file.isDirectory()) {
					File files[] = file.listFiles();
					for (int i = 0; i < files.length; i++) {
						deleteFiles(files[i]);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
