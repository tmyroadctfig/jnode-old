/*
 * reserved comment block
 * DO NOT REMOVE OR ALTER!
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

package com.sun.org.apache.xerces.internal.jaxp.validation;

import com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;

/**
 * <p>A container for grammar pools which only contain schema grammars.</p>
 * 
 * @author Michael Glavassevich, IBM
 * @version $Id: XSGrammarPoolContainer.java,v 1.1.4.1 2005/09/05 11:49:36 sunithareddy Exp $
 */
public interface XSGrammarPoolContainer {
    
    /**
     * <p>Returns the grammar pool contained inside the container.</p>
     * 
     * @return the grammar pool contained inside the container
     */
    public XMLGrammarPool getGrammarPool();
    
    /**
     * <p>Returns whether the schema components contained in this object
     * can be considered to be a fully composed schema and should be
     * used to the exclusion of other schema components which may be
     * present elsewhere.</p>
     * 
     * @return whether the schema components contained in this object
     * can be considered to be a fully composed schema
     */
    public boolean isFullyComposed();
    
}
