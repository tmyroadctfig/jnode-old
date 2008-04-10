/*
 * $Id$
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
package org.jnode.shell.syntax;

import java.util.ArrayList;
import java.util.List;

/**
 * The SyntaxSpecLoader traverses a syntax specification (e.g. in XML) wrapped
 * in an adapter, and creates a Syntax tree.
 * 
 * @author crawley@jnode.org
 */
public class SyntaxSpecLoader {

    public Syntax loadSyntax(SyntaxSpecAdapter element) {
        int nos = element.getNosChildren();
        List<Syntax> childSyntaxes = new ArrayList<Syntax>(nos);
        for (int i = 0; i < nos; i++) {
            childSyntaxes.add(loadSyntax(element.getChild(i), false));
        }
        int nosSyntaxes = childSyntaxes.size();
        if (nosSyntaxes == 0) {
            return new EmptySyntax(null, null);
        }
        else if (nosSyntaxes == 1) {
            return childSyntaxes.get(0);
        }
        else {
            return new AlternativesSyntax(childSyntaxes.toArray(new Syntax[nosSyntaxes]));
        }
    }
    
    public Syntax loadSyntax(SyntaxSpecAdapter syntaxElement, boolean nullOK) 
    throws SyntaxFailureException, IllegalArgumentException {
        String label = syntaxElement.getAttribute("label");
        String description = syntaxElement.getAttribute("description");
        String kind = syntaxElement.getName();
        if (kind.equals("empty")) {
            if (nullOK && description == null && label == null) {
                return null;
            }
            else {
                return new EmptySyntax(label, description);
            }
        }
        else if (kind.equals("alternatives")) {
            int nos = syntaxElement.getNosChildren();
            Syntax[] alts = new Syntax[nos];
            for (int i = 0; i < nos; i++) {
                alts[i] = loadSyntax(syntaxElement.getChild(i), true);
            }
            return new AlternativesSyntax(label, description, alts);
        }
        else if (kind.equals("optionSet")) {
            int nos = syntaxElement.getNosChildren();
            OptionSyntax[] options = new OptionSyntax[nos];
            for (int i = 0; i < nos; i++) {
                try {
                    options[i] = (OptionSyntax) loadSyntax(syntaxElement.getChild(i), false);
                }
                catch (ClassCastException ex) {
                    throw new SyntaxFailureException(
                            "<optionSyntax> element can only contain <option> elements");
                }
            }
            return new OptionSetSyntax(label, description, options);
        }
        else if (kind.equals("option")) {
            String argLabel = syntaxElement.getAttribute("argLabel");
            if (argLabel == null) {
                throw new SyntaxFailureException("<option> element has no 'argLabel' attribute");
            }
            String shortName = syntaxElement.getAttribute("shortName");
            String longName = syntaxElement.getAttribute("longName");
            if (shortName == null) {
                if (longName == null) {
                    throw new SyntaxFailureException(
                    "<option> element has must have a 'shortName' or 'longName' attribute");
                }
                return new OptionSyntax(argLabel, longName, description);
            }
            else {
                if (shortName.length() != 1) {
                    throw new SyntaxFailureException(
                    "<option> elements 'shortName' attribute must be one character long");
                }
                if (longName == null) {
                    return new OptionSyntax(argLabel, shortName.charAt(0), description);
                }
                else {
                    return new OptionSyntax(argLabel, longName, shortName.charAt(0), description);
                }
            }
        }
        else if (kind.equals("powerset")) {
            int nos = syntaxElement.getNosChildren();
            Syntax[] members = new Syntax[nos];
            for (int i = 0; i < nos; i++) {
                members[i] = loadSyntax(syntaxElement.getChild(i), false);
            }
            return new PowersetSyntax(label, description, members);
        }
        else if (kind.equals("repeat")) {
            int nos = syntaxElement.getNosChildren();
            int minCount = getCount(syntaxElement, "minCount", 0);
            int maxCount = getCount(syntaxElement, "maxCount", Integer.MAX_VALUE);
            Syntax[] members = new Syntax[nos];
            for (int i = 0; i < nos; i++) {
                members[i] = loadSyntax(syntaxElement.getChild(i), false);
            }
            Syntax childSyntax = (members.length == 1) ?
                members[0] : new SequenceSyntax(members);
            return new RepeatSyntax(label, childSyntax, minCount, maxCount, description);
        }
        else if (kind.equals("sequence")) {
            int nos = syntaxElement.getNosChildren();
            Syntax[] seq = new OptionSyntax[nos];
            for (int i = 0; i < nos; i++) {
                seq[i] = loadSyntax(syntaxElement.getChild(i), false);
            }
            return new SequenceSyntax(label, description, seq);
        }
        else if (kind.equals("argument")) {
            String argLabel = syntaxElement.getAttribute("argLabel");
            if (argLabel == null) {
                System.out.println(syntaxElement);
                throw new SyntaxFailureException("<argument> element has no 'argLabel' attribute");
            }
            return new ArgumentSyntax(label, argLabel, description);
        }
        else if (kind.equals("symbol")) {
            String symbol = syntaxElement.getAttribute("symbol");
            if (symbol == null) {
                throw new SyntaxFailureException(
                        "<symbol> element has no 'symbol' attribute");
            }
            return new TokenSyntax(label, symbol, description);
        }
        else {
            throw new SyntaxFailureException(
                    "<" + kind + "> element does not represent a known syntax");
        }
    }
    
    private int getCount(SyntaxSpecAdapter element, String  name, int defaultValue) {
        String tmp = element.getAttribute(name);
        if (tmp == null) {
            return defaultValue;
        }
        else {
            try {
                return Integer.parseInt(tmp);
            }
            catch (NumberFormatException ex) {
                throw new SyntaxFailureException(
                        "'" + name + "' attribute is not an integer");
            }
        }
    }
}
