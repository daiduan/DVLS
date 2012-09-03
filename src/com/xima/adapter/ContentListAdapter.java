package com.xima.adapter;

import java.util.List;

import com.xima.datadef.Item;
import com.xima.ui.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ContentListAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private List<Item> itemList;

	public ContentListAdapter(Context context, List<Item> itemList) {
		this.itemList = itemList;
		this.inflater = LayoutInflater.from(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getCount()
	 */
	
	public int getCount() {
		return itemList.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getItem(int)
	 */
	
	public Object getItem(int arg0) {
		return itemList.get(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getItemId(int)
	 */
	
	public long getItemId(int position) {
		return position;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getView(int, android.view.View,
	 * android.view.ViewGroup)
	 */
	
	public View getView(int position, View convertView, ViewGroup parent) {
		Item item = itemList.get(position);
		View v = null;

//		if ("object.item.audioItem.musicTrack"
//				.equals(item.getObjectClass())) {
//			v = inflater.inflate(R.layout.audio_item, null);
//			TextView artist = (TextView) v.findViewById(R.id.artist);
//			artist.setText(item.getArtist());
//			TextView album = (TextView) v.findViewById(R.id.album);
//			album.setText(item.getAlbum());
//
//			ImageView addToPlaylist = (ImageView) v
//					.findViewById(R.id.addToPlaylist);
//			addToPlaylist.setId(item.getId());
//			if (PlaylistHelper.getInstance().getPlayList().contains(item)) {
//				addToPlaylist
//						.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
//			}
//			addToPlaylist.setOnClickListener(new OnClickListener() {
//
//				
//				public void onClick(View arg0) {
//
//					ImageView v = (ImageView) arg0;
//
//					Item i = new Item(arg0.getId(), null, null, null, null);
//
//					if (!PlaylistHelper.getInstance().getPlayListIds()
//							.contains(arg0.getId())) {
//						int indexOf = dlnaService.getCurrentLevelItems()
//								.indexOf(i);
//						if (indexOf != -1) {
//							PlaylistHelper.getInstance().addItem(
//									dlnaService.getCurrentLevelItems().get(
//											indexOf));
//							v.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
//						}
//					} else {
//						PlaylistHelper.getInstance().removeItem(i);
//						v.setImageResource(android.R.drawable.ic_menu_add);
//					}
//
//				}
//			});
//		} else 
			if ("object.item.videoItem".equals(item.getObjectClass())) {
			v = inflater.inflate(R.layout.video_item, null);
		} else if ("object.item.imageItem".equals(item.getObjectClass())) {
			v = inflater.inflate(R.layout.picture_item, null);
		} else if ("object.container.album.musicAlbum".equals(item
				.getObjectClass())) {
			v = inflater.inflate(R.layout.album_item, null);
		} else if (item.getObjectClass()!=null && item.getObjectClass().startsWith("object.container")) {
			v = inflater.inflate(R.layout.container_item, null);
		} else {
			System.out.println(item.getObjectClass());
			v = inflater.inflate(R.layout.list_item, null);
		}

		TextView title = (TextView) v.findViewById(R.id.title);
		if (item != null && title != null) {
			title.setText(item.getTitle());
		}

		return v;
	}

}
