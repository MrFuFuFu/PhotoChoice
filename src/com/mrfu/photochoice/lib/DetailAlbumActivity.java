package com.mrfu.photochoice.lib;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.mrfu.photochoice.R;

/***
 * @author MrFu
 */
public class DetailAlbumActivity extends Activity {
	private FloderAlbumGridAdapter mFloderGridAdapter;
	private GridView mGv_videoorimage;
	private Context mContext;
	private Cursor mCursor;
	private int mDataColumnIndex;
	@SuppressWarnings("unused")
	private int mDurationColumnIndex;
	private ArrayList<FolderAlbumBucketEntry> mBucketEntries = new ArrayList<FolderAlbumBucketEntry>();
	/**The selected item**/
	private String mSelectItem;
	private ArrayList<String> mSelectedImageItems = new ArrayList<String>();
	private boolean mIsVideo;
	private boolean mIsMultiChoicePhoto;
	private int mMaxMultiChicePhotoCount;
	
	private final static int SCAN_OK = 1;
	private ProgressDialog mProgressDialog;
	String nameString;
	Button title_button;
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case SCAN_OK:
				mProgressDialog.dismiss();
				mFloderGridAdapter = new FloderAlbumGridAdapter(mContext, mBucketEntries, mIsVideo, mIsMultiChoicePhoto, mGv_videoorimage);
				mGv_videoorimage.setAdapter(mFloderGridAdapter);
				break;
			default:
				break;
			}
		};
	};
	private String broadcastAction;
	private boolean isSelectAll;
	private int mPosition;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail_album_activity);
		mContext = this;
		mIsVideo = getIntent().getBooleanExtra("isVideo", true);
		mIsMultiChoicePhoto = getIntent().getBooleanExtra("isMultiChoicePhoto", true);
		mMaxMultiChicePhotoCount = getIntent().getIntExtra("maxMultiChicePhotoCount", 9);
		broadcastAction = getIntent().getStringExtra("broadcastAction");
		mPosition = getIntent().getIntExtra("position", 99);
		View back = findViewById(R.id.title_back);
		back.setVisibility(View.VISIBLE);
		TextView title = (TextView)findViewById(R.id.title_text);
		if (getIntent() != null) {
			nameString = getIntent().getStringExtra("name");
			isSelectAll = getIntent().getBooleanExtra("isSelectAll", false);
			if (null != nameString) {
				title.setText(nameString);
			}else {
				title.setText("");
			}
			
		}else {
			title.setText(R.string.myalbumvideo);
		}
		
		if (!mIsVideo && mIsMultiChoicePhoto) {
			title_button = (Button)findViewById(R.id.title_button);
			title_button.setVisibility(View.VISIBLE);
			title_button.setText("Choice");
		}
		
		init();
	}

	private void init() {
		mGv_videoorimage = (GridView)findViewById(R.id.gv_videoorimage);
		mProgressDialog = ProgressDialog.show(this, null, getResources().getString(R.string.loading));
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if (mIsVideo) {
						initVideos(nameString);
					}else if(mIsVideo){
						initVideos();
					}
					if (!mIsVideo && !isSelectAll) {
						initPhoneImages(nameString);
					}else if(!mIsVideo && isSelectAll){
						initPhoneImages();
					}
					if (null != mCursor) {
						int count = mCursor.getCount();
						if (count > 0) {
							if (mIsVideo) {
								mDataColumnIndex = mCursor.getColumnIndex(MediaStore.Video.Media.DATA);
								mDurationColumnIndex = mCursor.getColumnIndex(MediaStore.Video.Media.DURATION);
							}else {
								mDataColumnIndex = mCursor.getColumnIndex(MediaStore.Images.Media.DATA);
							}
							//move position to first element
							mCursor.moveToFirst();
							for (int i = 0; i < count; i++) {
								mCursor.moveToPosition(i);
								if (mIsVideo) {
									if (null != mCursor) {
										try {//More than 26 seconds of the video does not show 
											 //Note: Some of the video, you can not get the video length, returns null , so : try catch
//											if (Long.parseLong(mCursor.getString(mDurationColumnIndex)) < 1000 * 26) {
												mBucketEntries.add(new FolderAlbumBucketEntry(mCursor.getString(mDataColumnIndex)));
//											}
										} catch (NumberFormatException e) {
											mBucketEntries.add(new FolderAlbumBucketEntry(mCursor.getString(mDataColumnIndex)));
										}
									}
								}else {
									mBucketEntries.add(new FolderAlbumBucketEntry(mCursor.getString(mDataColumnIndex)));
								}
								
							}
						}else{
							Toast.makeText(mContext, getResources().getString(R.string.nofile), Toast.LENGTH_SHORT).show();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}finally{
					//Handler scanned images complete notification  
	                mHandler.sendEmptyMessage(SCAN_OK);
					mCursor.close();
				}
			}
		}).start();


		mGv_videoorimage.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				FloderAlbumGridAdapter adapter = (FloderAlbumGridAdapter)parent.getAdapter();
				FolderAlbumBucketEntry entry = (FolderAlbumBucketEntry)adapter.getItem(position);
				if (mIsVideo) {
					mSelectItem = entry.bucketUrl;
					Intent intent = new Intent();
					intent.setAction(Utils.SELECTED_ACTION_VIDEO);
					intent.putExtra("selectItem", mSelectItem);
					sendBroadcast(intent);
					setResult(RESULT_OK);
					finish();
				}else if(mIsMultiChoicePhoto){//Select more than one picture
					if (!entry.status && mSelectedImageItems.size() >= mMaxMultiChicePhotoCount) {
						Toast.makeText(mContext, 
								getResources().getString(R.string.max_picture) + mMaxMultiChicePhotoCount + getResources().getString(R.string.picture), 
								Toast.LENGTH_SHORT).show();
						return;
					}
					entry.status = !entry.status;
					adapter.notifyDataSetChanged();
					
					if (entry.status) {
						mSelectedImageItems.add(entry.bucketUrl);
					}else {
						mSelectedImageItems.remove(entry.bucketUrl);
					}
					if (title_button != null) {
						title_button.setText("("+mSelectedImageItems.size()+")选择");
					}
				}else {//single select picture
					mSelectedImageItems.add(entry.bucketUrl);
					onRightClick(null);
				}
			}
		});
		
		mGv_videoorimage.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				if (!mIsVideo) {
					FloderAlbumGridAdapter adapter = (FloderAlbumGridAdapter) parent.getAdapter();
					FolderAlbumBucketEntry entry = (FolderAlbumBucketEntry) adapter.getItem(position);
					File file = new File(entry.bucketUrl);
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setDataAndType(Uri.fromFile(file), "image/*");
					startActivity(intent);
					return true;
				}
				return true;

			}
		});
	}

	private void initVideos(String bucketName) {
		final String orderBy_Video = MediaStore.Video.Media.DATE_TAKEN;
		String searchParams = "bucket_display_name = \"" + bucketName + "\"";
		final String[] columns = { MediaStore.Video.Media.DATA, MediaStore.Video.Media._ID, MediaStore.Video.Media.DURATION};//MediaStore.Video.Media.DATA???
		mCursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, columns, searchParams, null, orderBy_Video + " DESC");
	}
	private void initVideos() {
		final String orderBy_Video = MediaStore.Video.Media.DATE_TAKEN;
		//Here we set up a string array of the thumbnail ID column we want to get back
		String [] proj = {MediaStore.Video.Media.DATA,MediaStore.Video.Media._ID, MediaStore.Video.Media.DURATION};
		mCursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, proj, null,null, orderBy_Video + " DESC");
	}

	private void initPhoneImages(String bucketName) {
		final String orderBy_Image = MediaStore.Images.Media.DATE_TAKEN;
		String searchParams = "bucket_display_name = \"" + bucketName + "\"";
		final String[] columns = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
		mCursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, searchParams, null, orderBy_Image + " DESC");
	}

	private void initPhoneImages() {
		final String orderBy_Image = MediaStore.Images.Media.DATE_TAKEN;
		final String[] columns = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
		mCursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy_Image + " DESC");
	}
	
	public FloderAlbumGridAdapter getAdapter() {
		if (mFloderGridAdapter != null) {
			return mFloderGridAdapter;
		}
		return null;
	}
	
	public void onRightClick(View view){
		Intent intent = new Intent();
		intent.setAction(broadcastAction);
		intent.putStringArrayListExtra("selectedImageItems", mSelectedImageItems);
		intent.putExtra("position", mPosition);
		sendBroadcast(intent);
		setResult(RESULT_OK);
		finish();
	}
}
