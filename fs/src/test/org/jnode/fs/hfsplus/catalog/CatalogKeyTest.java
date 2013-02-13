/*
 * $Id: header.txt 5714 2010-01-03 13:33:07Z lsantha $
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
 
package org.jnode.fs.hfsplus.catalog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.ByteBuffer;

import org.jnode.fs.hfsplus.HfsUnicodeString;
import org.junit.Test;

public class CatalogKeyTest {
	byte[] EMPTY_KEY_AS_BYTES_ARRAY = ByteBuffer.allocate(6).putShort((short) 6).putInt(17).array();
	String NODE_NAME_AS_STRING = "test.txt";

	@Test
	public void fromBytesArrayWithEmptyName(){
		CatalogKey key = new CatalogKey(EMPTY_KEY_AS_BYTES_ARRAY,0);
		assertNotNull(key.getNodeName());
		assertEquals("",key.getNodeName().getUnicodeString());
		assertEquals(6,key.getKeyLength());
		assertEquals(17,key.getParentId().getId());
	}
	
	@Test
	public void fromBytesArray(){
		CatalogKey key = new CatalogKey(getKeyDatas(),0);
		assertEquals(NODE_NAME_AS_STRING,key.getNodeName().getUnicodeString());
		assertEquals(24,key.getKeyLength());
		assertEquals(17,key.getParentId().getId());
	}
	
	@Test
	public void fromCNIDAndString() {
		CatalogNodeId id = CatalogNodeId.HFSPLUS_START_CNID;
		HfsUnicodeString string = new HfsUnicodeString(NODE_NAME_AS_STRING);
		CatalogKey key = new CatalogKey(id,string);
		assertEquals(NODE_NAME_AS_STRING,key.getNodeName().getUnicodeString());
		assertEquals(24,key.getKeyLength());
		assertEquals(7,key.getParentId().getId());
		
	}

	@Test
	public void fromCNIDAndEmptyString() {
		CatalogNodeId id = CatalogNodeId.HFSPLUS_START_CNID;
		HfsUnicodeString string = new HfsUnicodeString("");
		CatalogKey key = new CatalogKey(id,string);
		assertEquals("",key.getNodeName().getUnicodeString());
		assertEquals(8,key.getKeyLength());
		assertEquals(7,key.getParentId().getId());
		
	}
	
	//
	
	private byte[] getKeyDatas() {
		char[] chars = NODE_NAME_AS_STRING.toCharArray();
		ByteBuffer buffer = ByteBuffer.allocate(24).putShort((short)24).putInt(17).putShort((short)8);
		for(char c : chars) {
			buffer.putChar(c);
		}
		return buffer.array();
	}
}
