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
 
package org.jnode.awt;

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.Component;

/**
 * @author epr
 */
public class JNodeGenericPeer {

	protected final JNodeToolkit toolkit;
	protected final Object component;

	// Global event queue.
	protected static EventQueue eventQueue;

	public JNodeGenericPeer(JNodeToolkit toolkit, Object component) {
		this.toolkit = toolkit;
		this.component = component;
	}

	static void enableQueue(EventQueue sq) {
		if (eventQueue == null) {
			eventQueue = sq;
		}
	}
	
	/**
	 * @return
	 */
	public final Object getComponent() {
		return this.component;
	}

	/**
	 * @see java.awt.peer.ComponentPeer#getToolkit()
	 * @return The toolkit
	 */
	public final Toolkit getToolkit() {
		return toolkit;
	}

	/**
	 * Gets the implementation toolkit
	 * @return The toolkit
	 */
	public final JNodeToolkit getToolkitImpl() {
		return toolkit;
	}

	/**
	 * Destroy the peer and release all resource
	 */
	public void dispose() {
		// Nothing to do
	}
}
