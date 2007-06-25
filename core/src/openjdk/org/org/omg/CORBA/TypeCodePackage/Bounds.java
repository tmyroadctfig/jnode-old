/*
 * Copyright 1997-1999 Sun Microsystems, Inc.  All Rights Reserved.
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

package org.omg.CORBA.TypeCodePackage;

/**
 * Provides the <code>TypeCode</code> operations <code>member_name()</code>,
 * <code>member_type()</code>, and <code>member_label</code>.
 * These methods
 * raise <code>Bounds</code> when the index parameter is greater than or equal
 * to the number of members constituting the type.
 *
 * @version 1.7, 03/18/98
 * @since   JDK1.2
 */

public final class Bounds extends org.omg.CORBA.UserException {

    /**
     * Constructs a <code>Bounds</code> exception with no reason message.
     */
    public Bounds() {
	super();
    }

    /**
     * Constructs a <code>Bounds</code> exception with the specified 
     * reason message.
     * @param reason the String containing a reason message
     */
    public Bounds(String reason) {
	super(reason);
    }
}
