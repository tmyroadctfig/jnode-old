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
 
package org.jnode.awt.swingpeers;

import javax.swing.text.JTextComponent;
import java.awt.Rectangle;
import java.awt.TextComponent;
import java.awt.peer.TextComponentPeer;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class SwingTextComponentPeer extends SwingComponentPeer implements
		TextComponentPeer {

	public SwingTextComponentPeer(SwingToolkit toolkit,
			TextComponent textComponent, JTextComponent peer) {
		super(toolkit, textComponent, peer);

		SwingToolkit.add(textComponent, jComponent);
		SwingToolkit.copyAwtProperties(textComponent, jComponent);
		setText(textComponent.getText());
		setEditable(textComponent.isEditable());
	}

	/**
	 * @see java.awt.peer.TextComponentPeer#filterEvents(long)
	 */
	public long filterEvents(long filter) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see java.awt.peer.TextComponentPeer#getCaretPosition()
	 */
	public int getCaretPosition() {
		return ((JTextComponent) jComponent).getCaretPosition();
	}

	/**
	 * @see java.awt.peer.TextComponentPeer#getCharacterBounds(int)
	 */
	public Rectangle getCharacterBounds(int pos) {
		// TODO implement me
		return null;
	}

	/**
	 * @see java.awt.peer.TextComponentPeer#getIndexAtPoint(int, int)
	 */
	public int getIndexAtPoint(int x, int y) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see java.awt.peer.TextComponentPeer#getSelectionEnd()
	 */
	public int getSelectionEnd() {
		return ((JTextComponent) jComponent).getSelectionEnd();
	}

	/**
	 * @see java.awt.peer.TextComponentPeer#getSelectionStart()
	 */
	public int getSelectionStart() {
		return ((JTextComponent) jComponent).getSelectionStart();
	}

	/**
	 * @see java.awt.peer.TextComponentPeer#getText()
	 */
	public String getText() {
		return ((JTextComponent) jComponent).getText();
	}

	/**
	 * @see java.awt.peer.TextComponentPeer#select(int, int)
	 */
	public void select(int start_pos, int end_pos) {
		((JTextComponent) jComponent).select(start_pos, end_pos);
	}

	/**
	 * @see java.awt.peer.TextComponentPeer#setCaretPosition(int)
	 */
	public void setCaretPosition(int pos) {
		((JTextComponent) jComponent).setCaretPosition(pos);
	}

	/**
	 * @see java.awt.peer.TextComponentPeer#setEditable(boolean)
	 */
	public void setEditable(boolean editable) {
		((JTextComponent) jComponent).setEditable(editable);
	}

	/**
	 * @see java.awt.peer.TextComponentPeer#setText(java.lang.String)
	 */
	public void setText(String text) {
		((JTextComponent) jComponent).setText(text);
	}
}
