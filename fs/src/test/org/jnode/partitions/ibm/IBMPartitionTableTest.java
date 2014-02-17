package org.jnode.partitions.ibm;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collection;
import org.jnode.fs.FileSystemTestUtils;
import org.jnode.util.FileUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link IBMPartitionTable}.
 */
public class IBMPartitionTableTest {

    @Test
    public void testDetection() throws Exception {

        Collection<String> testFileNames = Arrays.asList(
            "bsd.dd",
            "dos-2.dd",
            "dos-3-to-6.dd",
            "freebsd.dd",
            "grub.dd",
            "hp-en.dd",
            "hp-no.dd",
            "lilo.dd",
            "makeboot.dd",
            "mbr-sig.dd",
            "syslinux.dd",
            "win-2k.dd",
            "win-9x.dd",
            "win-vista.dd",
            "win-xp.dd");

        for (String testFileName : testFileNames) {
            File testFile = FileSystemTestUtils.getTestFile("partitions/ibm/" + testFileName);
            byte[] buffer = FileUtils.load(new FileInputStream(testFile), true);

            System.out.println("Testing: " + testFileName);
            Assert.assertTrue("Expected a valid partition for: " + testFileName,
                IBMPartitionTable.containsPartitionTable(buffer));
        }
    }

    @Test
    public void testNotDetected() throws Exception {

        Collection<String> testFileNames = Arrays.asList(
            "linux-kernel.dd");

        for (String testFileName : testFileNames) {
            File testFile = FileSystemTestUtils.getTestFile("partitions/ibm/" + testFileName);
            byte[] buffer = FileUtils.load(new FileInputStream(testFile), true);

            System.out.println("Testing: " + testFileName);
            Assert.assertFalse("Not expected a valid partition for: " + testFileName,
                IBMPartitionTable.containsPartitionTable(buffer));
        }
    }
}
