/*
 * $Id: Command.java 3772 2008-02-10 15:02:53Z lsantha $
 *
 * JNode.org
 * Copyright (C) 2007-2008 JNode.org
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
        StringBuffer sb = new StringBuffer();
        sb.append("CaseItem{");
        if (pattern != null) {
            sb.append(",pattern=");
            CommandNode.appendArray(sb, pattern);
        }
        if (body != null) {
            sb.append(",body=").append(body);
        }
        sb.append("}");
        return sb.toString();
    }
}
