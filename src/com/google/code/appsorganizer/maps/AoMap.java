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
package com.google.code.appsorganizer.maps;

import java.util.Arrays;

/**
 * @author fabio
 * 
 */
public abstract class AoMap<K extends Comparable<K>, V> {

	private final K[] keys;
	private final V[] values;

	public AoMap(V[] data) {
		keys = createKeyArray(data.length);
		values = data;
		for (int i = 0; i < data.length; i++) {
			keys[i] = createKey(data[i]);
		}
	}

	protected abstract K createKey(V v);

	protected abstract K[] createKeyArray(int length);

	public V get(K key) {
		int i = Arrays.binarySearch(keys, key);
		if (i < 0) {
			return null;
		} else {
			return values[i];
		}
	}

	public V getAt(int i) {
		if (i < 0) {
			return null;
		} else {
			return values[i];
		}
	}

	public int getPosition(K key) {
		return Arrays.binarySearch(keys, key);
	}

	public K[] keys() {
		return keys;
	}

	public V[] values() {
		return values;
	}

	public int size() {
		return keys.length;
	}
}
