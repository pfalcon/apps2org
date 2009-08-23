/*
 * Copyright (C) 2009 Apps Organizer
 *
 * This file is part of Apps Organizer
 *
 * Apps Organizer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Apps Organizer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Apps Organizer.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.google.code.appsorganizer.model;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;

public interface Application extends Comparable<Application>, GridObject {

	Long getId();

	String getName();

	Drawable getIcon();

	String getPackage();

	int getIconResource();

	Intent getIntent();

	Uri getIntentUri();

	byte[] getIconBytes();

	Iterable<Object> getIterable(String[] cursorColumns);
}
