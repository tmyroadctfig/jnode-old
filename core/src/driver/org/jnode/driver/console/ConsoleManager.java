/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.driver.console;

import java.util.Set;

import org.jnode.driver.input.KeyboardListener;
import org.jnode.driver.input.PointerListener;

/**
 * @author epr
 */
public interface ConsoleManager extends KeyboardListener, PointerListener {

    /** The name used to bind this manager in the InitialNaming namespace. */
    public static final Class NAME = ConsoleManager.class;

    /**
     * Gets the console with the given index
     * 
     * @param name
     * @return The console
     */
    public Console getConsole(String name);

    /**
     * Gets the console with the given accelerator keycode.
     * @param keyCode
     * @return
     */
    public Console getConsoleByAccelerator(int keyCode);

    /**
     * Gets the names of all registers consoles.
     * @return
     */
    public Set/*<String>*/ getConsoleNames();

    /**
     * Register a new console.
     * @param console
     */
    public void registerConsole(Console console);

    /**
     * Remove an already registered console.
     * @param console
     */
    public void unregisterConsole(Console console);

    /**
     * Gets the currently focused console.
     * 
     * @return Console
     */
    public Console getFocus();

    /**
     * Focus the given console
     * 
     * @param console
     */
    public void focus(Console console);
    
    /**
     * Option constants for use in {@link #createConsole(String, int)}
     * @author Ewout Prangsma (epr@users.sourceforge.net)
     */
    public static final class CreateOptions {
        /** Create a text console */
        public static final int TEXT = 0x01;
        
        /** Create a scrollable console */
        public static final int SCROLLABLE = 0x02;      
        
        /** Do not claim System.out, err, in when focused */
        public static final int NO_SYSTEM_OUT_ERR_IN = 0x04;
    }
    
    /**
     * Create a new console.
     * @param name The name of the new console, or null for an automatic name.
     * @param options The options that determine the type of console to create.
     * @return
     */
    public Console createConsole(String name, int options);
}
