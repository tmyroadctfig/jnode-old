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
 
package org.jnode.vm.x86;

import org.jnode.util.NumberUtils;
import org.jnode.vm.ObjectVisitor;
import org.jnode.vm.VmMagic;
import org.jnode.vm.VmThread;
import org.jnode.vm.memmgr.VmHeapManager;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.ObjectReference;

/**
 * Thread implementation for Intel X86 processor.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VmX86Thread extends VmThread {

	// State when not running
	volatile int eax;
	volatile int ebx;
	volatile int ecx;
	volatile int edx;
	volatile int esi;
	volatile int edi;
	volatile int eflags;
	volatile Address eip;
	volatile Address esp;
	volatile Address ebp;
	
	private static final int FXSTATE_SIZE = 512+16;
	
	// Saved state of FPU & XMM
	private final byte[] fxState;
	private volatile Address fxStatePtr;	// This value is set in assembler code
	
	// State upon last system exception
	volatile int exEax;
	volatile int exEbx;
	volatile int exEcx;
	volatile int exEdx;
	volatile int exEsi;
	volatile int exEdi;
	volatile int exEflags;
	volatile int exEip;
	volatile int exEsp;
	volatile int exEbp;
	volatile int exCr2;
	
	/**
	 * Initialize this instance 
	 */
	VmX86Thread() {
		fxState = new byte[FXSTATE_SIZE];
	}

    /**
     * Create a new instance. This constructor can only be called during the
     * bootstrap phase.
     */
    VmX86Thread(byte[] stack) {
        super(stack, getStackEnd(stack, stack.length), stack.length);
		fxState = new byte[FXSTATE_SIZE];
    }
    
	/**
	 * @param javaThread
	 */
	public VmX86Thread(Thread javaThread) {
		super(javaThread);
		fxState = new byte[FXSTATE_SIZE];
	}

	
	/**
	 * Gets the most current stackframe of this thread.
	 * @return Stackframe 
	 */
	protected Address getStackFrame() {
		return ebp;
	}
	
	/**
	 * Gets the most current instruction pointer of this thread.
	 * This method is only valid when this thread is not running.
	 * @return IP 
	 */
	protected Address getInstructionPointer() {
		return eip;
	}

    /**
     * Gets the stackframe of the last system exception of this thread. 
     */
    protected Address getExceptionStackFrame() {
        return Address.fromIntZeroExtend(exEbp);
    }

    /**
     * Gets the instruction pointer of the last system exception of this thread. 
     */
    protected Address getExceptionInstructionPointer() {
        return Address.fromIntZeroExtend(exEip);
    }

	/**
	 * Calculate the end of the stack. 
	 * @param stack
	 * @param stackSize
	 * @return End address of the stack
	 */
	protected Address getStackEnd(Object stack, int stackSize) {
		return ObjectReference.fromObject(stack).toAddress().add(STACK_OVERFLOW_LIMIT);
	}
	
	/**
	 * Calculate the end of the stack. 
	 * @param stack
	 * @param stackSize
	 * @return End address of the stack
	 */
	private static Address getStackEnd(byte[] stack, int stackSize) {
		return VmMagic.getArrayData(stack).add(STACK_OVERFLOW_LIMIT);
	}
	
	/**
	 * Gets a human readable representation of the system exception state.
	 * @return String
	 */
	public String getReadableErrorState() {
		return "EAX " +NumberUtils.hex(exEax) +
			" EBX " +NumberUtils.hex(exEbx) +
			" ECX " +NumberUtils.hex(exEcx) +
			" EDX " +NumberUtils.hex(exEdx) +
			" ESI " +NumberUtils.hex(exEsi) +
			" EDI " +NumberUtils.hex(exEdi) +
			" ESP " +NumberUtils.hex(exEsp) +
			" EIP " +NumberUtils.hex(exEip) +
			" CR2 " +NumberUtils.hex(exCr2) +
			" EFLAGS " +NumberUtils.hex(exEflags);
	}

    /**
     * Visit all objects on the stack and register state of this thread.
     * @param visitor
     * @param heapManager
     */
    public boolean visit(ObjectVisitor visitor, VmHeapManager heapManager) {
        // For now do it stupid, but safe, just scan the whole stack.
        final int stackSize = getStackSize();
        final Object stack = getStack();        
        if (stack != null) {
            final Address stackBottom = ObjectReference.fromObject(stack).toAddress();
            final Address stackTop = stackBottom.add(stackSize);
            final Address stackEnd;
            if (this == currentThread()) {
                stackEnd = stackBottom;
            } else {
                stackEnd = esp;
            }
            
            Address ptr = stackTop;
            final int slotSize = VmX86Architecture.SLOT_SIZE;
            while (ptr.GE(stackEnd)) {
                final Address child = ptr.loadAddress();
                if (child != null) {
                    if (heapManager.isObject(child)) {
                        if (!visitor.visit(child)) {
                            return false;
                        }
                    }
                }
                ptr = ptr.sub(slotSize);                
            }
        }
        // Scan registers
        Address addr = Address.fromIntZeroExtend(eax);
        if (heapManager.isObject(addr)) {
            if (!visitor.visit(addr)) {
                return false;
            }
        }
        addr = Address.fromIntZeroExtend(ebx);
        if (heapManager.isObject(addr)) {
            if (!visitor.visit(addr)) {
                return false;
            }
        }
        addr = Address.fromIntZeroExtend(ecx);
        if (heapManager.isObject(addr)) {
            if (!visitor.visit(addr)) {
                return false;
            }
        }
        addr = Address.fromIntZeroExtend(edx);
        if (heapManager.isObject(addr)) {
            if (!visitor.visit(addr)) {
                return false;
            }
        }
        addr = Address.fromIntZeroExtend(esi);
        if (heapManager.isObject(addr)) {
            if (!visitor.visit(addr)) {
                return false;
            }
        }
        addr = Address.fromIntZeroExtend(edi);
        if (heapManager.isObject(addr)) {
            if (!visitor.visit(addr)) {
                return false;
            }
        }
        return true;
    }
}
