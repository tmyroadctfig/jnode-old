/*
 * $Id$
 */
package org.jnode.vm.classmgr;

import org.jnode.vm.VmSystemObject;

/**
 * @author epr
 */
public class VmLineNumberMap extends VmSystemObject {

	/** Number of line number info's in this object */
	private final int noLineNrs;
	/**
	 * Line number table.
	 * The line-number table should be interpreted as follows:
	 * Each line number entry consists of two entries in the following
	 * array.
	 * Entry i*2+0 = start_pc
	 * Entry i*2+1 = line_number
	 * So lnTable.length == noLineNrs*2
	 */
	private final char[] lnTable;

	public static final int LNT_STARTPC_OFS = 0;
	public static final int LNT_LINENR_OFS = 1;
	public static final int LNT_ELEMSIZE = 2;

	public VmLineNumberMap(char[] lnTable) {
		this.lnTable = lnTable;
		this.noLineNrs = (lnTable == null) ? 0 : (lnTable.length / LNT_ELEMSIZE);
	}

	/**
	 * Gets the number of entries of this table
	 * @return length of the table
	 */
	public int getLength() {
		return noLineNrs;
	}

	/**
	 * Gets the start PC field of an entry in this table with a given index.
	 * @param index
	 * @return int
	 */
	public int getStartPCAt(int index) {
		return lnTable[index * LNT_ELEMSIZE + LNT_STARTPC_OFS];
	}

	/**
	 * Gets the line number field of an entry in this table with a given index.
	 * @param index
	 * @return int
	 */
	public int getLineNrAt(int index) {
		return lnTable[index * LNT_ELEMSIZE + LNT_LINENR_OFS];
	}
	
	/**
	 * Gets the linenumber for a given PC.
	 * @param pc
	 * @return The line number
	 */
	public int findLineNr(int pc) {
		int ln = 0;
		final int len = noLineNrs * LNT_ELEMSIZE;
		for (int i = 0; i < len; i += LNT_ELEMSIZE) {
			final char startPC = lnTable[i + LNT_STARTPC_OFS];
			final char lineNr = lnTable[i + LNT_LINENR_OFS];
			if (startPC <= pc) {
				ln = lineNr;
			} else {
				return ln;
			}
		}
		return ln;
	}

}
