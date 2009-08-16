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
package com.google.code.appsorganizer;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;

/**
 * @author fabio
 * 
 */
public class MockApplication implements Application {

	private final String name;

	public MockApplication(String name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.code.appsorganizer.Application#getIcon()
	 */
	public Drawable getIcon() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.code.appsorganizer.Application#getIconResource()
	 */
	public int getIconResource() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.code.appsorganizer.Application#getId()
	 */
	public Long getId() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.code.appsorganizer.Application#getIntent()
	 */
	public Uri getIntentUri() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.google.code.appsorganizer.Application#getIterable(java.lang.String[])
	 */
	public Iterable<Object> getIterable(String[] cursorColumns) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.code.appsorganizer.Application#getLabel()
	 */
	public String getLabel() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.code.appsorganizer.Application#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.code.appsorganizer.Application#getPackage()
	 */
	public String getPackage() {
		return null;
	}

	public Intent getIntent() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Application another) {
		return 0;
	}

	public byte[] getIconBytes() {
		// TODO Auto-generated method stub
		return null;
	}

}
