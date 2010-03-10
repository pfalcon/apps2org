package com.google.code.appsorganizer.prova;

import java.util.ArrayList;

public class DataSource {
	static final int ITEM_COUNT = 100;
	ArrayList<String> items;

	public DataSource() {
		items = new ArrayList<String>(ITEM_COUNT);
		for (int i = 0; i < ITEM_COUNT; ++i) {
			items.add("item" + Integer.toString(i));
		}
	}

	public int getItemCount() {
		return items.size();
	}

	public String getItem(int itemIndex) {
		// It's a slow data source
		try {
			Thread.sleep(500L);
		} catch (InterruptedException ex) {
		}
		return items.get(itemIndex);
	}

}
