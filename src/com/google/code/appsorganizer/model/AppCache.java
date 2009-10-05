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

import com.google.code.appsorganizer.db.ObjectWithId;

public class AppCache extends ObjectWithId {

	public final String packageName;
	public final String name;
	public final String label;
	public boolean starred;
	public byte[] image;
	public boolean disabled;

	public AppCache(String packageName, String name, String label) {
		this.packageName = packageName;
		this.name = name;
		this.label = label;
	}
}
