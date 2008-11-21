/*
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

/*
 * This file is available under and governed by the GNU General Public
 * License version 2 only, as published by the Free Software Foundation.
 * However, the following notice accompanied the original version of this
 * file and, per its terms, should not be removed:
 *
 * Copyright (c) 2000 World Wide Web Consortium,
 * (Massachusetts Institute of Technology, Institut National de
 * Recherche en Informatique et en Automatique, Keio University). All
 * Rights Reserved. This program is distributed under the W3C's Software
 * Intellectual Property License. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See W3C License http://www.w3.org/Consortium/Legal/ for more
 * details.
 */

package org.w3c.dom.html;

import org.w3c.dom.Element;

/**
 *  All HTML element interfaces derive from this class. Elements that only 
 * expose the HTML core attributes are represented by the base 
 * <code>HTMLElement</code> interface. These elements are as follows:  HEAD 
 * special: SUB, SUP, SPAN, BDO font: TT, I, B, U, S, STRIKE, BIG, SMALL 
 * phrase: EM, STRONG, DFN, CODE, SAMP, KBD, VAR, CITE, ACRONYM, ABBR list: 
 * DD, DT NOFRAMES, NOSCRIPT ADDRESS, CENTER The <code>style</code> attribute 
 * of an HTML element is accessible through the 
 * <code>ElementCSSInlineStyle</code> interface which is defined in the  . 
 * <p>See also the <a href='http://www.w3.org/TR/2000/CR-DOM-Level-2-20000510'>Document Object Model (DOM) Level 2 Specification</a>.
 */
public interface HTMLElement extends Element {
    /**
     *  The element's identifier. See the  id attribute definition in HTML 4.0.
     */
    public String getId();
    public void setId(String id);

    /**
     *  The element's advisory title. See the  title attribute definition in 
     * HTML 4.0.
     */
    public String getTitle();
    public void setTitle(String title);

    /**
     *  Language code defined in RFC 1766. See the  lang attribute definition 
     * in HTML 4.0.
     */
    public String getLang();
    public void setLang(String lang);

    /**
     *  Specifies the base direction of directionally neutral text and the 
     * directionality of tables. See the  dir attribute definition in HTML 
     * 4.0.
     */
    public String getDir();
    public void setDir(String dir);

    /**
     *  The class attribute of the element. This attribute has been renamed 
     * due to conflicts with the "class" keyword exposed by many languages. 
     * See the  class attribute definition in HTML 4.0.
     */
    public String getClassName();
    public void setClassName(String className);

}
