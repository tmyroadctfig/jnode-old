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
 
package org.jnode.driver.chipset.via;

import org.apache.log4j.Logger;
import org.jnode.driver.Driver;
import org.jnode.driver.DriverException;
import org.jnode.driver.pci.PCIDevice;
import org.jnode.util.NumberUtils;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class Via82C686 extends Driver {

	/** My logger */
	private final Logger log = Logger.getLogger(getClass());
	
	/**
	 * Start the device
	 * @throws DriverException
	 */
	protected void startDevice() throws DriverException {
		// TODO apply io-apic quirk
		
		final PCIDevice dev = (PCIDevice)getDevice();
		for (int i = 0x55; i <= 0x58; i++) {
			final int v = dev.readConfigByte(i);
			log.debug("PCI[" + NumberUtils.hex(i, 2) + "] " + NumberUtils.hex(v, 2));
		}
		
	}

	/**
	 * Stop the device
	 * @throws DriverException
	 */
	protected void stopDevice() throws DriverException {
		// Nothing to do here
	}
}
