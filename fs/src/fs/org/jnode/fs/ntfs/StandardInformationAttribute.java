/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 
package org.jnode.fs.ntfs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jnode.fs.ntfs.attribute.NTFSResidentAttribute;

/**
 * @author Daniel Noll (daniel@noll.id.au)
 */
public class StandardInformationAttribute extends NTFSResidentAttribute {

    /**
     * Constructs the attribute.
     *
     * @param fileRecord the containing file record.
     * @param offset offset of the attribute within the file record.
     */
    public StandardInformationAttribute(FileRecord fileRecord, int offset) {
        super(fileRecord, offset);
    }

    /**
     * Gets the creation time.
     *
     * @return the creation time, as a 64-bit NTFS filetime value.
     */
    public long getCreationTime() {
        return getInt64(getAttributeOffset());
    }

    /**
     * Gets the modification time.
     *
     * @return the modification time, as a 64-bit NTFS filetime value.
     */
    public long getModificationTime() {
        return getInt64(getAttributeOffset() + 0x08);
    }

    /**
     * Gets the time when the MFT record last changed.
     *
     * @return the MFT change time, as a 64-bit NTFS filetime value.
     */
    public long getMftChangeTime() {
        return getInt64(getAttributeOffset() + 0x10);
    }

    /**
     * Gets the access time.
     *
     * @return the access time, as a 64-bit NTFS filetime value.
     */
    public long getAccessTime() {
        return getInt64(getAttributeOffset() + 0x18);
    }

    /**
     * Gets the flags.
     *
     * @return the flags.
     */
    public int getFlags() {
        return getInt32(getAttributeOffset() + 0x20);
    }

    /**
     * Gets the maximum number of versions.
     *
     * @return the maximum.
     */
    public int getMaxVersions() {
        return getInt32(getAttributeOffset() + 0x24);
    }

    /**
     * Gets the version number.
     *
     * @return the version number.
     */
    public int getVersionNumber() {
        return getInt32(getAttributeOffset() + 0x28);
    }

    /**
     * Gets the class ID.
     *
     * @return the class ID.
     */
    public int getClassId() {
        return getInt32(getAttributeOffset() + 0x2c);
    }

    /**
     * Gets the owner ID (version 3.0+).
     *
     * @return the owner ID.
     */
    public int getOwnerId() {
        return getInt32(getAttributeOffset() + 0x30);
    }

    /**
     * Gets the security ID (version 3.0+).
     *
     * @return the security ID.
     */
    public int getSecurityId() {
        return getInt32(getAttributeOffset() + 0x34);
    }

    /**
     * Gets the quota charged (version 3.0+).
     *
     * @return the quota charged.
     */
    public int getQuotaCharged() {
        return getInt32(getAttributeOffset() + 0x38);
    }

    /**
     * Gets the update sequence number (USN) (version 3.0+).
     *
     * @return the update sequence number.
     */
    public int getUpdateSequenceNumber() {
        return getInt32(getAttributeOffset() + 0x40);
    }

    /**
     * The file attribute flags.
     */
    public static final class Flags {

        /**
         * The map of value to name.
         */
        private static final Map<Integer, String> flagNames = new LinkedHashMap<Integer, String>();

        public static final int READ_ONLY = register("Read-only", 0x1);
        public static final int HIDDEN = register("Hidden", 0x2);
        public static final int SYSTEM = register("System", 0x4);
        public static final int ARCHIVE = register("Archive", 0x20);
        public static final int DEVICE = register("Archive", 0x40);
        public static final int NORMAL = register("Normal", 0x80);
        public static final int TEMPORARY = register("Temporary", 0x100);
        public static final int SPARSE = register("Sparse", 0x200);
        public static final int REPARSE_POINT = register("Reparse Point", 0x400);
        public static final int COMPRESSED = register("Compressed", 0x800);
        public static final int OFFLINE = register("Offline", 0x1000);
        public static final int NOT_INDEXED = register("Not Indexed", 0x2000);
        public static final int ENCRYPTED = register("Encrypted", 0x4000);

        public static List<String> getNames(int value) {
            List<String> names = new ArrayList<String>();

            for (Map.Entry<Integer, String> entry : flagNames.entrySet()) {
                int flag = entry.getKey();

                if ((value & flag) != 0) {
                    names.add(entry.getValue());
                    value -= flag;
                }
            }

            if (value != 0) {
                names.add(String.format("Unknown 0x%x", value));
            }

            return names;
        }

        /**
         * Registers a value with the name map.
         *
         * @param name the name to register.
         * @param value the value.
         * @return the value.
         */
        private static int register(String name, int value) {
            flagNames.put(value, name);
            return value;
        }
    }
}
