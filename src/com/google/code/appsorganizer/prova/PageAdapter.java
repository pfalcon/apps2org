package com.google.code.appsorganizer.prova;

import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.code.appsorganizer.R;

public class PageAdapter extends BaseAdapter {

	private final Context context;
	private final int itemRowResID;
	private final int loadingRowResID;
	int itemsLoaded;
	int itemsToLoad;
	boolean allItemsLoaded;
	Boolean loading;
	UIUpdateTask updateTask;
	static final int PRELOAD_ITEMS = 30;
	static final String LOG_TAG = "PAGEADAPTER";

	DataSource dataSource;
	ArrayList<String> items;
	Handler uiHandler = new Handler();

	public PageAdapter(Context context, int itemRowResID, int loadingRowResID) {
		this.context = context;
		this.itemRowResID = itemRowResID;
		this.loadingRowResID = loadingRowResID;
		dataSource = new DataSource();
		itemsLoaded = 0;
		itemsToLoad = 0;
		items = new ArrayList<String>();
		allItemsLoaded = false;
		loading = Boolean.FALSE;
		updateTask = new UIUpdateTask();
	}

	public int getCount() {
		int count = itemsLoaded;
		if (!allItemsLoaded) {
			++count;
		}
		return count;
	}

	public Object getItem(int position) {
		String result;
		synchronized (items) {
			result = items.get(position);
		}
		return result;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		boolean isLastRow = position >= itemsLoaded;
		int rowResID = isLastRow ? loadingRowResID : itemRowResID;
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(rowResID, parent, false);
		if (isLastRow) {
			if (position < dataSource.getItemCount()) {
				// Should there be more items loaded?
				int nextItemToLoad = position + PRELOAD_ITEMS;
				int allItemsToLoad = dataSource.getItemCount();
				if (nextItemToLoad > allItemsToLoad) {
					nextItemToLoad = allItemsToLoad;
				}
				Log.d(LOG_TAG, "nextItemToLoad: " + nextItemToLoad);
				if (nextItemToLoad > itemsToLoad) {
					itemsToLoad = nextItemToLoad;
					Log.d(LOG_TAG, "itemsToLoad: " + itemsToLoad);
					// Launch the loading thread if it is not currently running
					synchronized (loading) {
						if (!loading.booleanValue()) {
							Log.d(LOG_TAG, "Staring loading task");
							loading = Boolean.TRUE;
							Thread t = new LoadingThread();
							t.start();
							Log.d(LOG_TAG, "Loading task started");
						}
					}
				}
			} else {
				uiHandler.post(updateTask);
			}
		} else {
			String item = items.get(position);
			TextView itemControl = (TextView) v.findViewById(R.id.name);
			if (itemControl != null) {
				itemControl.setText(item);
			}
		}
		return v;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	@Override
	public boolean isEnabled(int position) {
		return true;
	}

	class LoadingThread extends Thread {
		@Override
		public void run() {
			int itemsOriginallyLoaded = 0;
			synchronized (items) {
				itemsOriginallyLoaded = items.size();
			}
			for (int i = itemsOriginallyLoaded; i < itemsToLoad; ++i) {
				Log.d(LOG_TAG, "Loading item #" + i);
				String item = dataSource.getItem(i);
				synchronized (items) {
					items.add(item);
				}
				itemsLoaded = i + 1;
				uiHandler.post(updateTask);
				Log.d(LOG_TAG, "Published item #" + i);
			}
			if (itemsLoaded >= (dataSource.getItemCount() - 1)) {
				allItemsLoaded = true;
			}
			synchronized (loading) {
				loading = Boolean.FALSE;
			}
		}
	}

	class UIUpdateTask implements Runnable {
		public void run() {
			Log.d(LOG_TAG, "Publishing progress");
			notifyDataSetChanged();
		}
	}
}
