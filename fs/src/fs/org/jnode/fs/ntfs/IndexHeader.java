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
 
package org.jnode.fs.ntfs;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class IndexHeader extends NTFSStructure {

    /** Size of this structure index bytes */
    public static final int SIZE = 0x10;
    
    public static final class Flags {
        public static final int LARGE_INDEX = 0x01;
    }
    
    /**
     * Initialize this instance.
     * @param attr
     */
    public IndexHeader(IndexRootAttribute attr) {
        super(attr, attr.getAttributeOffset() + 0x10);
    }
    
    /**
     * Initialize this instance.
     * @param indexBlock
     * @param offset
     */
    public IndexHeader(IndexBlock indexBlock, int offset) {
        super(indexBlock, offset);
    }
    
    /**
     * Gets the offset of the first index entry.
     * @return
     */
    public int getFirstEntryOffset() {
        return getUInt32AsInt(0x00);
    }
    
    /**
     * Gets the total size of the index entries (in bytes???).
     * @return
     */
    public int getIndexEntriesSize() {
        return getUInt32AsInt(0x04);
    }
    
    /**
     * Gets the allocated size of the index entries (in bytes???).
     * @return
     */
    public int getAllocatedIndexEntriesSize() {
        return getUInt32AsInt(0x08);
    }
    
    /**
     * Gets the flags.
     * @return
     */
    public int getFlags() {
        return getUInt8(0x0C);
    }

    /**
     * Is an index allocation needed.
     * @return
     */
    public boolean isLargeIndex() {
        return ((getFlags() & Flags.LARGE_INDEX) != 0);
    }    
}
