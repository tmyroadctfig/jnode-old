/*
 * $Id$
 */
package org.jnode.vm;

import java.util.HashMap;

import org.jnode.vm.classmgr.SelectorMap;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmStatics;
import org.jnode.vm.classmgr.VmType;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VmJavaClassLoader extends VmAbstractClassLoader {

	/** The java classloader */
	private final ClassLoader loader;
	/** The system classloader */
	private final VmSystemClassLoader systemLoader;
	/** The loaded classes (nbame, VmType) */
	private final HashMap loadedClasses = new HashMap();

	/**
	 * Initialize this class.
	 * 
	 * @param loader
	 */
	public VmJavaClassLoader(ClassLoader loader) {
		this.loader = loader;
		this.systemLoader = VmSystem.getSystemClassLoader();
	}

	/**
	 * @see org.jnode.vm.classmgr.VmClassLoader#asClassLoader()
	 */
	public final ClassLoader asClassLoader() {
		return loader;
	}

	/**
	 * @see org.jnode.vm.classmgr.VmClassLoader#compileRuntime(org.jnode.vm.classmgr.VmMethod, int)
	 */
	public final void compileRuntime(VmMethod vmMethod, int optLevel) {
		systemLoader.compileRuntime(vmMethod, optLevel);
	}

	/**
	 * @see org.jnode.vm.classmgr.VmClassLoader#findLoadedClass(java.lang.String)
	 */
	public final VmType findLoadedClass(String className) {
		return (VmType) loadedClasses.get(className);
	}

	/**
	 * Add a class that has been loaded.
	 * 
	 * @param name
	 * @param cls
	 */
	public final void addLoadedClass(String name, VmType cls) {
		loadedClasses.put(name, cls);
	}

	/**
	 * @see org.jnode.vm.classmgr.VmClassLoader#getArchitecture()
	 */
	public final VmArchitecture getArchitecture() {
		return systemLoader.getArchitecture();
	}

	/**
	 * @see org.jnode.vm.classmgr.VmClassLoader#isCompileRequired()
	 */
	public final boolean isCompileRequired() {
		return systemLoader.isCompileRequired();
	}

	/**
	 * @see org.jnode.vm.classmgr.VmClassLoader#loadClass(java.lang.String, boolean)
	 */
	public final VmType loadClass(String className, boolean resolve) throws ClassNotFoundException {
		final VmType cls;
		if (className.charAt(0) == '[') {
			cls = loadArrayClass(className, resolve);
			addLoadedClass(className, cls);
		} else {
			cls = loader.loadClass(className).getVmClass();
		}
		cls.link();
		return cls;
	}

	/**
	 * @see org.jnode.vm.classmgr.VmClassLoader#isSystemClassLoader()
	 */
	public final boolean isSystemClassLoader() {
		return false;
	}

	/**
	 * @see org.jnode.vm.VmAbstractClassLoader#getSelectorMap()
	 */
	protected final SelectorMap getSelectorMap() {
		return systemLoader.getSelectorMap();
	}

	/**
	 * @see org.jnode.vm.VmAbstractClassLoader#getStatics()
	 */
	protected final VmStatics getStatics() {
		return systemLoader.getStatics();
	}

	/**
	 * @see org.jnode.vm.classmgr.VmClassLoader#resourceExists(java.lang.String)
	 */
	public final boolean resourceExists(String resName) {
		return false;
	}

}
