/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.x86.AbstractX86Stream;
import org.jnode.assembler.x86.Register;
import org.jnode.vm.JvmType;

/**
 * @author Patrik Reali
 */
final class FloatItem extends WordItem {

	private float value;

	/**
	 * Initialize a blank item.
	 */
	FloatItem(ItemFactory factory) {
	    super(factory);
	}
	
	/**
	 * @param kind
	 * @param offsetToFP
	 * @param value
	 */
	final void initialize(int kind, int offsetToFP, Register reg, float value) {
		super.initialize(kind, reg, offsetToFP);
		this.value = value;
	}

	/**
	 * @see org.jnode.vm.x86.compiler.l1a.WordItem#cloneConstant()
	 */
	protected WordItem cloneConstant() {
		return factory.createFConst(getValue());
	}

	/**
	 * Get the JVM type of this item
	 * 
	 * @return the JVM type
	 */
	final int getType() {
		return JvmType.FLOAT;
	}

	/**
	 * Gets the constant value.
	 * 
	 * @return
	 */
	float getValue() {
		assertCondition(kind == Kind.CONSTANT, "kind == Kind.CONSTANT");
		return value;
	}

	/**
	 * Load my constant to the given os.
	 * 
	 * @param os
	 * @param reg
	 */
	protected void loadToConstant(EmitterContext ec, AbstractX86Stream os,
			Register reg) {
		os.writeMOV_Const(reg, Float.floatToIntBits(value));
	}

	/**
	 * Pop the top of the FPU stack into the given memory location.
	 * 
	 * @param os
	 * @param reg
	 * @param disp
	 */
	protected void popFromFPU(AbstractX86Stream os, Register reg, int disp) {
		os.writeFSTP32(reg, disp);
	}

	/**
	 * Push my constant on the stack using the given os.
	 * 
	 * @param os
	 */
	protected void pushConstant(EmitterContext ec, AbstractX86Stream os) {
		os.writePUSH(Float.floatToIntBits(value));
	}

	/**
	 * Push the given memory location on the FPU stack.
	 * 
	 * @param os
	 * @param reg
	 * @param disp
	 */
	protected void pushToFPU(AbstractX86Stream os, Register reg, int disp) {
		os.writeFLD32(reg, disp);
	}
}