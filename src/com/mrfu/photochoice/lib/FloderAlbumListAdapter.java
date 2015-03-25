package com.mrfu.photochoice.lib;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mrfu.photochoice.R;
import com.mrfu.photochoice.lib.NativeImageLoader.NativeImageCallBack;
import com.squareup.picasso.Picasso;
/***
 * @author MrFu
 */
public class FloderAlbumListAdapter extends BaseAdapter {

	private int mWidth;
	private Context mContext;
	private ArrayList<FolderAlbumBucketEntry> mBucketEntries;
	private boolean mIsVideo;
	private ListView mListView;
	private Point mPoint = new Point(mWidth/2, mWidth/2);
	
	/**
	 * 
	 * @param context
	 * @param bucketEntries
	 * @param isVideo yes: Video  false: Image
	 */
	public FloderAlbumListAdapter(Context context2,
			ArrayList<FolderAlbumBucketEntry> bucketEntries2, boolean isVideo2, ListView listView) {
		super();
		this.mContext = context2;
		this.mBucketEntries = bucketEntries2;
		this.mIsVideo = isVideo2;
		this.mListView = listView;
	}
	
	
	@Override
	public int getCount() {
		return mBucketEntries.size();
	}

	@Override
	public Object getItem(int position) {
		return mBucketEntries.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressWarnings("deprecation")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			Display display = ((Activity)mContext).getWindowManager().getDefaultDisplay();
			mWidth = display.getWidth();
			convertView =LayoutInflater.from(mContext).inflate(R.layout.folder_album_list_item, parent, false);
			holder = new ViewHolder();
			holder.imageView = (ImageView)convertView.findViewById(R.id.iv_folder_RowView1);
			holder.nameTextView = (TextView)convertView.findViewById(R.id.tv_folder_RowView1);
			holder.iv_folder_icon_play1 = (ImageView)convertView.findViewById(R.id.iv_folder_icon_play1);
			convertView.setTag(holder);
		}else {
			holder = (ViewHolder)convertView.getTag();
		}
		RelativeLayout.LayoutParams imageParams = (RelativeLayout.LayoutParams) holder.imageView.getLayoutParams();
		imageParams.width  = mWidth/3;
		imageParams.height = mWidth/3;
		holder.imageView.setLayoutParams(imageParams);
		holder.imageView.setImageResource(R.drawable.default_videoimg);
		holder.imageView.setTag(mBucketEntries.get(position).bucketUrl);
		mPoint.set(mWidth/2, mWidth/2);
		if(mIsVideo){
			holder.iv_folder_icon_play1.setVisibility(View.VISIBLE);
			Bitmap bitmap = NativeImageLoader.getInstance().loadNativeImage(true, mBucketEntries.get(position).bucketUrl, mPoint, new NativeImageCallBack() {
				@Override
				public void onImageLoader(Bitmap bitmap, String path) {
					ImageView mImageView = (ImageView) mListView.findViewWithTag(path);  
	                if(bitmap != null && mImageView != null){  
	                    mImageView.setImageBitmap(bitmap);  
	                }  
				}
			});
        	if (bitmap != null) {
				holder.imageView.setImageBitmap(bitmap);
			}else {
				holder.imageView.setImageResource(R.drawable.default_videoimg);
			}
			
			
		}else{
			Picasso.with(mContext).load(new File(mBucketEntries.get(position).bucketUrl))
			.resize(mWidth/3, mWidth/3).centerCrop().placeholder(R.drawable.default_videoimg).into(holder.imageView);
        	holder.iv_folder_icon_play1.setVisibility(View.GONE);
		}
		
		holder.nameTextView.setText(mBucketEntries.get(position).bucketName );
		
		return convertView;
	}
	
	class ViewHolder {
		ImageView imageView;
		TextView nameTextView;
		ImageView iv_folder_icon_play1;
	}

}
