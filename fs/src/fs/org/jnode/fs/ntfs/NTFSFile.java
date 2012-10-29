/*
 * $Id$
 *
 * Copyright (C) 2003-2012 JNode.org
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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import org.jnode.fs.FSFile;
import org.jnode.fs.FSFileSlackSpace;
import org.jnode.fs.FSFileStreams;
import org.jnode.fs.FileSystem;
import org.jnode.util.ByteBufferUtils;

/**
 * @author vali
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class NTFSFile implements FSFile, FSFileSlackSpace, FSFileStreams {

    /**
     * The associated file record.
     */
    private FileRecord fileRecord;

    /**
     * The file system that contains this file.
     */
    private NTFSFileSystem fs;

    private final IndexEntry indexEntry;

    /**
     * Initialize this instance.
     *
     * @param fs the file system.
     * @param indexEntry
     */
    public NTFSFile(NTFSFileSystem fs, IndexEntry indexEntry) {
        this.fs = fs;
        this.indexEntry = indexEntry;
    }

    public long getLength() {
        return indexEntry.getRealFileSize();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSFile#setLength(long)
     */
    public void setLength(long length) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSFile#read(long, byte[], int, int)
     */
    // public void read(long fileOffset, byte[] dest, int off, int len)
    public void read(long fileOffset, ByteBuffer destBuf) throws IOException {
        // TODO optimize it also to use ByteBuffer at lower level
        final ByteBufferUtils.ByteArray destBA = ByteBufferUtils.toByteArray(destBuf);
        final byte[] dest = destBA.toArray();
        getFileRecord().readData(fileOffset, dest, 0, dest.length);
        destBA.refreshByteBuffer();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSFile#write(long, byte[], int, int)
     */
    // public void write(long fileOffset, byte[] src, int off, int len) {
    public void write(long fileOffset, ByteBuffer src) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSObject#isValid()
     */
    public boolean isValid() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSObject#getFileSystem()
     */
    public FileSystem<?> getFileSystem() {
        return fs;
    }

    /**
     * @return Returns the fileRecord.
     */
    public FileRecord getFileRecord() {
        if (fileRecord == null) {
            try {
                fileRecord =
                        indexEntry.getParentFileRecord().getVolume().getMFT().getIndexedFileRecord(
                                indexEntry);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return this.fileRecord;
    }

    /**
     * @param fileRecord The fileRecord to set.
     */
    public void setFileRecord(FileRecord fileRecord) {
        this.fileRecord = fileRecord;
    }

    @Override
    public byte[] getSlackSpace() throws IOException {
        FileRecord.AttributeIterator dataAttributes =
            getFileRecord().findAttributesByTypeAndName(NTFSAttribute.Types.DATA, null);
        NTFSAttribute attribute = dataAttributes.next();

        if (attribute == null || attribute.isResident()) {
            // If the data attribute is missing there is no slack space. If it is resident then another attribute might
            // immediately follow the data. So for now we'll ignore that case
            return new byte[0];
        }

        int clusterSize = ((NTFSFileSystem) getFileSystem()).getNTFSVolume().getClusterSize();

        int slackSpaceSize = clusterSize - (int) (getLength() % clusterSize);

        if (slackSpaceSize == clusterSize) {
            slackSpaceSize = 0;
        }

        byte[] slackSpace = new byte[slackSpaceSize];
        getFileRecord().readData(getLength(), slackSpace, 0, slackSpace.length);

        return slackSpace;
    }

    /**
     * Flush any cached data to the disk.
     * 
     * @throws IOException
     */
    public void flush() throws IOException {
        // TODO implement me
    }

    @Override
    public Map<String, FSFile> getStreams() {
        Map<String, FSFile> streams = new HashMap<String, FSFile>();

        FileRecord.AttributeIterator dataAttributes = getFileRecord().findAttributesByType(NTFSAttribute.Types.DATA);
        NTFSAttribute attribute = dataAttributes.next();

        while (attribute != null) {
            String attributeName = attribute.getAttributeName();

            // The unnamed data attribute is the main file data, so ignore it
            if (attributeName != null) {
                streams.put(attributeName, new StreamFile(attributeName, attribute));
            }

            attribute = dataAttributes.next();
        }

        return streams;
    }

    /**
     * A file for reading data out of alternate streams.
     */
    private class StreamFile implements FSFile {
        /**
         * The name of the alternate data stream.
         */
        private String attributeName;

        /**
         * The attribute for the alternate data stream.
         */
        private NTFSAttribute attribute;

        /**
         * Creates a new stream file.
         *
         * @param attributeName the name of the alternate data stream.
         * @param attribute the attribute for the alternate data stream.
         */
        public StreamFile(String attributeName, NTFSAttribute attribute) {
            this.attributeName = attributeName;
            this.attribute = attribute;
        }

        @Override
        public long getLength() {
            if (attribute.isResident()) {
                NTFSResidentAttribute dataAttribute = (NTFSResidentAttribute) attribute;
                return dataAttribute.getAttributeLength();
            }
            else {
                NTFSNonResidentAttribute dataAttribute = (NTFSNonResidentAttribute) attribute;
                return dataAttribute.getAttributeActualSize();
            }
        }

        @Override
        public void setLength(long length) throws IOException {
            throw new UnsupportedOperationException("Not implemented yet");
        }

        @Override
        public void read(long fileOffset, ByteBuffer dest) throws IOException {
            ByteBufferUtils.ByteArray destByteArray = ByteBufferUtils.toByteArray(dest);
            byte[] destBuffer = destByteArray.toArray();
            getFileRecord().readData(attributeName, fileOffset, destBuffer, 0, destBuffer.length);
            destByteArray.refreshByteBuffer();
        }

        @Override
        public void write(long fileOffset, ByteBuffer src) throws IOException {
            throw new UnsupportedOperationException("Not implemented yet");
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public FileSystem<?> getFileSystem() {
            return NTFSFile.this.getFileSystem();
        }
    }
}
