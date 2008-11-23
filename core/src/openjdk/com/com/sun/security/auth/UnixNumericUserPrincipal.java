/*
 * Copyright 2000-2003 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.security.auth;

import java.security.Principal;

/**
 * <p> This class implements the <code>Principal</code> interface
 * and represents a user's Unix identification number (UID).
 *
 * <p> Principals such as this <code>UnixNumericUserPrincipal</code>
 * may be associated with a particular <code>Subject</code>
 * to augment that <code>Subject</code> with an additional
 * identity.  Refer to the <code>Subject</code> class for more information
 * on how to achieve this.  Authorization decisions can then be based upon 
 * the Principals associated with a <code>Subject</code>.
 * 
 * @see java.security.Principal
 * @see javax.security.auth.Subject
 */
public class UnixNumericUserPrincipal implements
					Principal,
					java.io.Serializable {
    private static final long serialVersionUID = -4329764253802397821L;

    /**
     * @serial
     */
    private String name;

    /**
     * Create a <code>UnixNumericUserPrincipal</code> using a
     * <code>String</code> representation of the
     * user's identification number (UID).
     *
     * <p>
     *
     * @param name the user identification number (UID) for this user.
     *
     * @exception NullPointerException if the <code>name</code>
     *			is <code>null</code>.
     */
    public UnixNumericUserPrincipal(String name) {
	if (name == null) {
	    java.text.MessageFormat form = new java.text.MessageFormat
		(sun.security.util.ResourcesMgr.getString
			("invalid null input: value",
			"sun.security.util.AuthResources"));
	    Object[] source = {"name"};
	    throw new NullPointerException(form.format(source));
	}

	this.name = name;
    }

    /**
     * Create a <code>UnixNumericUserPrincipal</code> using a
     * long representation of the user's identification number (UID).
     *
     * <p>
     *
     * @param name the user identification number (UID) for this user
     *			represented as a long.
     */
    public UnixNumericUserPrincipal(long name) {
	this.name = (new Long(name)).toString();
    }

    /**
     * Return the user identification number (UID) for this
     * <code>UnixNumericUserPrincipal</code>.
     *
     * <p>
     *
     * @return the user identification number (UID) for this
     *		<code>UnixNumericUserPrincipal</code>
     */
    public String getName() {
	return name;
    }

    /**
     * Return the user identification number (UID) for this
     * <code>UnixNumericUserPrincipal</code> as a long.
     *
     * <p>
     *
     * @return the user identification number (UID) for this
     *		<code>UnixNumericUserPrincipal</code> as a long.
     */
    public long longValue() {
	return ((new Long(name)).longValue());
    }

    /**
     * Return a string representation of this
     * <code>UnixNumericUserPrincipal</code>.
     *
     * <p>
     *
     * @return a string representation of this
     *		<code>UnixNumericUserPrincipal</code>.
     */
    public String toString() {
	java.text.MessageFormat form = new java.text.MessageFormat
		(sun.security.util.ResourcesMgr.getString
			("UnixNumericUserPrincipal: name",
			"sun.security.util.AuthResources"));
	Object[] source = {name};
	return form.format(source);
    }

    /**
     * Compares the specified Object with this
     * <code>UnixNumericUserPrincipal</code>
     * for equality.  Returns true if the given object is also a
     * <code>UnixNumericUserPrincipal</code> and the two
     * UnixNumericUserPrincipals
     * have the same user identification number (UID).
     *
     * <p>
     *
     * @param o Object to be compared for equality with this
     *		<code>UnixNumericUserPrincipal</code>.
     *
     * @return true if the specified Object is equal equal to this
     *		<code>UnixNumericUserPrincipal</code>.
     */
    public boolean equals(Object o) {
	if (o == null)
	    return false;

        if (this == o)
            return true;
 
        if (!(o instanceof UnixNumericUserPrincipal))
            return false;
        UnixNumericUserPrincipal that = (UnixNumericUserPrincipal)o;

	if (this.getName().equals(that.getName()))
	    return true;
	return false;
    }
 
    /**
     * Return a hash code for this <code>UnixNumericUserPrincipal</code>.
     *
     * <p>
     *
     * @return a hash code for this <code>UnixNumericUserPrincipal</code>.
     */
    public int hashCode() {
	return name.hashCode();
    }
}
