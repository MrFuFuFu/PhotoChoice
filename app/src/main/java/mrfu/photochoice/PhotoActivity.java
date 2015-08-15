package mrfu.photochoice;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import mrfu.photochoice.lib.FolderAlbumActivity;
import mrfu.photochoice.lib.SquareImageView;
import mrfu.photochoice.lib.Utils;

/**
 * @author Mr.Fu
 * 2015-3-25 10:24:46
 */
public class PhotoActivity extends Activity{
	private String imagePath;
	private String path;
	ArrayList<ImageModel> mImageArrayList = new ArrayList<ImageModel>();
	private GridView mGridView;
	private GridImageAdapter mAdapter;
	/**When you select a single photo of the location in gridview**/
	int cameraPosition = 88;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photo);
        View back = findViewById(R.id.title_back);
        back.setVisibility(View.VISIBLE);
        TextView title = (TextView)findViewById(R.id.title_text);
        title.setText(R.string.choice);

		IntentFilter intentFilter = new IntentFilter(Utils.SELECTED_ACTION_IMAGE_2);
		registerReceiver(broadcastReceiver, intentFilter);
		
		mGridView = (GridView)findViewById(R.id.grid);
		
		initData();
	}

    private void initTitleBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setLogo(R.mipmap.logo);
        toolbar.inflateMenu(R.menu.menu_main);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
               @Override
               public boolean onMenuItemClick(MenuItem item) {
                   Uri uri = Uri.parse("https://github.com/MrFuFuFu");
                   Intent i = new Intent(Intent.ACTION_VIEW, uri);
                   i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                   startActivity(i);
                   return false;
               }
           }
        );
    }

	private void initData() {
		ArrayList<String> selectedImageItems = getIntent().getStringArrayListExtra("selectedImageItems");
		path = getIntent().getStringExtra("path");
		if (!TextUtils.isEmpty(path)) {//from camera
			ImageModel model = new ImageModel(path, false);
			mImageArrayList.add(model);
		}else if (selectedImageItems != null && selectedImageItems.size() > 0) {//from albums
			int size = selectedImageItems.size();
			for (int i = 0; i < size; i++) {
				String path = Utils.thumbnailCompress2File(this,Uri.fromFile(new File(selectedImageItems.get(i))),getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels);
				ImageModel model2 = new ImageModel(path, false);
				mImageArrayList.add(model2);
			}
		}
		mAdapter = new GridImageAdapter(mImageArrayList, this);
		mGridView.setAdapter(mAdapter);
		aboutUpload(mImageArrayList, 0);
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if ((position == mAdapter.getList().size()) && mAdapter.getList().size() < 9) {//click icon_add
					showImageDialog(99);
				}else {
					showImageDialog(position);//Click on the photo of the location has been selected 
				}
				
			}
		});
	}


	BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			ArrayList<String> selectedImageItems = intent.getStringArrayListExtra("selectedImageItems");
			ArrayList<ImageModel> imageModels = new ArrayList<ImageModel>();
			for (int i = 0; i < selectedImageItems.size(); i++) {
				String path = Utils.thumbnailCompress2File(PhotoActivity.this,Uri.fromFile(new File(selectedImageItems.get(i))),getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels);
				ImageModel model = new ImageModel(path, false);
				imageModels.add(model);
			}
			int position = intent.getIntExtra("position", 99);
			if (position == 99) {//click the icon_add to get the picture
				int last_DataSize = mImageArrayList.size();
				mAdapter.append(imageModels);
				aboutUpload(imageModels, last_DataSize);
			}else {//click to specify the location of the picture get the picture
				mAdapter.setIndexList(imageModels.get(0), position);
				aboutUpload(imageModels, position);
			}
		}
	};

	/**
	 * here you can do to upload images to the server's operating
	 * ...after uploading to remember to set the mAdapter.setProgress(index, true);
	 * @param imageArrayList 
	 */
	private void aboutUpload(List<ImageModel> imageArrayList, final int index){
		for (int i = 0; i < imageArrayList.size(); i++) {
			//upload image to server
//			uploadImage(imageArrayList.get(i).imagePath, index ++);
			mAdapter.setProgress(index, false);
		}
	}
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(broadcastReceiver);
		super.onDestroy();
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void showImageDialog(final int position) {
		final AlertDialog.Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
		dialog.setItems(R.array.array_image,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (which == 0) {// albums
							Intent intent2 = new Intent(PhotoActivity.this, FolderAlbumActivity.class);
							intent2.putExtra("isVideo", false);
							if (position == 99) {//99  here means not specify the location of choice
								intent2.putExtra("isMultiChoicePhoto", true);
								intent2.putExtra("maxMultiChicePhotoCount", 9 - mAdapter.getList().size());
							}else {
								intent2.putExtra("isMultiChoicePhoto", false);
								intent2.putExtra("position", position);
							}
							intent2.putExtra("broadcastAction", Utils.SELECTED_ACTION_IMAGE_2);
							startActivity(intent2);
						} else if (which == 1) {//open camera
							try {
								cameraPosition = position;
								imagePath = Utils.startCameraActivityForResult(PhotoActivity.this, 1);
							} catch (Exception e) {
								Toast.makeText(PhotoActivity.this,"open camera error~", Toast.LENGTH_SHORT).show();
							}
						} else {
							dialog.dismiss();
						}
					}
				});
		dialog.show();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == RESULT_OK){
			if(requestCode == 0){
				Uri uri = data.getData();
				if(uri != null){
					imagePath = Utils.thumbnailCompress2File(this,uri,getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels);
				}
				
			}else if(requestCode == 1){
				//Crop pictures, rotate pictures
				int degree = Utils.readPictureDegree(imagePath);
				if(degree != 0){
					Utils.compressFileAndRotateToBitmapThumb(imagePath, getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels, degree);
				}else{
					Utils.compressFileToBitmapThumb(imagePath, getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels);
				}
				
				List<ImageModel> imageModels = new ArrayList<ImageModel>();
				ImageModel model = new ImageModel(imagePath, false);
				imageModels.add(model);
				int index_DataSize = 0;
				if (cameraPosition == 88 || cameraPosition == 99) {
					index_DataSize = mImageArrayList.size();
					mAdapter.append(imageModels);
				}else {
					index_DataSize = cameraPosition;
					mAdapter.setIndexList(imageModels.get(0), cameraPosition);
					cameraPosition = 88;
				}
				aboutUpload(imageModels, index_DataSize);
				
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}


	private class GridImageAdapter extends BaseAdapter{
		private List<ImageModel> list;
		private Context mContext;
		
		public GridImageAdapter(List<ImageModel> mImageArrayList, Context mContext) {
			this.list = mImageArrayList;
			this.mContext = mContext;
		}
		
		public void setProgress(int position, boolean isVisible){
			if (list.size() >= position && list.get(position) != null) {
				list.get(position).isFinish = isVisible;
				notifyDataSetChanged();
			}
		}
		
//		public void setList(List<ImageModel> list){
//			this.list = list;
//			this.notifyDataSetChanged();
//		}
		
		public void append(List<ImageModel> list){
			this.list.addAll(list);
			this.notifyDataSetChanged();
		}
		
		public void setIndexList(ImageModel model, int index){
			if (list.get(index) != null) {
				list.get(index).imagePath = model.imagePath;
				list.get(index).isFinish = model.isFinish;
			}
		}
		
		public List<ImageModel> getList(){
			return this.list;
		}

		@Override
		public int getCount() {
			return list.size() + 1;//for the last icon_add 
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView != null) {
				holder = (ViewHolder)convertView.getTag();
			}else {
				convertView = View.inflate(mContext, R.layout.grid_imageview, null);
				holder = new ViewHolder();
				holder.imageView = (SquareImageView)convertView.findViewById(R.id.imageview);
				holder.progress = (ProgressBar)convertView.findViewById(R.id.progress);
				convertView.setTag(holder);
			}
			holder.imageView.setVisibility(View.VISIBLE);
			if (position == list.size()) {
				holder.imageView.setImageResource(R.mipmap.icon_add);
				holder.progress.setVisibility(View.GONE);
			}else {
				holder.imageView.setImageBitmap(BitmapFactory.decodeFile(list.get(position).imagePath));
				holder.progress.setVisibility(list.get(position).isFinish ? View.GONE : View.VISIBLE);
			}
			if (position == 9) {
				holder.imageView.setVisibility(View.GONE);
			}
			return convertView;
		}
		
		private class ViewHolder{
			SquareImageView imageView;
			ProgressBar progress;
		}
		
	}

	private class ImageModel{
		public String imagePath;
		public boolean isFinish;
		public ImageModel(String imagePath, boolean isFinish) {
			this.imagePath = imagePath;
			this.isFinish = isFinish;
		}
	}
	

	public void onBackClick(View view){
		finish();
	}
}
