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
 
package org.jnode.driver;

import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;

/**
 * Plugin start instantiates and initialize the default device manager.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class DriverPlugin extends Plugin {

    private DefaultDeviceManager devMgr;

    /**
     * @param descriptor
     */
    public DriverPlugin(PluginDescriptor descriptor) {
        super(descriptor);
    }

    /**
     * Start this plugin
     *
     * @throws PluginException
     */
    protected void startPlugin() throws PluginException {
        devMgr = new DefaultDeviceManager(getDescriptor().getExtensionPoint("finders"),
            getDescriptor().getExtensionPoint("mappers"));
        devMgr.start();
    }

    /**
     * Stop this plugin
     *
     * @throws PluginException
     */
    protected void stopPlugin() throws PluginException {
        devMgr.stop();
        devMgr = null;
    }
}
