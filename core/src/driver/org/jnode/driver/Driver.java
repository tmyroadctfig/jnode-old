/**
 * $Id$
 */
package org.jnode.driver;


/**
 * Abstract driver of a Device.
 * 
 * Every device driver must extend this class directly or indirectly.
 * 
 * A suitable driver for a specific Device is found by a DeviceToDriverMapper.
 * 
 * @see org.jnode.driver.Device
 * @see org.jnode.driver.DeviceToDriverMapper
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class Driver {
		
	/** The device this driver it to control */
	private Device device;
		
	/**
	 * Default constructor
	 */
	public Driver() {
	}

	/**
	 * Sets the device this driver is to control.
	 * @param device The device to control, never null
	 * from the device.
	 * @throws DriverException
	 */
	protected final void connect(Device device)
	throws DriverException {
		if (this.device != null) {
			throw new DriverException("This driver is already connected to a device");
		}
		verifyConnect(device);
		this.device = device;
		afterConnect(device);
	}

	/**
	 * Gets the device this driver is to control.
	 * @return The device I'm driving
	 */
	public final Device getDevice() {
		return device;
	}
	
	/**
	 * This method is called just before a new device is set to this driver.
	 * If we should refuse the given device, throw a DriverException.
	 * @param device
	 * @throws DriverException
	 */
	protected void verifyConnect(Device device)
	throws DriverException {
		/* do nothing for now */
	}
	
	/**
	 * This method is called after a new device is set to this driver.
	 * You can initialize the driver and/or the device here. 
	 * Note not to start the device yet.
	 * @param device
	 */
	protected void afterConnect(Device device) {
		/* do nothing for now */
	}
	
	/**
	 * Start the device.
	 * @throws DriverException
	 */
	protected abstract void startDevice()
	throws DriverException;
	
	/**
	 * Stop the device.
	 * @throws DriverException
	 */
	protected abstract void stopDevice() 
	throws DriverException;
}
