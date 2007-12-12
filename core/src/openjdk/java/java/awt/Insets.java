/*
 * Copyright 1995-2003 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package java.awt;

/**
 * An <code>Insets</code> object is a representation of the borders 
 * of a container. It specifies the space that a container must leave 
 * at each of its edges. The space can be a border, a blank space, or 
 * a title. 
 *
 * @version 	1.37, 05/05/07
 * @author 	Arthur van Hoff
 * @author 	Sami Shaio
 * @see         java.awt.LayoutManager
 * @see         java.awt.Container
 * @since       JDK1.0
 */
public class Insets implements Cloneable, java.io.Serializable {

    /**
     * The inset from the top.
     * This value is added to the Top of the rectangle
     * to yield a new location for the Top.
     *
     * @serial
     * @see #clone()
     */
    public int top;

    /**
     * The inset from the left.
     * This value is added to the Left of the rectangle
     * to yield a new location for the Left edge.
     *
     * @serial
     * @see #clone()
     */
    public int left;

    /**
     * The inset from the bottom.
     * This value is subtracted from the Bottom of the rectangle
     * to yield a new location for the Bottom.
     *
     * @serial
     * @see #clone()
     */
    public int bottom;

    /**
     * The inset from the right.
     * This value is subtracted from the Right of the rectangle
     * to yield a new location for the Right edge.
     *
     * @serial
     * @see #clone()
     */
    public int right;

    /*
     * JDK 1.1 serialVersionUID 
     */
    private static final long serialVersionUID = -2272572637695466749L;
  
    static {
        /* ensure that the necessary native libraries are loaded */
	Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }
    }

    /**
     * Creates and initializes a new <code>Insets</code> object with the 
     * specified top, left, bottom, and right insets. 
     * @param       top   the inset from the top.
     * @param       left   the inset from the left.
     * @param       bottom   the inset from the bottom.
     * @param       right   the inset from the right.
     */
    public Insets(int top, int left, int bottom, int right) {
	this.top = top;
	this.left = left;
	this.bottom = bottom;
	this.right = right;
    }

    /**
     * Set top, left, bottom, and right to the specified values
     *
     * @param       top   the inset from the top.
     * @param       left   the inset from the left.
     * @param       bottom   the inset from the bottom.
     * @param       right   the inset from the right.
     * @since 1.5
     */
    public void set(int top, int left, int bottom, int right) {
        this.top = top;
        this.left = left;
        this.bottom = bottom;
        this.right = right;
    }

    /**
     * Checks whether two insets objects are equal. Two instances 
     * of <code>Insets</code> are equal if the four integer values
     * of the fields <code>top</code>, <code>left</code>, 
     * <code>bottom</code>, and <code>right</code> are all equal.
     * @return      <code>true</code> if the two insets are equal;
     *                          otherwise <code>false</code>.
     * @since       JDK1.1
     */
    public boolean equals(Object obj) {
	if (obj instanceof Insets) {
	    Insets insets = (Insets)obj;
	    return ((top == insets.top) && (left == insets.left) &&
		    (bottom == insets.bottom) && (right == insets.right));
	}
	return false;
    }

    /**
     * Returns the hash code for this Insets.
     *
     * @return    a hash code for this Insets.
     */
    public int hashCode() {
        int sum1 = left + bottom;
        int sum2 = right + top;
        int val1 = sum1 * (sum1 + 1)/2 + left;
        int val2 = sum2 * (sum2 + 1)/2 + top;
        int sum3 = val1 + val2;
        return sum3 * (sum3 + 1)/2 + val2;
    }

    /**
     * Returns a string representation of this <code>Insets</code> object. 
     * This method is intended to be used only for debugging purposes, and 
     * the content and format of the returned string may vary between 
     * implementations. The returned string may be empty but may not be 
     * <code>null</code>.
     * 
     * @return  a string representation of this <code>Insets</code> object.
     */
    public String toString() {
	return getClass().getName() + "[top="  + top + ",left=" + left + ",bottom=" + bottom + ",right=" + right + "]";
    }

    /**
     * Create a copy of this object.
     * @return     a copy of this <code>Insets</code> object.
     */
    public Object clone() { 
	try { 
	    return super.clone();
	} catch (CloneNotSupportedException e) { 
	    // this shouldn't happen, since we are Cloneable
	    throw new InternalError();
	}
    }
    /**
     * Initialize JNI field and method IDs
     */
    private static native void initIDs();

}
