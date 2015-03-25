package com.mrfu.photochoice.lib;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Video.VideoColumns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mrfu.photochoice.R;

/**
 * @author MrFu
 * According to File classification albums
 * **/
public class FolderAlbumActivity extends Activity {
	private ListView mLv_folder;
	private Context mContext;
	private FloderAlbumListAdapter mFloderAlbumListAdapter;
	private Cursor mCursor;
	private TextView mEmpty;
	
	// The indices should match the following projections.
	private final int INDEX_BUCKET_ID 		= 0;
	private final int INDEX_BUCKET_NAME 	= 1;
	private final int INDEX_BUCKET_URL 		= 2;
	private static final String[] PROJECTION_VIDEO_BUCKET = {
		VideoColumns.BUCKET_ID,
		VideoColumns.BUCKET_DISPLAY_NAME,
		VideoColumns.DATA,
		VideoColumns.DURATION
	};
	private static final String[] PROJECTION_IMAGE_BUCKET = {
		ImageColumns.BUCKET_ID,
		ImageColumns.BUCKET_DISPLAY_NAME,
		ImageColumns.DATA};
	

	private final static int SCAN_OK = 1;
	private ProgressDialog mProgressDialog;
	private boolean mIsVideo;
	private boolean mIsMultiChoicePhoto;
	private int mMaxMultiChicePhotoCount;
	ArrayList<FolderAlbumBucketEntry> mBucketEntries = new ArrayList<FolderAlbumBucketEntry>();
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case SCAN_OK:
				mProgressDialog.dismiss();
				if (!mIsVideo) {
					String url0 = "";
					if (mBucketEntries.size() > 0) {
						url0 = mBucketEntries.get(0).bucketUrl;
					}
					FolderAlbumBucketEntry entry = new FolderAlbumBucketEntry(0, getResources().getString(R.string.all_picture), url0);
					mBucketEntries.add(0, entry);
				}
				mFloderAlbumListAdapter = new FloderAlbumListAdapter(mContext, mBucketEntries, mIsVideo, mLv_folder);
				mLv_folder.setAdapter(mFloderAlbumListAdapter);
				break;
			default:
				break;
			}
		};
	};
	private String broadcastAction;
	private int mPosition;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.folder_album_activity);
		mContext = this;
		mIsVideo = getIntent().getBooleanExtra("isVideo", true);
		mIsMultiChoicePhoto = getIntent().getBooleanExtra("isMultiChoicePhoto", true);
		mMaxMultiChicePhotoCount = getIntent().getIntExtra("maxMultiChicePhotoCount", 9);
		broadcastAction = getIntent().getStringExtra("broadcastAction");
		mPosition = getIntent().getIntExtra("position", 99);
		View back = findViewById(R.id.title_back);
		back.setVisibility(View.VISIBLE);
		TextView title = (TextView)findViewById(R.id.title_text);
		title.setText(mIsVideo ? R.string.myalbumvideo : R.string.myalbumphoto);
		mEmpty = (TextView)findViewById(R.id.empty);
		mEmpty.setVisibility(View.GONE);
		if (!hasSDCard()) {
			mEmpty.setVisibility(View.VISIBLE);
			return;
		}
		init();
	}
	/**
	 * @return exist:true
	 */
	private boolean hasSDCard() {
		return Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState());
	}

	private void init() {
		mLv_folder = (ListView)findViewById(R.id.lv_folder);
		mProgressDialog = ProgressDialog.show(this, null, getResources().getString(R.string.loading));
		new Thread(new Runnable() {
//			private int mDurationColumnIndex;

			@Override
			public void run() {
				if (mIsVideo) {
					final String orderBy_Video = MediaStore.Video.Media.DATE_TAKEN;
					mCursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, PROJECTION_VIDEO_BUCKET, null, null, orderBy_Video + " DESC");
					if (null != mCursor) {
						try {
//							mDurationColumnIndex = mCursor.getColumnIndex(MediaStore.Video.Media.DURATION);
							while (mCursor.moveToNext()) {
								FolderAlbumBucketEntry entry = new FolderAlbumBucketEntry(mCursor.getInt(INDEX_BUCKET_ID), mCursor.getString(INDEX_BUCKET_NAME), mCursor.getString(INDEX_BUCKET_URL));
								try {
									//More than 26 seconds of the video does not show 
									 //Note: Some of the video, you can not get the video length, returns null , so : try catch
//									if (Long.parseLong(mCursor.getString(mDurationColumnIndex)) < 1000 * 26) {
										if (!mBucketEntries.contains(entry)) {
											mBucketEntries.add(entry);
										}
//									}
								} catch (NumberFormatException e) {
									if (!mBucketEntries.contains(entry)) {
										mBucketEntries.add(entry);
									}
								}
							}
							if (null !=mCursor) {
								if (mCursor.getCount() == 0) {
									Toast.makeText(mContext, getResources().getString(R.string.nofile), Toast.LENGTH_SHORT).show();
									mEmpty.setVisibility(View.VISIBLE);
								}
							}
						}catch (Exception e) {
							e.printStackTrace();
						}finally {
							//Handler scanned images complete notification  
			                mHandler.sendEmptyMessage(SCAN_OK);
							mCursor.close();
						}
					}
				}else { 
					final String orderBy_Image = MediaStore.Images.Media.DATE_TAKEN;
					mCursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, PROJECTION_IMAGE_BUCKET, null, null, orderBy_Image + " DESC");
					if (null != mCursor) {
						try {
							while (mCursor.moveToNext()) {
								FolderAlbumBucketEntry entry = new FolderAlbumBucketEntry(mCursor.getInt(INDEX_BUCKET_ID), mCursor.getString(INDEX_BUCKET_NAME), mCursor.getString(INDEX_BUCKET_URL));
								if (!mBucketEntries.contains(entry)) {
									mBucketEntries.add(entry);
								}
							}
							if (mCursor.getCount() == 0) {
								Toast.makeText(mContext, getResources().getString(R.string.nofile), Toast.LENGTH_SHORT).show();
								mEmpty.setVisibility(View.VISIBLE);
							}
						}catch (Exception e) {
							e.printStackTrace();
						}finally {
							//Handler scanned images complete notification  
							mHandler.sendEmptyMessage(SCAN_OK);
							mCursor.close();
						}
					}
				}
			}
		}).start();

		mLv_folder.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				FolderAlbumBucketEntry bucketEntry = (FolderAlbumBucketEntry)parent.getItemAtPosition(position);
				Intent intent = new Intent(mContext,DetailAlbumActivity.class);
				intent.putExtra("name", bucketEntry.bucketName);
				if (position == 0) {
					intent.putExtra("isSelectAll", true);
				}
				intent.putExtra("isVideo", mIsVideo);
				intent.putExtra("isMultiChoicePhoto", mIsMultiChoicePhoto);
				intent.putExtra("position", mPosition);
				intent.putExtra("maxMultiChicePhotoCount", mMaxMultiChicePhotoCount);
				intent.putExtra("broadcastAction", broadcastAction);
				if (mIsVideo) {
					startActivityForResult(intent, 0);//startActivity(intent);
				}else {
					startActivityForResult(intent, 0);
				}
//				finish();
			}
		});
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode ==RESULT_OK) {
			if (requestCode == 0) {
				finish();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	public FloderAlbumListAdapter getAdapter() {
		if (mFloderAlbumListAdapter != null) {
			return mFloderAlbumListAdapter;
		}
		return null;
	}
	

	public void onBackClick(View view){
		finish();
	}
}
