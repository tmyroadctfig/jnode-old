/*
 * Copyright 2003 Sun Microsystems, Inc.  All Rights Reserved.
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
package sun.swing;

import java.util.*;

/**
 * <b>WARNING:</b> This class is an implementation detail and is only
 * public so that it can be used by two packages. You should NOT consider
 * this public API.
 * <p>
 * <b>WARNING 2:</b> This is not a general purpose List implementation! It
 * has a specific use and will not work correctly if you use it outside of
 * its use.
 * <p>
 * A specialized ArrayList that caches its hashCode as well as overriding
 * equals to avoid creating an Iterator. This class is useful in scenarios
 * where the list won't change and you want to avoid the overhead of hashCode
 * iterating through the elements invoking hashCode. This also assumes you'll
 * only ever compare a BakedArrayList to another BakedArrayList.
 * 
 * @version 1.10, 05/05/07
 * @author Scott Violet
 */
public class BakedArrayList extends ArrayList {
    /**
     * The cached hashCode.
     */
    private int _hashCode;

    public BakedArrayList(int size) {
        super(size);
    }

    public BakedArrayList(java.util.List data) {
        this(data.size());
        for (int counter = 0, max = data.size(); counter < max; counter++){
            add(data.get(counter));
        }
        cacheHashCode();
    }

    /**
     * Caches the hash code. It is assumed you won't modify the list, or that
     * if you do you'll call cacheHashCode again.
     */
    public void cacheHashCode() {
        _hashCode = 1;
        for (int counter = size() - 1; counter >= 0; counter--) {
            _hashCode = 31 * _hashCode + get(counter).hashCode();
        }
    }

    public int hashCode() {
        return _hashCode;
    }

    public boolean equals(Object o) {
        BakedArrayList list = (BakedArrayList)o;
        int size = size();

        if (list.size() != size) {
            return false;
        }
        while (size-- > 0) {
            if (!get(size).equals(list.get(size))) {
                return false;
            }
        }
        return true;
    }
}
