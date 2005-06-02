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

import java.awt.Dimension;
import java.awt.List;
import java.awt.peer.ListPeer;

import javax.swing.AbstractListModel;
import javax.swing.JList;
import javax.swing.ListModel;

/**
 * AWT list peer implemented as a {@link javax.swing.JList}.
 * @author Levente S\u00e1ntha
 */

final class SwingListPeer extends SwingComponentPeer<List, SwingList> implements ListPeer {

	//
	// Construction
	//

	public SwingListPeer(SwingToolkit toolkit, final List list) {
        super(toolkit, list, new SwingList(list));
        final JList jList = (JList)jComponent;
		SwingToolkit.add(list, jList);
		SwingToolkit.copyAwtProperties(list, jList);
		final ListModel model = new AbstractListModel() {
			public Object getElementAt(int idx) {
				return list.getItem(idx);
			}

			public int getSize() {
				return list.getItemCount();
			}
		};
	}

	public void add(String item, int index) {

	}

	// Deprecated

	public void addItem(String item, int index) {
		add(item, index);
	}

	public void clear() {
		removeAll();
	}

    public void removeAll() {

    }

	public int[] getSelectedIndexes() {
		return null;
	}

	public void makeVisible(int index) {
	}

	public Dimension minimumSize(int rows) {
		return getMinimumSize(rows);
	}

	public Dimension preferredSize(int rows) {
		return getPreferredSize(rows);
	}

	public void select(int index) {
	}

	public void setMultipleMode(boolean b) {
	}

	public void setMultipleSelections(boolean v) {
		setMultipleMode(v);
	}

    public void delItems(int start_index, int end_index) {

    }

    public void deselect(int index) {

    }

    public Dimension getMinimumSize(int s) {
        return getMinimumSize();
    }

    public Dimension getPreferredSize(int s) {
        return getPreferredSize();
    }
    

}

final class SwingList extends JList implements ISwingPeer<List> {
    private final List awtComponent;

    public SwingList(List awtComponent) {
        this.awtComponent = awtComponent;
    }

    /**
     * @see org.jnode.awt.swingpeers.ISwingPeer#getAWTComponent()
     */
    public List getAWTComponent() {
        return awtComponent;
    }
}

