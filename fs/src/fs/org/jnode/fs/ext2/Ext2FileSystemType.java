/*
 * $Id$
 */
package org.jnode.fs.ext2;

import org.jnode.driver.Device;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.FileSystemType;
import org.jnode.fs.partitions.PartitionTableEntry;
import org.jnode.fs.partitions.ibm.IBMPartitionTableEntry;
import org.jnode.fs.partitions.ibm.IBMPartitionTypes;

/**
 * @author Andras Nagy
 */
public class Ext2FileSystemType implements FileSystemType {

	public static final String NAME = "EXT2";

	/**
	 * @see org.jnode.fs.FileSystemType#create(Device)
	 */
	public FileSystem create(Device device) throws FileSystemException {
		return new Ext2FileSystem(device);
	}

	/**
	 * @see org.jnode.fs.FileSystemType#getName()
	 */
	public String getName() {
		return NAME;
	}

	/**
	 * @see org.jnode.fs.FileSystemType#supports(PartitionTableEntry, byte[], FSBlockDeviceAPI)
	 */
	public boolean supports(PartitionTableEntry pte, byte[] firstSector, FSBlockDeviceAPI devApi) {
		if (pte instanceof IBMPartitionTableEntry) {
			return (((IBMPartitionTableEntry)pte).getSystemIndicator() == IBMPartitionTypes.PARTTYPE_LINUXNATIVE);
		} else
			return false;
	}

	/**
	 * @see org.jnode.fs.FileSystemType#format(org.jnode.driver.Device, java.lang.Object)
	 */
	public FileSystem format(Device device, Object specificOptions) throws FileSystemException {
        throw new FileSystemException("Not ye implemented");
	}
}
