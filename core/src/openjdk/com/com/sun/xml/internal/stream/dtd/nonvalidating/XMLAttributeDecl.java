/*
 * Portions Copyright 2005 Sun Microsystems, Inc.  All Rights Reserved.
 */

/*
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sun.xml.internal.stream.dtd.nonvalidating;

import com.sun.org.apache.xerces.internal.xni.QName;

/**
 * @version $Id: XMLAttributeDecl.java,v 1.3 2005/11/03 17:16:39 jeffsuttor Exp $
 */
public class XMLAttributeDecl {
    
    
    /** name */
    public final QName name = new QName();
    
    /** simpleType */
    public final XMLSimpleType simpleType = new XMLSimpleType();
    
    /** optional */
    public boolean optional;
    
    
    /**
     * setValues
     *
     * @param name
     * @param simpleType
     * @param optional
     */
    public void setValues(QName name, XMLSimpleType simpleType, boolean optional) {
        this.name.setValues(name);
        this.simpleType.setValues(simpleType);
        this.optional   = optional;
    }
    
    /**
     * clear
     */
    public void clear() {
        this.name.clear();
        this.simpleType.clear();
        this.optional   = false;
    }
    
}
