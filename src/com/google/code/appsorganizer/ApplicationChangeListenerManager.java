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

import java.util.ArrayList;

import com.google.code.appsorganizer.db.DbChangeListener;

/**
 * @author fabio
 * 
 */
public class ApplicationChangeListenerManager {

	private ApplicationChangeListenerManager() {
	}

	private static final ArrayList<DbChangeListener> listeners = new ArrayList<DbChangeListener>();

	public static boolean addListener(DbChangeListener object) {
		return listeners.add(object);
	}

	public static boolean removeListener(DbChangeListener object) {
		return listeners.remove(object);
	}

	public static void notifyDataSetChanged(Object source, short type) {
		for (DbChangeListener a : listeners) {
			a.dataSetChanged(source, type);
		}
	}

	public static void notifyDataSetChanged(Object source) {
		notifyDataSetChanged(source, DbChangeListener.CHANGED_ALL);
	}
}
