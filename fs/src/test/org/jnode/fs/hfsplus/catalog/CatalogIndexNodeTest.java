package org.jnode.fs.hfsplus.catalog;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;

import org.jnode.fs.hfsplus.HfsUnicodeString;
import org.jnode.fs.hfsplus.tree.IndexRecord;
import org.jnode.fs.hfsplus.tree.NodeDescriptor;
import org.junit.Before;
import org.junit.Test;

public class CatalogIndexNodeTest {
	
	private CatalogIndexNode node;
	
	@Before
	public void setUp() {
		NodeDescriptor descriptor = new NodeDescriptor(0, 0, 0, 1, 2);
		node = new CatalogIndexNode(descriptor, 4096);
	}

	@Test
	public void testAddNodeRecord() {
		IndexRecord index = new IndexRecord(getNodeData(),0);
		node.addNodeRecord(index);
		assertEquals(14,node.getRecordOffset(0));
		assertEquals(0,node.getNodeRecord(0).getKey().getKeyLength());
		assertEquals(123456,node.getNodeRecord(0).getIndex());
	}
	
	public byte[] getNodeData() {
		return ByteBuffer.allocate(18).putInt(17).putShort((short) 0).putInt(1).array();
	}

}
