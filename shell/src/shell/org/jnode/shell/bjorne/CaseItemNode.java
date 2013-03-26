/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.shell.bjorne;

public class CaseItemNode {
    private final BjorneToken[] pattern;

    private final CommandNode body;

    public CaseItemNode(BjorneToken[] pattern, CommandNode body) {
        this.pattern = pattern;
        this.body = body;
    }

    public CommandNode getBody() {
        return body;
    }

    public BjorneToken[] getPattern() {
        return pattern;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CaseItem{");
        if (pattern != null) {
            sb.append("pattern=");
            CommandNode.appendArray(sb, pattern);
        }
        if (body != null) {
            if (pattern != null) {
                sb.append(",");
            }
            sb.append("body=").append(body);
        }
        sb.append("}");
        return sb.toString();
    }
}
