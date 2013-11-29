package org.jnode.fs.ext4;

import java.util.Iterator;
import org.jnode.driver.Device;
import org.jnode.driver.block.FileDevice;
import org.jnode.fs.DataStructureAsserts;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FileSystemTestUtils;
import org.jnode.fs.ext2.Ext2FileSystem;
import org.jnode.fs.ext2.Ext2FileSystemType;
import org.jnode.fs.service.FileSystemService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class Ext4FileSystemTest {

    private Device device;
    private FileSystemService fss;

    @Before
    public void setUp() throws Exception {
        // create file system service.
        fss = FileSystemTestUtils.createFSService(Ext2FileSystemType.class.getName());
    }

    @Test
    public void testReadExt4SpecialFiles() throws Exception {

        device = new FileDevice(FileSystemTestUtils.getTestFile("ext4/test-special-files.ext4"), "r");
        Ext2FileSystemType type = fss.getFileSystemType(Ext2FileSystemType.ID);
        Ext2FileSystem fs = type.create(device, true);

        String expectedStructure =
            "type: EXT2 vol: total:15728640 free:13918208\n" +
            "  /; \n" +
            "    lost+found; \n" +
            "    console; 0; d41d8cd98f00b204e9800998ecf8427e\n" +
            "    fifo; 0; d41d8cd98f00b204e9800998ecf8427e\n" +
            "    sda1; 0; d41d8cd98f00b204e9800998ecf8427e\n" +
            "    The_Rabies_Virus_Remains_a_Medical_Mystery.jpg; 6606; a28d342db2d2081f9d2eb287d49c1110\n" +
            "    wired-science.jpg; 16; 67bc4bf64a29239a9f148fb768bfbbc8\n" +
            "    wolf_slice_1.jpg; 6606; a28d342db2d2081f9d2eb287d49c1110\n" +
            "    index.html; 106102; 99248bc850c65b03b04776342e4b3e7d\n";

        DataStructureAsserts.assertStructure(fs, expectedStructure);
    }

    @Test
    public void testReadExt4Mmp() throws Exception {

        device = new FileDevice(FileSystemTestUtils.getTestFile("ext4/ext4-mmp.dd"), "r");
        Ext2FileSystemType type = fss.getFileSystemType(Ext2FileSystemType.ID);
        Ext2FileSystem fs = type.create(device, true);

        String expectedStructure =
            "type: EXT2 vol: total:2998272 free:1870848\n" +
                "  /; \n" +
                "    lost+found; \n";

        DataStructureAsserts.assertStructure(fs, expectedStructure);
    }

    @Test
    public void testReadExt4LargeDirectory() throws Exception {

        // Filesystem created without the 'dir_index' feature
        device = new FileDevice(FileSystemTestUtils.getTestFile("ext4/ext4-large-directory.dd"), "r");
        Ext2FileSystemType type = fss.getFileSystemType(Ext2FileSystemType.ID);
        Ext2FileSystem fs = type.create(device, true);

        FSDirectory rootDirectory = fs.getRootEntry().getDirectory();
        FSDirectory largeDirectory = rootDirectory.getEntry("large-directory").getDirectory();

        int childCount = 0;
        Iterator<? extends FSEntry> iterator = largeDirectory.iterator();
        while (iterator.hasNext()) {
            FSEntry entry = iterator.next();

            if (entry.isFile()) {
                Assert.assertEquals("b1946ac92492d2347c6235b4d2611184", DataStructureAsserts.getMD5Digest(entry.getFile()));
                childCount++;
            }
        }

        Assert.assertEquals(65001, childCount);
    }

    @Test
    public void testReadExt4LargeDirectoryWithIndex() throws Exception {

        // Filesystem created with the 'dir_index' feature
        device = new FileDevice(FileSystemTestUtils.getTestFile("ext4/ext4-large-dir-with-index.dd"), "r");
        Ext2FileSystemType type = fss.getFileSystemType(Ext2FileSystemType.ID);
        Ext2FileSystem fs = type.create(device, true);

        FSDirectory rootDirectory = fs.getRootEntry().getDirectory();
        FSDirectory largeDirectory = rootDirectory.getEntry("large-directory").getDirectory();

        int childCount = 0;
        Iterator<? extends FSEntry> iterator = largeDirectory.iterator();
        while (iterator.hasNext()) {
            FSEntry entry = iterator.next();

            if (entry.isFile()) {
                Assert.assertEquals("b1946ac92492d2347c6235b4d2611184", DataStructureAsserts.getMD5Digest(entry.getFile()));
                childCount++;
            }
        }

        Assert.assertEquals(65001, childCount);
    }
}

