package com.mrfu.photochoice;


import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;

import com.mrfu.photochoice.lib.FolderAlbumActivity;
import com.mrfu.photochoice.lib.Utils;

/***
 * @author MrFu
 */
public class MainActivity extends Activity {
	private String imagePath;
	private CheckBox checkbox;

	BroadcastReceiver broadcastReceiver_Photo = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			ArrayList<String> selectedImageItems = intent.getStringArrayListExtra("selectedImageItems");
			Intent intent2 =new Intent(MainActivity.this,PhotoActivity.class);
			intent2.putExtra("selectedImageItems", selectedImageItems);
			startActivityForResult(intent2, 2);
		}
	};
	

	BroadcastReceiver broadcastReceiver_Video = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String selectItem = "";
			selectItem = intent.getStringExtra("selectItem");
			Log.i("BroadcastReceiver", "selectItem: " + selectItem);
			
			File file = new File(selectItem); 
			if (file.exists()) {
				//get file.path
				if (file.getPath().contains("mp4") || file.getPath().contains("wmv") || file.getPath().contains("avi") || file.getPath().contains("3gp")) {
					Uri uri = Uri.fromFile(file);
					Log.i("BroadcastReceiver:uri", "uri:" + uri.getPath());
					Toast.makeText(MainActivity.this, "uri:" + uri.getPath(), Toast.LENGTH_LONG).show();
				}
			}
		}
	};

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		IntentFilter intentFilter = new IntentFilter(Utils.SELECTED_ACTION_IMAGE_OUT);//photo
		registerReceiver(broadcastReceiver_Photo, intentFilter);
		IntentFilter intentFilter2 = new IntentFilter(Utils.SELECTED_ACTION_VIDEO);//video
		registerReceiver(broadcastReceiver_Video, intentFilter2);
		
		checkbox = (CheckBox)findViewById(R.id.checkbox);
		checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				((Button)findViewById(R.id.button)).setText(isChecked ? "click choice video" : "click choice photo");
			}
		});
		
		((Button)findViewById(R.id.button)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (checkbox.isChecked()) {
					Intent intent = new Intent(MainActivity.this, FolderAlbumActivity.class);
					intent.putExtra("isVideo", true);
					startActivity(intent);
				}else {
					Intent intent2 = new Intent(MainActivity.this, FolderAlbumActivity.class);
					intent2.putExtra("isVideo", false);
					intent2.putExtra("isMultiChoicePhoto", true);
					intent2.putExtra("maxMultiChicePhotoCount", 9);
					intent2.putExtra("broadcastAction", Utils.SELECTED_ACTION_IMAGE_OUT);
					startActivity(intent2);
				}
			}
		});
		((Button)findViewById(R.id.button2)).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!checkbox.isChecked()) {
					try {
						imagePath = Utils.startCameraActivityForResult(MainActivity.this, 1);
					} catch (Exception e) {
						Toast.makeText(MainActivity.this, "open camera error", Toast.LENGTH_SHORT).show();
					}
				}else {
					Toast.makeText(MainActivity.this, "checkbox need no chooice", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == 1) {
				int degree = Utils.readPictureDegree(imagePath);
				if(degree != 0){
					Utils.compressFileAndRotateToBitmapThumb(imagePath, getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels, degree);
				}else{
					Utils.compressFileToBitmapThumb(imagePath, getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels);
				}
				
				Intent intent = new Intent(this,PhotoActivity.class);
				intent.putExtra("path", imagePath);
				startActivityForResult(intent, 2);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	protected void onDestroy() {
		unregisterReceiver(broadcastReceiver_Photo);
		unregisterReceiver(broadcastReceiver_Video);
		super.onDestroy();
	};
}
