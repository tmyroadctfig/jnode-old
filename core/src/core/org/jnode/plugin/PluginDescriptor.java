/*
 * $Id$
 */
package org.jnode.plugin;

/**
 * Descriptor of a Plugin.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface PluginDescriptor {
	
	/**
	 * Gets the unique identifier of this plugin
	 * @return the unique id
	 */
	public abstract String getId();
	
	/**
	 * Gets the human readable name of this plugin
	 * @return The name
	 */
	public abstract String getName();
	
	/**
	 * Gets the name of the provider of this plugin
	 * @return The name of the provider
	 */
	public abstract String getProviderName();
	
	/**
	 * Gets the version of this plugin
	 * @return The version
	 */
	public abstract String getVersion();
	
	/**
	 * Gets the required imports
	 * @return The required imports
	 */
	public abstract PluginPrerequisite[] getPrerequisites();
	
	/**
	 * Gets all extension-points provided by this plugin
	 * @return The provided extension-points
	 */
	public abstract ExtensionPoint[] getExtensionPoints();
	
	/**
	 * Returns the extension point with the given simple identifier
	 * declared in this plug-in, or null if there is no such extension 
	 * point. 
	 * @param extensionPointId the simple identifier of the extension point (e.g. "wizard"). 
	 * @return the extension point, or null
	 */
	public abstract ExtensionPoint getExtensionPoint(String extensionPointId);
	
	/**
	 * Gets all extensions provided by this plugin
	 * @return The provided extensions
	 */
	public abstract Extension[] getExtensions();
	
	/**
	 * Gets the runtime information of this descriptor.
	 * @return The runtime, or null if no runtime information is provided.
	 */
	public Runtime getRuntime();

	/**
	 * Gets the registry this plugin is declared in.
	 * @return The registry
	 */
	public PluginRegistry getPluginRegistry();
	
	/**
	 * Gets the plugin that is described by this descriptor.
	 * If no plugin class is given in the descriptor, an empty
	 * plugin is returned.
	 * This method will always returns the same plugin instance for a given
	 * descriptor.
	 * @return The plugin
	 * @throws PluginException
	 */
	public Plugin getPlugin()
	throws PluginException;
	
	/**
	 * Is this a descriptor of a system plugin.
	 * System plugins are not reloadable.
	 * @return boolean
	 */
	public boolean isSystemPlugin();
	
	/**
	 * Gets the classloader of this plugin descriptor.
	 * @return ClassLoader
	 */
	public ClassLoader getPluginClassLoader();
}