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

package org.jnode.test.support;

import java.net.URL;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.cmos.CMOSConstants;
import org.jnode.driver.cmos.CMOSService;
import org.jnode.driver.floppy.support.DefaultFloppyDeviceFactory;
import org.jnode.driver.floppy.support.FloppyDeviceFactory;
import org.jnode.driver.ide.IDEDeviceFactory;
import org.jnode.naming.InitialNaming;
import org.jnode.naming.InitialNaming.NameSpace;
import org.jnode.plugin.PluginException;
import org.jnode.system.BootLog;
import org.jnode.test.fs.unit.config.OsType;
import org.jnode.test.fs.unit.config.StubNameSpace;
import org.jnode.test.fs.unit.config.factories.MockFloppyDeviceFactory;
import org.jnode.test.fs.unit.config.factories.MockIDEDeviceFactory;
import org.jnode.test.fs.unit.stubs.StubDeviceManager;


public class ContextManager
{   
    private static final Logger log = Logger.getLogger(ContextManager.class);    
    private static ContextManager instance;
    
    private boolean initialized = false; 
    
    static public ContextManager getInstance()
    {
        if(instance == null)
        {
            instance = new ContextManager();
        }
        
        return instance;
    }
        
    public void init()
    {
        if(!initialized)
        {
            try
            {                
                initLog4j();
                initNaming();
            }
            catch (PluginException e)
            {
                log.fatal("error in initNaming", e);
            }
            
            initialized = true;
        }
    }
    
    protected void initLog4j()
    {
        if(OsType.OTHER_OS.isCurrentOS())
        {
            // configure Log4j only if outside of JNode
            // (because JNode has its own config for Log4j)
            
            // name must be of max 8 characters !!!
            // but extension can be larger that 3 characters !!!!!
            // (probably only under windows)
            String configLog4j = "log4jCfg.properties";
            
            URL url = ContextManager.class.getResource(configLog4j);
            if(url == null)
            {
                System.err.println("can't find resource "+configLog4j);
            }
            else
            {
                PropertyConfigurator.configure(url);
            }
        }        
    }
    
    protected void initNaming() throws PluginException
    {
        if(OsType.OTHER_OS.isCurrentOS())
        {   
            NameSpace namespace = new StubNameSpace();
            InitialNaming.setNameSpace(namespace);
            populateNameSpace(namespace);
            
            //StubDeviceManager.INSTANCE.start();
        }        
    }
    
    protected void populateNameSpace(NameSpace namespace)
    {
        try
        {
            namespace.bind(FloppyDeviceFactory.NAME, new MockFloppyDeviceFactory());
            namespace.bind(IDEDeviceFactory.NAME, new MockIDEDeviceFactory());
            namespace.bind(DeviceManager.NAME, StubDeviceManager.INSTANCE);
            
            CMOSService cmos = new CMOSService()
            {
                public int getRegister(int regnr)
                {
                    switch(regnr)
                    {
                    case CMOSConstants.CMOS_FLOPPY_DRIVES: return 0x11;
                    default: return 0;
                    }
                }
                
            };
            namespace.bind(CMOSService.NAME, cmos);                        
        }
        catch (NameAlreadyBoundException e)
        {
            log.fatal("can't register stub services", e);
        }
        catch (NamingException e)
        {
            log.fatal("can't register stub services", e);
        }        
    }
    
    private ContextManager()
    {        
        initLog4j();
    }        
}
