/*
 * $Id$
 */
package org.jnode.vm.x86.compiler;

import org.jnode.assembler.Label;
import org.jnode.assembler.NativeStream;
import org.jnode.assembler.ObjectResolver;
import org.jnode.assembler.x86.AbstractX86Stream;
import org.jnode.assembler.x86.X86Stream;
import org.jnode.vm.Address;
import org.jnode.vm.classmgr.AbstractVmClassLoader;
import org.jnode.vm.classmgr.VmCompiledCode;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.compiler.CompiledMethod;
import org.jnode.vm.compiler.NativeCodeCompiler;

/**
 * Abstract native code compiler for the Intel X86 architecture.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class AbstractX86Compiler extends NativeCodeCompiler implements X86CompilerConstants {

	private X86CompilerContext context;

	/**
	 * Initialize this compiler
	 * 
	 * @param loader
	 */
	public final void initialize(AbstractVmClassLoader loader) {
		if (context == null) {
			context = new X86CompilerContext(loader);
		}
	}

	/**
	 * @see org.jnode.vm.compiler.Compiler#createNativeStream(org.jnode.assembler.ObjectResolver)
	 */
	public NativeStream createNativeStream(ObjectResolver resolver) {
		final X86Stream os = new X86Stream(0);
		os.setResolver(resolver);
		return os;
	}

	/**
	 * @see org.jnode.vm.compiler.Compiler#doCompileAbstract(org.jnode.vm.classmgr.VmMethod,
	 *      org.jnode.assembler.NativeStream, int, boolean)
	 */
	protected final CompiledMethod doCompileAbstract(VmMethod method, NativeStream nos, int level, boolean isBootstrap) {
		if (isBootstrap) {
			//System.out.println("Abstraxct method " + method);
			final CompiledMethod cm = new CompiledMethod(level);
			final AbstractX86Stream os = (AbstractX86Stream) nos;
			// Create the helper
			final X86CompilerHelper helper = new X86CompilerHelper(os, context, isBootstrap);
			// Start an "object"
			final NativeStream.ObjectInfo objectInfo = os.startObject(context.getVmMethodCodeClass());
			// Start the code creation
			cm.setCodeStart(os.setObjectRef(new Label(method.getMangledName() + "$$abstract-start")));
			// Call abstract method error method
			helper.writeJumpTableJMP(X86JumpTable.VM_INVOKE_ABSTRACT_OFS);
			// Close the "object"
			objectInfo.markEnd();
			// The end
			cm.setCodeEnd(os.setObjectRef(new Label(method.getMangledName() + "$$abstract-end")));

			return cm;
		} else {
			// Set the address of the abstract method code
			final Address errorAddr = X86JumpTable.getJumpTableEntry(X86JumpTable.VM_INVOKE_ABSTRACT_OFS);
			final VmCompiledCode code = new VmCompiledCode(null, errorAddr, null, 0, null, null, null);
			method.setCompiledCode(code, level);
			return null;
		}
	}

	/**
	 * @return Returns the context.
	 */
	protected final X86CompilerContext getContext() {
		return this.context;
	}
}
