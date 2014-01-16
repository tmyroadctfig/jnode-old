package org.jnode.fs.exfat;

import junit.framework.TestCase;
import org.jnode.driver.Device;
import org.jnode.driver.block.FileDevice;
import org.jnode.fs.DataStructureAsserts;
import org.jnode.fs.FileSystemTestUtils;
import org.jnode.fs.service.FileSystemService;

public class ExFatFileSystemTest extends TestCase {

    private Device device;
    private FileSystemService fss;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // create file system service.
        fss = FileSystemTestUtils.createFSService(ExFatFileSystemType.class.getName());
    }

    public void testReadSmallDisk() throws Exception {

        device = new FileDevice(FileSystemTestUtils.getTestFile("fs/exfat/test.exfat"), "r");
        ExFatFileSystemType type = fss.getFileSystemType(ExFatFileSystemType.ID);
        ExFatFileSystem fs = type.create(device, true);

        String expectedStructure =
            "type: ExFAT vol:Disk Image total:-1 free:-1\n" +
                "  null; \n" +
                "    .DS_Store; 6148; f4ca5ca925aae4c51cf564b7e8fc5ead\n" +
                "    test.txt; 179; 73ced839d7039cc88c03ddc225159bd5\n" +
                "    ._.DS_Store; 4096; 19233eef9b0c16089a3522fb2eefe83f\n" +
                "    .Trashes; \n" +
                "    .fseventsd; \n" +
                "      fseventsd-uuid; 36; eae15fa70d47f025bbfdc3d58af3dfa4\n" +
                "      0000000006c7ffbd; 95; 0b9bedacc74534867302e4dcd98fcfcb\n" +
                "      0000000006c7ffbe; 72; e9e1bbc20e18b28ca4b8f45ce14f21cb\n" +
                "    ._test.txt; 4096; 009b7e7f1db8e0f96e168fd5b0ab583f\n" +
                "    ._.Trashes; 4096; f9e90e04b2ae7c188a55c0eb0655f8eb\n";

        DataStructureAsserts.assertStructure(fs, expectedStructure);
    }
}
