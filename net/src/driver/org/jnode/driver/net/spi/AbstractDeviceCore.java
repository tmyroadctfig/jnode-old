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
 
package org.jnode.driver.net.spi;

import org.apache.log4j.Logger;
import org.jnode.net.HardwareAddress;
import org.jnode.net.SocketBuffer;
import org.jnode.util.TimeoutException;

/**
 * This abstract class is not intended for any external purpose. It only serves
 * as a voluntary guide for driver implementation of network cards.
 * 
 * @author epr
 */
public abstract class AbstractDeviceCore {
	
	/** My logger */
	protected final Logger log = Logger.getLogger(getClass());

	/**
	 * Gets the hardware address of this device
	 */
	public abstract HardwareAddress getHwAddress();
	
	/**
	 * Initialize the device
	 */
	public abstract void initialize();
	
	/**
	 * Disable the device
	 */
	public abstract void disable();

	/**
	 * Release all resources
	 */
	public abstract void release();

	/**
	 * Transmit the given buffer
	 * @param buf
	 * @param timeout
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	public abstract void transmit(SocketBuffer buf, long timeout)
	throws InterruptedException, TimeoutException;
}
