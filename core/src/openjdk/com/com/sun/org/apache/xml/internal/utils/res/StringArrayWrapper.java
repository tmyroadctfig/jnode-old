/*
 * reserved comment block
 * DO NOT REMOVE OR ALTER!
 */
/*
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * $Id: StringArrayWrapper.java,v 1.1.4.1 2005/09/08 11:39:33 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.utils.res;

/**
 *
 * It is a mutable object to wrap the String[] used in
 * the contents of the XResourceBundle class
 */
public class StringArrayWrapper {
    private String[] m_string;
    
    public StringArrayWrapper(String[] arg){
        m_string = arg;
    }
    
    public String getString(int index){
        return m_string[index];
    }
    
    public int getLength(){
        return m_string.length;
    }
}

