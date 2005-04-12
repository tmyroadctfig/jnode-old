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
 
package org.jnode.build;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import nanoxml.XMLElement;
import nanoxml.XMLParseException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.ZipFileSet;
import org.apache.tools.ant.util.FileUtils;
import org.jnode.plugin.Library;
import org.jnode.plugin.PluginException;
import org.jnode.plugin.Runtime;
import org.jnode.plugin.model.PluginDescriptorModel;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class PluginTask extends AbstractPluginTask {

	private LinkedList descriptorSets = new LinkedList();
	private File todir;
	private File tmpDir = new File(System.getProperty("java.io.tmpdir"));

	public ZipFileSet createDescriptors() {
		final ZipFileSet fs = new ZipFileSet();
		descriptorSets.add(fs);
		return fs;
	}

	/**
	 * @see org.apache.tools.ant.Task#execute()
	 * @throws BuildException
	 */
	public void execute() throws BuildException {

		if (descriptorSets.isEmpty()) {
			throw new BuildException("At at least 1 descriptorset element");
		}
		if (todir == null) {
			throw new BuildException("The todir attribute must be set");
		}
		if (getPluginDir() == null) {
			throw new BuildException("The pluginDir attribute must be set");
		}
		if (!todir.exists()) {
			todir.mkdirs();
		} else if (!todir.isDirectory()) {
			throw new BuildException("todir must be a directory");
		}

        Map descriptors = new HashMap();
		for (Iterator i = descriptorSets.iterator(); i.hasNext();) {
			final FileSet fs = (FileSet) i.next();
			final DirectoryScanner ds = fs.getDirectoryScanner(getProject());
			final String[] files = ds.getIncludedFiles();
			for (int j = 0; j < files.length; j++) {
				buildPlugin(descriptors, new File(ds.getBasedir(), files[j]));
			}
		}
    }

    /**
     * 
     * @param descriptors map of fullPluginId to File descriptor 
     * @param descriptor
     * @throws BuildException
     */
	protected void buildPlugin(Map descriptors, File descriptor) throws BuildException {
		final PluginDescriptorModel descr = readDescriptor(descriptor);

		final String fullId = descr.getId() + "_" + descr.getVersion();
        if(descriptors.containsKey(fullId))
        {
            File otherDesc = (File) descriptors.get(fullId);
            throw new BuildException("Same id("+fullId+") for 2 plugins: "+otherDesc+", "+descriptor);
        }        
        descriptors.put(fullId, descriptor);
        
		File destFile = new File(todir, fullId + ".jar");

		final Jar jarTask = new Jar();
		jarTask.setProject(getProject());
		jarTask.setTaskName(getTaskName());
		jarTask.setDestFile(destFile);
		jarTask.setCompress(false);

		// Add plugin.xml
		final File tmpPluginDir;
		final File tmpPluginXmlFile;
		try {
			tmpPluginDir = new File(tmpDir, "jnode-plugins" + File.separator + fullId);
			tmpPluginDir.mkdirs();
			tmpPluginXmlFile = new File(tmpPluginDir, "plugin.xml");
			FileUtils.newFileUtils().copyFile(descriptor, tmpPluginXmlFile);
			FileSet fs = new FileSet();
			fs.setDir(tmpPluginDir);
			fs.createInclude().setName("plugin.xml");
			jarTask.addFileset(fs);
		} catch (IOException ex) {
			throw new BuildException(ex);
		}

		// Add runtime resources
		final Runtime rt = descr.getRuntime();
		if (rt != null) {
			final HashMap fileSets = new HashMap();
			final Library[] libs = rt.getLibraries();
			for (int l = 0; l < libs.length; l++) {
				processLibrary(jarTask, libs[l], fileSets, getPluginDir());
			}
		}

		jarTask.execute();
	}

	/**
	 * @return The destination directory
	 */
	public final File getTodir() {
		return this.todir;
	}

	/**
	 * @param todir
	 */
	public final void setTodir(File todir) {
		this.todir = todir;
	}

	/**
	 * @return The temp directory
	 */
	public final File getTmpDir() {
		return this.tmpDir;
	}

	/**
	 * @param tmpDir
	 */
	public final void setTmpDir(File tmpDir) {
		this.tmpDir = tmpDir;
	}

}
