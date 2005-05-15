/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jnode.vm.VmSystemObject;

/**
 * @author epr
 */
public class BootableHashMap<K, V> extends VmSystemObject implements Map<K, V> {

	private HashMap<K, V> mapCache;
	private Entry<K, V>[] entryArray;
	private int hashCode;
	private transient boolean locked;
	
	/**
	 * Constructs an empty HashMap with the default initial capacity (16) 
	 * and the default load factor (0.75).
	 */
	public BootableHashMap() {
		this.hashCode = super.hashCode();
	}
	
	/**
	 * Constructs an empty HashMap with the default initial capacity (16) 
	 * and the default load factor (0.75).
	 * @param initialCapacity
	 */
	public BootableHashMap(int initialCapacity) {
		mapCache = new HashMap<K, V>(initialCapacity);
		this.hashCode = mapCache.hashCode();
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 * @return int
	 */
	public int hashCode() {
		if (mapCache != null) {
			return getMapCache().hashCode();			
		} else {
			return hashCode;
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 * @return String
	 */
	public String toString() {
		if (mapCache != null) {
			return getMapCache().toString();
		} else {
			return super.toString();
		}
	}

	/**
	 * @return The collection of values
	 */
	public Collection<V> values() {
		return getMapCache().values();
	}

	/**
	 * @return The set of keys
	 */
	public Set<K> keySet() {
		return getMapCache().keySet();
	}

	/**
	 * @param key
	 * @return The object for the given key, or null if the given key is not found.
	 */
	public V get(Object key) {
		return getMapCache().get(key);
	}

	/**
	 * 
	 */
	public void clear() {
		getMapCache().clear();
	}

	/**
	 * @return The number of elements
	 */
	public int size() {
		return getMapCache().size();
	}

	/**
	 * @param key
	 * @param value
	 * @return Object
	 */
	public V put(K key, V value) {
		return getMapCache().put(key, value);
	}

	/**
	 * @param m
	 */
	public void putAll(Map<? extends K, ? extends V> m) {
		getMapCache().putAll(m);
	}

	/**
	 * @return The set of entries
	 */
	public Set<Map.Entry<K, V>> entrySet() {
		return getMapCache().entrySet();
	}

	/**
	 * @param key
	 * @return True if the key is contained, false otherwiser
	 */
	public boolean containsKey(Object key) {
		return getMapCache().containsKey(key);
	}

	/**
	 * @return True if this map is empty, false otherwise
	 */
	public boolean isEmpty() {
		return getMapCache().isEmpty();
	}

	/**
	 * @param obj
	 * @see java.lang.Object#equals(java.lang.Object)
	 * @return boolean
	 */
	public boolean equals(Object obj) {
		return getMapCache().equals(obj);
	}

	/**
	 * @param o
	 * @return Object
	 */
	public V remove(Object o) {
		return getMapCache().remove(o);
	}

	/**
	 * @param value
	 * @return True if the given value is contained, false otherwise
	 */
	public boolean containsValue(Object value) {
		return getMapCache().containsValue(value);
	}



	static final class Entry<eK, eV> extends VmSystemObject {
		private final eK key;
		private final eV value;
		
		public Entry(Map.Entry<eK, eV> entry) {
			this.key = entry.getKey();
			this.value = entry.getValue();
		}
		
		/**
		 * Gets the key
		 * @return Object
		 */
		public eK getKey() {
			return key;
		}

		/**
		 * Gets the value
		 * @return Object
		 */
		public eV getValue() {
			return value;
		}
	}
	
	/**
	 * Gets the hashmap
	 * @return
	 */
	private final HashMap<K, V> getMapCache() {
		if (locked) {
			throw new RuntimeException("Cannot change a locked BootableHashMap");
		}
		if (mapCache == null) {
			mapCache = new HashMap<K, V>();
			if (entryArray != null) {
				final int max = entryArray.length;
				for (int i = 0; i < max; i++) {
					final Entry<K, V> e = entryArray[i];
					mapCache.put(e.getKey(), e.getValue());
				}
				entryArray = null;
			}
		}
		return mapCache;
	}

	/**
	 * @see org.jnode.vm.VmSystemObject#verifyBeforeEmit()
	 */
    @SuppressWarnings("unchecked")
	public void verifyBeforeEmit() {
		super.verifyBeforeEmit();
		
		if (mapCache != null) {
			entryArray = new Entry[mapCache.size()];
			int index = 0;
			for (Map.Entry<K, V> entry : mapCache.entrySet()) {
				entryArray[index++] = new Entry<K, V>(entry);
			}
			hashCode = mapCache.hashCode();
			mapCache = null;
		}
		locked = true;
	}

}
