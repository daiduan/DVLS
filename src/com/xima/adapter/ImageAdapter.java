package com.xima.adapter;

import com.xima.datadef.CommandID;
import com.xima.ui.R;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter implements CommandID {
	private Context mContext;

	public ImageAdapter(Context c) {
		mContext = c;
	}

	public int getCount() {
		// TODO Auto-generated method stub
		return mThumbIds.length;
	}

	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	// create a new ImageView for each item referenced by the Adapter
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ImageView imageView;
		if (convertView == null) { // if it's not recycled, initialize some
									// attributes
			imageView = new ImageView(mContext);
			//设置每个图像的大小
			imageView.setLayoutParams(new GridView.LayoutParams(35, 35));
			//缩放方式
			imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			//图像边界
			imageView.setPadding(0,0,0,0);
			imageView.setBackgroundResource(R.drawable.bg);
		} else {
			imageView = (ImageView) convertView;
		}

		imageView.setImageResource(mThumbIds[position]);
		return imageView;
	}

}
