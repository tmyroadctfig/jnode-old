/*
 * $Id$
 */
package org.jnode.driver.video.ati.mach64;

import org.jnode.driver.Device;
import org.jnode.driver.DeviceException;
import org.jnode.driver.DriverException;
import org.jnode.driver.pci.PCIDevice;
import org.jnode.driver.video.AbstractFrameBufferDriver;
import org.jnode.driver.video.AlreadyOpenException;
import org.jnode.driver.video.FrameBufferConfiguration;
import org.jnode.driver.video.HardwareCursorAPI;
import org.jnode.driver.video.Surface;
import org.jnode.driver.video.UnknownConfigurationException;
import org.jnode.plugin.ConfigurationElement;
import org.jnode.system.ResourceNotFreeException;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class Mach64Driver extends AbstractFrameBufferDriver implements Mach64Constants {

    private FrameBufferConfiguration currentConfig;
    private Mach64Core kernel;
    private final String model;

	private static final FrameBufferConfiguration[] CONFIGS = new FrameBufferConfiguration[] { 
		Mach64Configuration.VESA_118, 
		Mach64Configuration.VESA_115 };


    /**
	 * Create a new instance
	 */
	public Mach64Driver(ConfigurationElement config) throws DriverException {
	    this.model = config.getAttribute("name");
	}

    /**
     * @see org.jnode.driver.video.FrameBufferAPI#getConfigurations()
     */
    public FrameBufferConfiguration[] getConfigurations() {
        return CONFIGS;
    }
    /**
     * @see org.jnode.driver.video.FrameBufferAPI#getCurrentConfiguration()
     */
    public FrameBufferConfiguration getCurrentConfiguration() {
        return currentConfig;
    }
    /**
     * @see org.jnode.driver.video.FrameBufferAPI#open(org.jnode.driver.video.FrameBufferConfiguration)
     */
    public Surface open(FrameBufferConfiguration config)
            throws UnknownConfigurationException, AlreadyOpenException,
            DeviceException {
		for (int i = 0; i < CONFIGS.length; i++) {
			if (config.equals(CONFIGS[i])) {
				try {
					final Mach64Surface surface;
                    surface = kernel.open((Mach64Configuration) config);
    				this.currentConfig = config;
    				return surface;
                } catch (ResourceNotFreeException ex) {
                    throw new DeviceException(ex);
                }
			}
		}
		throw new UnknownConfigurationException();
    }

	/**
	 * Notify of a close of the graphics object
	 * @param graphics
	 */
	final synchronized void close(Mach64Core graphics) {
		this.currentConfig = null;
	}
	
	/**
	 * @see org.jnode.driver.Driver#startDevice()
	 */
	protected void startDevice() throws DriverException {
		try {
			kernel = new Mach64Core(this, model, (PCIDevice) getDevice());
		} catch (ResourceNotFreeException ex) {
			throw new DriverException(ex);
		}
		super.startDevice();
		final Device dev = getDevice();
		//dev.registerAPI(DisplayDataChannelAPI.class, kernel);
		//dev.registerAPI(HardwareCursorAPI.class, kernel.getHardwareCursor());
	}

	/**
	 * @see org.jnode.driver.Driver#stopDevice()
	 */
	protected void stopDevice() throws DriverException {
		if (currentConfig != null) {
			kernel.close();
		}
		if (kernel != null) {
			kernel.release();
			kernel = null;
		}
		final Device dev = getDevice();
		//dev.unregisterAPI(DisplayDataChannelAPI.class);
		dev.unregisterAPI(HardwareCursorAPI.class);
		super.stopDevice();
	}

}
