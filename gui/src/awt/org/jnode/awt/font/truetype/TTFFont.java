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
 
package org.jnode.awt.font.truetype;

import java.awt.Font;
import java.io.IOException;

/**
 * @author epr
 */
public class TTFFont extends Font {

	private final TTFFontData fontData;

	/**
	 * @param fontData
	 * @param size
	 * @throws IOException
	 */
	public TTFFont(TTFFontData fontData, int size) throws IOException {
		super(fontData.getNameTable().getFontFamilyName(), fontData.getStyle(), size);
		this.fontData = fontData;
	}

	/**
	 * @return The font data
	 */
	public final TTFFontData getFontData() {
		return this.fontData;
	}
	
	/**
	 * @param size
	 * @see java.awt.Font#deriveFont(float)
	 * @return The derived font
	 */
	public Font deriveFont(float size) {
		try {
			return new TTFFont(fontData, (int) size);
		} catch (IOException ex) {
			return super.deriveFont(size);
		}
	}
}
