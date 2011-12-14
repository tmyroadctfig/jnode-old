/*
 * $Id: $
 *
 * Copyright (C) 2003-2010 JNode.org
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
 * along with this library; If not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jnode.partitions.gpt;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.partitions.PartitionTable;
import org.jnode.partitions.PartitionTableType;
import org.jnode.util.LittleEndian;

/**
 * The main GPT partition table class.
 *
 * @author Luke Quinane
 */
public class GptPartitionTable implements PartitionTable<GptPartitionTableEntry> {

    /** The type of partition table */
    private final GptPartitionTableType tableType;

    /** The partition entries */
    private final List<GptPartitionTableEntry> partitions = new ArrayList<GptPartitionTableEntry>();

    /** My logger */
    private static final Logger log = Logger.getLogger(GptPartitionTable.class);

    /**
     * Create a new instance
     *
     * @param tableType the partition table type.
     * @param first16KiB the first 16,384 bytes of the disk.
     * @param device the drive device.
     */
    public GptPartitionTable(GptPartitionTableType tableType, byte[] first16KiB, Device device) {
        this.tableType = tableType;

        if (containsPartitionTable(first16KiB)) {
            long entries = LittleEndian.getUInt32(first16KiB, 0x50);

            for (int partitionNumber = 0; partitionNumber < entries; partitionNumber++) {
                log.debug("try part " + partitionNumber);

                partitions.add(new GptPartitionTableEntry(this, first16KiB, partitionNumber));
            }
        }
    }

    /**
     * Checks if the given boot sector contain a GPT partition table.
     *
     * @param first16KiB the first 16,384 bytes of the disk.
     * @return {@code true} if the boot sector contains a GPT partition table.
     */
    public static boolean containsPartitionTable(byte[] first16KiB) {
        if (first16KiB.length < 0x1000 + 8) {
            // Not enough data to check for a valid partition table
            return false;
        }

        byte[] signatureBytes = new byte[8];
        System.arraycopy(first16KiB, 0x1000, signatureBytes, 0, signatureBytes.length);
        String signature = new String(signatureBytes, Charset.forName("US-ASCII"));

        return "EFI PART".equals(signature);
    }

    public Iterator<GptPartitionTableEntry> iterator() {
        return Collections.unmodifiableList(partitions).iterator();
    }

    /**
     * @see org.jnode.partitions.PartitionTable#getType()
     */
    public PartitionTableType getType() {
        return tableType;
    }
}