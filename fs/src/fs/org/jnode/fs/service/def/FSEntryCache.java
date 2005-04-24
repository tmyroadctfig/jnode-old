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
 
package org.jnode.fs.service.def;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.jnode.fs.FSEntry;

/**
 * @author epr
 */
final class FSEntryCache {

	/** My filesystem manager */
	//private final FileSystemManager fsm;
	/** The actual cache */
	private final HashMap<String, FSEntry> entries = new HashMap<String, FSEntry>();
	
	/**
	 * Create a new instance
	 * @param fsm
	 */
	public FSEntryCache(FileSystemManager fsm) {
		//this.fsm = fsm;
	}
	
	/**
	 * Gets a cached entry for a given path.
	 * @param path must be an absolute path
	 */
	public synchronized FSEntry getEntry(String path) {
		final FSEntry entry = entries.get(path);
		if (entry != null) {
			if (entry.isValid()) {
				return entry; 
			} else {
				entries.remove(path);
				return null;
			}
		} else {
			return null;
		}
	}
	
	/**
	 * Puts an entry in the cache. Any existing entry for the given path
	 * will be removed.
	 * @param path must be an absolute path
	 * @param entry
	 */
	public synchronized void setEntry(String path, FSEntry entry) {
		entries.put(path, entry);
	}
	
	/**
	 * Remove any entry bound to the given path or a path below the given
	 * path.
	 * @param rootPathStr must be an absolute path
	 */
	public synchronized void removeEntries(String rootPathStr) {
		entries.remove(rootPathStr);
		final ArrayList<String> removePathList = new ArrayList<String>();
		for (String pathStr : entries.keySet()) {
			if (pathStr.startsWith(rootPathStr)) {
				removePathList.add(pathStr);
			}
		}
		for (String path : removePathList) {
			entries.remove(path);			
		}
	}
}
