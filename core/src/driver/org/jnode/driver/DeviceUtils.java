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
 
package org.jnode.driver;

import java.util.Collection;
import java.util.Collections;

import javax.naming.NameNotFoundException;

import org.jnode.naming.InitialNaming;

/**
 * Class with utility methods for the device framework.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class DeviceUtils {

	/** Cached devicemanager reference */
	private static DeviceManager dm;
	
	/**
	 * Gets the device manager
	 * @return The device manager
	 * @throws NameNotFoundException
	 */
	public static DeviceManager getDeviceManager() 
	throws NameNotFoundException {
		if (dm == null) {
			dm = (DeviceManager)InitialNaming.lookup(DeviceManager.NAME);
		}
		return dm;
	}

	/**
	 * Gets a device by name
	 * @param deviceID
	 * @return The device
	 * @throws DeviceNotFoundException
	 */
	public static Device getDevice(String deviceID) 
	throws DeviceNotFoundException {
		try {
			return getDeviceManager().getDevice(deviceID);
		} catch (NameNotFoundException ex) {
			throw new DeviceNotFoundException("DeviceManager not found", ex);
		}
	}
	
	/**
	 * Gets a specific API from a device.
	 * @param deviceID the ame of the requested device
	 * @param api the API class to use
	 * @return The api implementation
	 * @throws DeviceNotFoundException
	 * @throws ApiNotFoundException
	 */
	public static  <T extends DeviceAPI> T getAPI(String deviceID, Class<T> api) 
	throws DeviceNotFoundException, ApiNotFoundException
	{
		try {
			return getDeviceManager().getDevice(deviceID).getAPI(api);
		} catch (NameNotFoundException ex) {
			throw new DeviceNotFoundException("DeviceManager not found", ex);
		}
	}
	

	/**
	 * Returns a collection of all known devices that implement the given api..
	 * The collection is not modifiable, but the underlying collection
	 * can change, so be aware of exceptions in iterators.
	 * @param apiClass
	 * @return All known devices the implement the given api.
	 */
	public static Collection<Device> getDevicesByAPI(Class<? extends DeviceAPI> apiClass) {
		try {
			return getDeviceManager().getDevicesByAPI(apiClass);
		} catch (NameNotFoundException ex) {
			return Collections.emptyList();
		}
	}
}
