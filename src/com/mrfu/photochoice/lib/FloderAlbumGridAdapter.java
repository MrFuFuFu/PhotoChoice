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
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.mrfu.photochoice.R;
import com.mrfu.photochoice.lib.NativeImageLoader.NativeImageCallBack;
import com.squareup.picasso.Picasso;
/***
 * @author MrFu
 */
public class FloderAlbumGridAdapter extends BaseAdapter {

	private int mWidth;
	private Context mContext;
	private ArrayList<FolderAlbumBucketEntry> mBucketEntries;
	private boolean mIsVideo;
	private boolean mIsMultiChoicePhoto;
	private GridView mGridView;
	private Point mPoint = new Point(mWidth/2, mWidth/2);

	/**
	 * 
	 * @param isMultiChoicePhoto 
	 * @param context
	 * @param bucketEntries
	 * @param isVideo yes: Video  false: Image
	 */
	public FloderAlbumGridAdapter(Context context2,
			ArrayList<FolderAlbumBucketEntry> bucketEntries2, boolean isVideo2, boolean isMultiChoicePhoto, GridView gridView) {
		super();
		this.mContext = context2;
		this.mBucketEntries = bucketEntries2;
		this.mIsVideo = isVideo2;
		this.mGridView = gridView;
		this.mIsMultiChoicePhoto = isMultiChoicePhoto;
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
		final ViewHolder holder;
		if (convertView == null) {
			Display display = ((Activity)mContext).getWindowManager().getDefaultDisplay();
			mWidth = display.getWidth();
			convertView =LayoutInflater.from(mContext).inflate(R.layout.folder_album_grid_item, parent, false);
			holder = new ViewHolder();
			holder.imageView = (ImageView)convertView.findViewById(R.id.iv_folder_RowView);
			holder.iv_folder_icon_play = (ImageView)convertView.findViewById(R.id.iv_folder_icon_play);
			holder.iv_folder_select = (ImageView)convertView.findViewById(R.id.iv_folder_select);
			convertView.setTag(holder);
		}else {
			holder = (ViewHolder)convertView.getTag();
		}
		FolderAlbumBucketEntry entry = mBucketEntries.get(position);
		holder.iv_folder_select.setVisibility(View.GONE);
		RelativeLayout.LayoutParams imageParams = (RelativeLayout.LayoutParams) holder.imageView.getLayoutParams();
		imageParams.width  = mWidth/2;
		imageParams.height = mWidth/2;
		holder.imageView.setLayoutParams(imageParams);
		holder.imageView.setImageResource(R.drawable.default_videoimg);
		holder.imageView.setTag(entry.bucketUrl);
		mPoint.set(mWidth/2, mWidth/2);
		if(mIsVideo){
        	holder.iv_folder_icon_play.setVisibility(View.VISIBLE);
        	Bitmap bitmap = NativeImageLoader.getInstance().loadNativeImage(true, entry.bucketUrl, mPoint, new NativeImageCallBack() {
				@Override
				public void onImageLoader(Bitmap bitmap, String path) {
					ImageView mImageView = (ImageView) mGridView.findViewWithTag(path);  
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
			
		}else if(mIsMultiChoicePhoto){//Select more than one picture
			holder.iv_folder_select.setVisibility(View.INVISIBLE);
			holder.iv_folder_icon_play.setVisibility(View.GONE);
			Picasso.with(mContext).load(new File(entry.bucketUrl))
			.resize(mWidth/3, mWidth/3).centerCrop().placeholder(R.drawable.default_videoimg).into(holder.imageView);
        	if (entry.status) {
				holder.iv_folder_select.setVisibility(View.VISIBLE);
			}else {
				holder.iv_folder_select.setVisibility(View.INVISIBLE);
			}
		}else {//single select picture
			holder.iv_folder_select.setVisibility(View.INVISIBLE);
			holder.iv_folder_icon_play.setVisibility(View.GONE);
			Picasso.with(mContext).load(new File(entry.bucketUrl))
			.resize(mWidth/2, mWidth/2).centerCrop().placeholder(R.drawable.default_videoimg).into(holder.imageView);
		}
		return convertView;
	}

	class ViewHolder {
		ImageView imageView;
		ImageView iv_folder_icon_play;
		ImageView iv_folder_select;
	}

}
