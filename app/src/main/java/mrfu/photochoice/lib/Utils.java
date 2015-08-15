package mrfu.photochoice.lib;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import mrfu.photochoice.R;

/**
 * @author Mr.Fu
 * 2015-3-25 am 10:16:52
 */
public class Utils {
	public static final String SELECTED_ACTION_VIDEO = "SelectedAction_video";
	public static final String SELECTED_ACTION_IMAGE_OUT = "SelectedActionImage_out";
	public static final String SELECTED_ACTION_IMAGE_2 = "SelectedActionImage_2";
	public static final String SELECTED_ACTION_IMAGE_3 = "SelectedActionImage_3";
	public static final String SELECTED_ACTION_IMAGE_4 = "SelectedActionImage_4";
	
	private static final int UNCONSTRAINED = -1;
	
	public static String startCameraActivityForResult(Activity activity,
			int requestCode) {
		String status = Environment.getExternalStorageState();
		if (!status.equals(Environment.MEDIA_MOUNTED)) {
			Toast.makeText(activity, activity.getResources().getString(R.string.insert_sdcard), Toast.LENGTH_SHORT).show();
			return "";
		}
		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		String imagePath = FileStore.createNewCacheFile(activity);
		File imageFile = new File(imagePath);
		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
		activity.startActivityForResult(cameraIntent, requestCode);

		return imagePath;
	}
	
	public static int readPictureDegree(String path) {
        int degree  = 0;
        try {
                ExifInterface exifInterface = new ExifInterface(path);
                int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                }
        } catch (IOException e) {
                e.printStackTrace();
        }
        return degree;
    }
	

	public static void compressFileAndRotateToBitmapThumb(String filePath,
			int width, int height, int degree) {
		Bitmap bitmap = compressFileToBitmapThumb(filePath, width, height);
		if (bitmap != null) {
			Bitmap b2 = rotateBitmap(bitmap, degree);
			if (b2 != null) {
				writeBitmap(filePath, b2);
			}
			if (bitmap == b2) {
				b2.recycle();
				b2 = null;
			} else {
				bitmap.recycle();
				bitmap = null;
				if (b2 != null) {
					b2.recycle();
					b2 = null;
				}
			}
		}
	}
	
	public static Bitmap rotateBitmap(Bitmap b, int degrees) {
		if (degrees != 0 && b != null) {
			Matrix m = new Matrix();
			m.setRotate(degrees, b.getWidth() / 2, b.getHeight() / 2);
			try {
				Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(),
						b.getHeight(), m, true);
				if (b != b2) {
					b = b2;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
		}
		return b;
	}
	

	public static String thumbnailCompress2File(Context context, Uri photoUri,
			int width, int height) {
		InputStream in = null;
		try {
			in = context.getContentResolver().openInputStream(photoUri);
		} catch (Exception e) {

			e.printStackTrace();
			return "";
		}
		String newFilePath = FileStore.createNewCacheFile(context);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(newFilePath);
			byte[] buffer = new byte[1024 * 64];
			while (true) {
				int iret = in.read(buffer);
				if (iret < 0)
					break;
				fos.write(buffer);
				fos.flush();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		try {
			Bitmap bm = compressFileToBitmapThumb(newFilePath, width, height);
			if (null != bm) {
				bm.recycle();
				bm = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return newFilePath;
	}
	
	public static Bitmap compressFileToBitmapThumb(String filePath, int width,
			int height) {
		if (null == filePath) {
			return null;
		}
		int targetSize = Math.min(width, height);
		int maxPixels = width * height;
		String tmpFilePath = filePath + ".tmp";
		File file = new File(filePath);
		if (!file.exists()) {
			return null;
		}
		new File(tmpFilePath).delete();

		FileInputStream fis = null;
		FileDescriptor fd = null;
		try {
			fis = new FileInputStream(file);
			fd = fis.getFD();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 1;
		options.inJustDecodeBounds = true;
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		try {
			BitmapFactory.decodeFileDescriptor(fd, null, options);
		} catch (Exception e) {
			e.printStackTrace();
			try {
				fis.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return null;
		} catch (Throwable t) {
			t.printStackTrace();
			try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		if (options.mCancel || options.outWidth == -1
				|| options.outHeight == -1) {
			try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		int sampleSize = computeSampleSize(options, targetSize, maxPixels);
		int maxSample = Math.max(sampleSize, 20);
		options.inJustDecodeBounds = false;

		options.inDither = false;
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		// options.inPreferredConfig = Bitmap.Config.RGB_565;

		for (int index = sampleSize; index <= maxSample; index++) {
			try {
				options.inSampleSize = index;
				Bitmap bm = BitmapFactory.decodeFileDescriptor(fd, null,
						options);
				if (null != bm) {
					fis.close();
					writeBitmap(tmpFilePath, bm);
					File tmpFile = new File(tmpFilePath);
					tmpFile.renameTo(file);

					return bm;
				}
			} catch (OutOfMemoryError e) {
			} catch (Exception e) {
			} catch (Throwable t) {
			}
		}
		try {
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	

	public static int computeSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength,
				maxNumOfPixels);

		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}

		return roundedSize;
	}
	
	public static void writeBitmap(String pathName, Bitmap bitmap) {
		if (null == bitmap || null == pathName)
			return;
		boolean bPng = false;
		if (pathName.endsWith(".png")) {
			bPng = true;
		}

		int compressRate = 70;

		File _file = new File(pathName);
		boolean bNew = true;
		if (_file.exists()) {
			bNew = false;
			_file = new File(pathName + ".tmp");
			_file.delete();
		}
		FileOutputStream fos = null;
		boolean bOK = false;
		try {
			fos = new FileOutputStream(_file);
			if (bPng) {
				bitmap.compress(Bitmap.CompressFormat.PNG, compressRate, fos);
			} else {
				bitmap.compress(Bitmap.CompressFormat.JPEG, compressRate, fos);
			}
			bOK = true;
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
					if (bNew == false && bOK) {
						_file.renameTo(new File(pathName));
					}
				} catch (IOException e) {
				}
			}
		}
	}
	

	private static int computeInitialSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;

		int lowerBound = (maxNumOfPixels == UNCONSTRAINED) ? 1 : (int) Math
				.ceil(Math.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == UNCONSTRAINED) ? 128 : (int) Math
				.min(Math.floor(w / minSideLength),
						Math.floor(h / minSideLength));

		if (upperBound < lowerBound) {
			// return the larger one when there is no overlapping zone.
			return lowerBound;
		}

		if ((maxNumOfPixels == UNCONSTRAINED)
				&& (minSideLength == UNCONSTRAINED)) {
			return 1;
		} else if (minSideLength == UNCONSTRAINED) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}
}
