package com.google.code.appsorganizer;

import android.graphics.drawable.Drawable;
import android.net.Uri;

public interface Application extends Comparable<Application> {

	Long getId();

	String getName();

	String getPackage();

	int getIconResource();

	Uri getIntent();

	Drawable getIcon();

	Iterable<Object> getIterable(String[] cursorColumns);
}
