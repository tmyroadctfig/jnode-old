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

package org.jnode.vm.x86.compiler;

import java.util.HashMap;
import java.util.Map;

import org.jnode.assembler.Label;
import org.jnode.assembler.x86.X86Assembler;
import org.jnode.assembler.x86.X86Constants;
import org.jnode.assembler.x86.X86Register;
import org.jnode.assembler.x86.X86Register.GPR;
import org.jnode.assembler.x86.X86Register.GPR64;
import org.jnode.vm.JvmType;
import org.jnode.vm.Vm;
import org.jnode.vm.VmProcessor;
import org.jnode.vm.classmgr.VmArray;
import org.jnode.vm.classmgr.VmInstanceField;
import org.jnode.vm.classmgr.VmIsolatedStaticsEntry;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmSharedStaticsEntry;
import org.jnode.vm.classmgr.VmStaticField;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.classmgr.VmTypeState;
import org.jnode.vm.compiler.EntryPoints;
import org.jnode.vm.memmgr.VmWriteBarrier;
import org.jnode.vm.x86.X86CpuID;
import org.vmmagic.pragma.PrivilegedActionPragma;

/**
 * Helpers class used by the X86 compilers.
 * 
 * @author epr
 * @author patrik_reali
 */
public class X86CompilerHelper implements X86CompilerConstants {

    /** Address size ax register (EAX/RAX) */
    public final GPR AAX;

    /** Address size bx register (EBX/RBX) */
    public final GPR ABX;

    /** Address size cx register (ECX/RCX) */
    public final GPR ACX;

    /** Address size dx register (EDX/RDX) */
    public final GPR ADX;

    /** The stack pointer (ESP/RSP) */
    public final GPR SP;

    /** The stack frame pointer (EBP/RBP) */
    public final GPR BP;

    /** The statics table pointer (EDI/RDI) */
    public final GPR STATICS;

    /** The size of an address (BITS32/BITS64) */
    public final int ADDRSIZE;

    /** The size of stack slot in bytes (4/8) */
    public final int SLOTSIZE;

    private final EntryPoints entryPoints;

    private VmMethod method;

    private String labelPrefix;

    private String instrLabelPrefix;

    private final boolean haveCMOV;

    private final Map<Integer, Label> addressLabels = new HashMap<Integer, Label>();

    private final boolean debug = Vm.getVm().isDebugMode();

    private final AbstractX86StackManager stackMgr;

    private final X86Assembler os;

    /**
     * Create a new instance
     * 
     * @param entryPoints
     */
    public X86CompilerHelper(X86Assembler os, AbstractX86StackManager stackMgr,
            EntryPoints entryPoints, boolean isBootstrap)
            throws PrivilegedActionPragma {
        this.os = os;
        if (os.isCode32()) {
            this.AAX = X86Register.EAX;
            this.ABX = X86Register.EBX;
            this.ACX = X86Register.ECX;
            this.ADX = X86Register.EDX;
            this.SP = X86Register.ESP;
            this.BP = X86Register.EBP;
            this.STATICS = X86CompilerConstants.STATICS32;
            this.ADDRSIZE = X86Constants.BITS32;
            this.SLOTSIZE = 4;
        } else {
            this.AAX = X86Register.RAX;
            this.ABX = X86Register.RBX;
            this.ACX = X86Register.RCX;
            this.ADX = X86Register.RDX;
            this.SP = X86Register.RSP;
            this.BP = X86Register.RBP;
            this.STATICS = X86CompilerConstants.STATICS64;
            this.ADDRSIZE = X86Constants.BITS64;
            this.SLOTSIZE = 8;
        }
        this.entryPoints = entryPoints;
        this.stackMgr = stackMgr;
        final X86CpuID cpuId = (X86CpuID) os.getCPUID();
        haveCMOV = cpuId.hasFeature(X86CpuID.FEAT_CMOV);
    }

    /**
     * Gets the method that is currently being compiled.
     * 
     * @return method
     */
    public final VmMethod getMethod() {
        return method;
    }

    /**
     * Sets the method that is currently being compiled.
     * 
     * @param method
     */
    public final void setMethod(VmMethod method) {
        this.method = method;
        this.labelPrefix = method.toString() + "_";
        this.instrLabelPrefix = labelPrefix + "_bci_";
        this.addressLabels.clear();
    }

    /**
     */
    public void startInlinedMethod(VmMethod inlinedMethod, Label curInstrLabel) {
        this.labelPrefix = curInstrLabel + "_" + inlinedMethod.getName() + "_";
        this.instrLabelPrefix = labelPrefix + "_bci_";
        this.addressLabels.clear();
    }

    /**
     * Create a method relative label to a given bytecode address.
     * 
     * @param address
     * @return The created label
     */
    public final Label getInstrLabel(int address) {
        Label l = addressLabels.get(address);
        if (l == null) {
            l = new Label(instrLabelPrefix + address);
            addressLabels.put(address, l);
        }
        return l;
    }

    /**
     * Create a method relative label
     * 
     * @param postFix
     * @return The created label
     */
    public final Label genLabel(String postFix) {
        return new Label(labelPrefix + postFix);
    }

    /**
     * Write code to call the address found at the given index in the system
     * jumptable.
     * 
     * @param index
     * @see X86JumpTable
     */
    public final void writeJumpTableCALL(int index) {
        if (os.isCode64()) {
            index *= 2;
        }
        final int offset = (VmArray.DATA_OFFSET * SLOTSIZE) + (index << 2);
        os.writeCALL(STATICS, offset);
    }

    /**
     * Write code to jump to the address found at the given index in the system
     * jumptable.
     * 
     * @param index
     * @see X86JumpTable
     */
    public final void writeJumpTableJMP(int index) {
        if (os.isCode64()) {
            index *= 2;
        }
        final int offset = (VmArray.DATA_OFFSET * SLOTSIZE) + (index << 2);
        os.writeJMP(STATICS, offset);
    }

    /**
     * Emit code to push the returncode of the given method signature.
     * 
     * @param signature
     */
    public final void pushReturnValue(String signature) {
        final int returnType = JvmType.getReturnType(signature);
        assertCondition(
                signature.endsWith("V") == (returnType == JvmType.VOID),
                "Return type");
        // System.out.println("Return type: " + returnType + "\t" + signature);
        switch (returnType) {
        case JvmType.VOID:
            // No return value
            break;
        case JvmType.DOUBLE:
        case JvmType.LONG:
            // Wide return value
            if (os.isCode32()) {
                stackMgr.writePUSH64(returnType, X86Register.EAX,
                        X86Register.EDX);
            } else {
                stackMgr.writePUSH64(returnType, X86Register.RAX);
            }
            break;
        case JvmType.REFERENCE:
            stackMgr.writePUSH(returnType, AAX);
            break;
        default:
            // int/float return value
            stackMgr.writePUSH(returnType, X86Register.EAX);
        }
    }

    /**
     * Emit code to invoke a java method
     * 
     * @param method
     */
    public final void invokeJavaMethod(VmMethod method) {
        final int offset = getSharedStaticsOffset(method);
        os.writeCALL(STATICS, offset);
        pushReturnValue(method.getSignature());
    }

    /**
     * Insert a yieldpoint into the code
     */
    public final void writeYieldPoint(Object curInstrLabel) {
        if (!method.isUninterruptible()) {
            final Label doneLabel = new Label(curInstrLabel + "noYP");
            final int offset = entryPoints.getVmThreadSwitchIndicatorOffset();
            final int flag = VmProcessor.TSI_SWITCH_REQUESTED;
            if (os.isCode32()) {
                os.writePrefix(X86Constants.FS_PREFIX);
                os.writeCMP_MEM(BITS32, offset, flag);
            } else {
                os.writeCMP_Const(BITS32, PROCESSOR64, offset, flag);
            }
            os.writeJCC(doneLabel, X86Constants.JNE);
            os.writeINT(X86CompilerConstants.YIELDPOINT_INTNO);
            os.setObjectRef(doneLabel);
        }
    }

    /**
     * Write class initialization code
     * 
     * @param method
     * @param methodReg
     *            Register that holds the method reference before this method is
     *            called.
     * @return true if code was written, false otherwise
     */
    public final boolean writeClassInitialize(VmMethod method) {
        // Only for static methods (non <clinit>)
        if (method.isStatic() && !method.isInitializer()) {
            // Only when class is not initialize
            final VmType<?> cls = method.getDeclaringClass();
            if (!cls.isInitialized()) {
                final GPR aax = this.AAX;
                final int size = os.getMode().getSize();

                // Save eax
                os.writePUSH(aax);
                // Do the is initialized test
                // Move method.declaringClass -> EAX
                final int typeOfs = getSharedStaticsOffset(method
                        .getDeclaringClass());
                os.writeMOV(size, aax, STATICS, typeOfs);
                // Test declaringClass.modifiers
                os.writeTEST(BITS32, aax, entryPoints.getVmTypeState()
                        .getOffset(), VmTypeState.ST_INITIALIZED);
                final Label afterInit = new Label(method.getMangledName()
                        + "$$after-classinit");
                os.writeJCC(afterInit, X86Constants.JNZ);
                // Call cls.initialize
                os.writePUSH(aax);
                invokeJavaMethod(entryPoints.getVmTypeInitialize());
                os.setObjectRef(afterInit);
                // Restore eax
                os.writePOP(aax);
                return true;
            }
        }
        return false;
    }

    public final void writeClassInitialize(Label curInstrLabel, GPR classReg,
            VmType<?> cls) {
        if (!cls.isInitialized()) {
            // Test declaringClass.modifiers
            os.writeTEST(BITS32, classReg, entryPoints.getVmTypeState()
                    .getOffset(), VmTypeState.ST_INITIALIZED);
            final Label afterInit = new Label(curInstrLabel
                    + "$$after-classinit-ex");
            os.writeJCC(afterInit, X86Constants.JNZ);
            if (os.isCode32()) {
                os.writePUSHA();
            } else {
                os.writePUSH(X86Register.RAX);
                os.writePUSH(X86Register.RBX);
                os.writePUSH(X86Register.RCX);
                os.writePUSH(X86Register.RDX);
                os.writePUSH(X86Register.RSI);
                os.writePUSH(X86Register.R8);
                os.writePUSH(X86Register.R9);
                os.writePUSH(X86Register.R10);
                os.writePUSH(X86Register.R11);
                // R12 contains processor and is preserved
                os.writePUSH(X86Register.R13);
                os.writePUSH(X86Register.R14);
                os.writePUSH(X86Register.R15);
            }
            // Call cls.initialize
            os.writePUSH(classReg);
            invokeJavaMethod(entryPoints.getVmTypeInitialize());
            if (os.isCode32()) {
                os.writePOPA();
            } else {
                os.writePOP(X86Register.R15);
                os.writePOP(X86Register.R14);
                os.writePOP(X86Register.R13);
                // R12 contains processor and is preserved
                os.writePOP(X86Register.R11);
                os.writePOP(X86Register.R10);
                os.writePOP(X86Register.R9);
                os.writePOP(X86Register.R8);
                os.writePOP(X86Register.RSI);
                os.writePOP(X86Register.RDX);
                os.writePOP(X86Register.RCX);
                os.writePOP(X86Register.RBX);
                os.writePOP(X86Register.RAX);
            }
            // Set label
            os.setObjectRef(afterInit);
        }
    }

    /**
     * Write stack overflow test code.
     * 
     * @param method
     */
    public final void writeStackOverflowTest(VmMethod method_) {
        // cmp esp,STACKEND
        // jg vm_invoke_testStackOverflowDone
        // vm_invoke_testStackOverflow:
        // int 0x31
        // vm_invoke_testStackOverflowDone:
        final int offset = entryPoints.getVmProcessorStackEnd().getOffset();
        final Label doneLabel = new Label(labelPrefix + "$$stackof-done");
        if (os.isCode32()) {
            os.writePrefix(X86Constants.FS_PREFIX);
            os.writeCMP_MEM(X86Register.ESP, offset);
        } else {
            os.writeCMP(X86Register.RSP, PROCESSOR64, offset);
        }
        os.writeJCC(doneLabel, X86Constants.JG);
        os.writeINT(0x31);
        os.setObjectRef(doneLabel);
    }

    /**
     * Write staticTable load code. After the code (generated by this method) is
     * executed, the STATICS register contains the reference to the statics
     * table.
     */
    public final void writeLoadSTATICS(Label curInstrLabel, String labelPrefix,
            boolean isTestOnly) {
        final int offset = entryPoints.getVmProcessorSharedStaticsTable()
                .getOffset();
        if (isTestOnly) {
            if (debug) {
                final Label ok = new Label(curInstrLabel + labelPrefix
                        + "$$ediok");
                if (os.isCode32()) {
                    os.writePrefix(X86Constants.FS_PREFIX);
                    os.writeCMP_MEM(this.STATICS, offset);
                } else {
                    os.writeCMP(this.STATICS, PROCESSOR64, offset);
                }
                os.writeJCC(ok, X86Constants.JE);
                os.writeINT(0x88);
                os.setObjectRef(ok);
            }
        } else {
            if (os.isCode32()) {
                os.writeXOR(this.STATICS, this.STATICS);
                os.writePrefix(X86Constants.FS_PREFIX);
                os.writeMOV(INTSIZE, this.STATICS, this.STATICS, offset);
            } else {
                os.writeMOV(BITS64, this.STATICS, PROCESSOR64, offset);
            }
        }
    }

    /**
     * Write isolatedStaticTable load code. After the code (generated by this
     * method) is executed, the given destination register contains the
     * reference to the isolated statics table of the current isolate.
     */
    public final void writeLoadIsolatedStatics(Label curInstrLabel,
            String labelPrefix, GPR dst) {
        final int offset = entryPoints.getVmProcessorIsolatedStaticsTable()
                .getOffset();
        if (os.isCode32()) {
            os.writeXOR(dst, dst);
            os.writePrefix(X86Constants.FS_PREFIX);
            os.writeMOV(INTSIZE, dst, dst, offset);
        } else {
            os.writeMOV(BITS64, dst, PROCESSOR64, offset);
        }
    }

    /**
     * Is class initialization code needed for the given method.
     * 
     * @param method
     * @return true if class init code is needed, false otherwise.
     */
    public static boolean isClassInitializeNeeded(VmMethod method) {
        // Only for static methods (non <clinit>)
        if (method.isStatic() && !method.isInitializer()) {
            // Only when class is not initialize
            final VmType<?> cls = method.getDeclaringClass();
            if (!cls.isInitialized()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Do we need a write barrier
     * 
     * @return True/false
     */
    public final boolean needsWriteBarrier() {
        return (entryPoints.getWriteBarrier() != null);
    }

    /**
     * Write code to call the arrayStoreWriteBarrier.
     * 
     * @param refReg
     * @param indexReg
     * @param valueReg
     */
    public final void writeArrayStoreWriteBarrier(GPR refReg, GPR indexReg,
            GPR valueReg, GPR scratchReg) {
        final VmWriteBarrier wb = entryPoints.getWriteBarrier();
        if (wb != null) {
            os.writeMOV_Const(scratchReg, wb);
            os.writePUSH(scratchReg);
            os.writePUSH(refReg);
            os.writePUSH(indexReg);
            os.writePUSH(valueReg);
            invokeJavaMethod(entryPoints.getArrayStoreWriteBarrier());
        }
    }

    /**
     * Write code to call the putfieldWriteBarrier.
     * 
     * @param field
     * @param refReg
     * @param valueReg
     */
    public final void writePutfieldWriteBarrier(VmInstanceField field,
            GPR refReg, GPR valueReg, GPR scratchReg) {
        if (field.isObjectRef()) {
            final VmWriteBarrier wb = entryPoints.getWriteBarrier();
            if (wb != null) {
                os.writeMOV_Const(scratchReg, wb);
                os.writePUSH(scratchReg);
                os.writePUSH(refReg);
                os.writePUSH(field.getOffset());
                os.writePUSH(valueReg);
                invokeJavaMethod(entryPoints.getPutfieldWriteBarrier());
            }
        }
    }

    /**
     * Write code to call the putstaticWriteBarrier.
     * 
     * @param field
     * @param valueReg
     */
    public final void writePutstaticWriteBarrier(VmStaticField field,
            GPR valueReg, GPR scratchReg) {
        if (Vm.VerifyAssertions) {
            Vm._assert(scratchReg.getSize() == this.ADDRSIZE,
                    "scratchReg wrong size");
            Vm._assert(valueReg.getSize() == this.ADDRSIZE,
                    "valueReg wrong size");
        }
        if (field.isObjectRef()) {
            final VmWriteBarrier wb = entryPoints.getWriteBarrier();
            if (wb != null) {
                os.writeMOV_Const(scratchReg, wb);
                os.writePUSH(scratchReg);
                if (field.isShared()) {
                    os.writePUSH(1); // shared = true
                    os.writePUSH(field.getSharedStaticsIndex());
                } else {
                    os.writePUSH(0); // shared = false
                    os.writePUSH(field.getIsolatedStaticsIndex());
                }
                os.writePUSH(valueReg);
                invokeJavaMethod(entryPoints.getPutstaticWriteBarrier());
            }
        }
    }

    /**
     * Is CMOVxx support bu the current cpu.
     * 
     * @return Returns the haveCMOV.
     */
    public final boolean haveCMOV() {
        return this.haveCMOV;
    }

    /**
     * Write code to load the given 32-bit shared statics table entry into the
     * given register.
     * 
     * @param curInstrLabel
     * @param dst
     * @param entry
     */
    public final void writeGetStaticsEntry(Label curInstrLabel, GPR dst,
            VmSharedStaticsEntry entry) {
        if (Vm.VerifyAssertions) {
            Vm._assert(dst.getSize() == BITS32, "dst wrong size");
        }
        writeLoadSTATICS(curInstrLabel, "gs", true);
        os.writeMOV(INTSIZE, dst, this.STATICS, getSharedStaticsOffset(entry));
    }

    /**
     * Write code to load the given 32-bit isolated statics table entry into the
     * given register.
     * 
     * @param curInstrLabel
     * @param dst
     * @param entry
     * @param tmp A temporary REFERENCE register
     */
    public final void writeGetStaticsEntry(Label curInstrLabel, GPR dst,
            VmIsolatedStaticsEntry entry, GPR tmp) {
        if (Vm.VerifyAssertions) {
            Vm._assert(dst.getSize() == BITS32, "dst wrong size");
        }
        writeLoadIsolatedStatics(curInstrLabel, "gs", tmp);
        os.writeMOV(INTSIZE, dst, tmp, getIsolatedStaticsOffset(entry));
    }

    /**
     * Write code to load the given statics table entry onto the FPU stack.
     * 
     * @param curInstrLabel
     * @param entry
     * @param is32bit
     *            If true, a 32-bit load is performed, otherwise a 64-bit load.
     */
    public final void writeGetStaticsEntryToFPU(Label curInstrLabel,
            VmSharedStaticsEntry entry, boolean is32bit) {
        writeLoadSTATICS(curInstrLabel, "gs", true);
        final int staticsIdx = getSharedStaticsOffset(entry);
        if (is32bit) {
            os.writeFLD32(this.STATICS, staticsIdx);
        } else {
            os.writeFLD64(this.STATICS, staticsIdx);
        }
    }

    /**
     * Write code to load the given statics table entry onto the FPU stack.
     * 
     * @param curInstrLabel
     * @param entry
     * @param is32bit
     *            If true, a 32-bit load is performed, otherwise a 64-bit load.
     * @param tmp
     *            A temporary register of the REFERENCE kind
     */
    public final void writeGetStaticsEntryToFPU(Label curInstrLabel,
            VmIsolatedStaticsEntry entry, boolean is32bit, GPR tmp) {
        writeLoadIsolatedStatics(curInstrLabel, "gs", tmp);
        final int staticsIdx = getIsolatedStaticsOffset(entry);
        if (is32bit) {
            os.writeFLD32(tmp, staticsIdx);
        } else {
            os.writeFLD64(tmp, staticsIdx);
        }
    }

    /**
     * Write code to push the given statics table entry to the stack
     * 
     * @param curInstrLabel
     * @param entry
     */
    /* Patrik, added to push without requiring allocation of a register */
    public final void writePushStaticsEntry(Label curInstrLabel,
            VmSharedStaticsEntry entry) {
        writeLoadSTATICS(curInstrLabel, "gs", true);
        os.writePUSH(this.STATICS, getSharedStaticsOffset(entry));
    }

    /**
     * Write code to load the given 64-bit shared statics table entry into the
     * given 32-bit registers.
     * 
     * @param curInstrLabel
     * @param lsbDst
     * @param msbReg
     * @param entry
     */
    public final void writeGetStaticsEntry64(Label curInstrLabel, GPR lsbDst,
            GPR msbReg, VmSharedStaticsEntry entry) {
        writeLoadSTATICS(curInstrLabel, "gs64", true);
        final int staticsOfs = getSharedStaticsOffset(entry);
        os.writeMOV(INTSIZE, msbReg, this.STATICS, staticsOfs + 4); // MSB
        os.writeMOV(INTSIZE, lsbDst, this.STATICS, staticsOfs + 0); // LSB
    }

    /**
     * Write code to load the given 64-bit isolated statics table entry into the
     * given 32-bit registers.
     * 
     * @param curInstrLabel
     * @param lsbDst
     * @param msbReg
     * @param entry
     */
    public final void writeGetStaticsEntry64(Label curInstrLabel, GPR lsbDst,
            GPR msbReg, VmIsolatedStaticsEntry entry) {
        writeLoadIsolatedStatics(curInstrLabel, "gs64", lsbDst);
        final int staticsOfs = getIsolatedStaticsOffset(entry);
        os.writeMOV(INTSIZE, msbReg, lsbDst, staticsOfs + 4); // MSB
        os.writeMOV(INTSIZE, lsbDst, lsbDst, staticsOfs + 0); // LSB
    }

    /**
     * Write code to load the given 64-bit shared statics table entry into the
     * given 64-bit register.
     * 
     * @param curInstrLabel
     * @param dstReg
     * @param entry
     */
    public final void writeGetStaticsEntry64(Label curInstrLabel, GPR64 dstReg,
            VmSharedStaticsEntry entry) {
        writeLoadSTATICS(curInstrLabel, "gs64", true);
        os
                .writeMOV(BITS64, dstReg, this.STATICS,
                        getSharedStaticsOffset(entry));
    }

    /**
     * Write code to load the given 64-bit shared statics table entry into the
     * given 64-bit register.
     * 
     * @param curInstrLabel
     * @param dstReg
     * @param entry
     */
    public final void writeGetStaticsEntry64(Label curInstrLabel, GPR64 dstReg,
            VmIsolatedStaticsEntry entry) {
        writeLoadIsolatedStatics(curInstrLabel, "gs64", dstReg);
        os.writeMOV(BITS64, dstReg, dstReg, getIsolatedStaticsOffset(entry));
    }

    /**
     * Write code to store the given statics table entry into the given
     * register.
     * 
     * @param curInstrLabel
     * @param src
     * @param entry
     */
    public final void writePutStaticsEntry(Label curInstrLabel, GPR src,
            VmSharedStaticsEntry entry) {
        writeLoadSTATICS(curInstrLabel, "ps", true);
        os.writeMOV(INTSIZE, this.STATICS, getSharedStaticsOffset(entry), src);
    }

    /**
     * Write code to store the given isolated statics table entry into the given
     * register.
     * 
     * @param curInstrLabel
     * @param src
     * @param entry
     */
    public final void writePutStaticsEntry(Label curInstrLabel, GPR src,
            VmIsolatedStaticsEntry entry, GPR tmp) {
        writeLoadIsolatedStatics(curInstrLabel, "ps", tmp);
        os.writeMOV(INTSIZE, tmp, getIsolatedStaticsOffset(entry), src);
    }

    /**
     * Write code to store the given 64-bit shared statics table entry into the
     * given 32-bit registers.
     * 
     * @param curInstrLabel
     * @param lsbSrc
     * @param msbSrc
     * @param entry
     */
    public final void writePutStaticsEntry64(Label curInstrLabel, GPR lsbSrc,
            GPR msbSrc, VmSharedStaticsEntry entry) {
        writeLoadSTATICS(curInstrLabel, "ps64", true);
        final int staticsOfs = getSharedStaticsOffset(entry);
        os.writeMOV(BITS32, this.STATICS, staticsOfs + 4, msbSrc); // MSB
        os.writeMOV(BITS32, this.STATICS, staticsOfs + 0, lsbSrc); // LSB
    }

    /**
     * Write code to store the given 64-bit shared statics table entry into the
     * given 32-bit registers.
     * 
     * @param curInstrLabel
     * @param lsbSrc
     * @param msbSrc
     * @param entry
     */
    public final void writePutStaticsEntry64(Label curInstrLabel, GPR lsbSrc,
            GPR msbSrc, VmIsolatedStaticsEntry entry, GPR tmp) {
        writeLoadIsolatedStatics(curInstrLabel, "ps64", tmp);
        final int staticsOfs = getIsolatedStaticsOffset(entry);
        os.writeMOV(BITS32, tmp, staticsOfs + 4, msbSrc); // MSB
        os.writeMOV(BITS32, tmp, staticsOfs + 0, lsbSrc); // LSB
    }

    /**
     * Write code to store the given 64-bit shared statics table entry into the
     * given 64-bit register.
     * 
     * @param curInstrLabel
     * @param srcReg
     * @param entry
     */
    public final void writePutStaticsEntry64(Label curInstrLabel, GPR64 srcReg,
            VmSharedStaticsEntry entry) {
        writeLoadSTATICS(curInstrLabel, "ps64", true);
        os
                .writeMOV(BITS64, this.STATICS, getSharedStaticsOffset(entry),
                        srcReg);
    }

    /**
     * Write code to store the given 64-bit isolated statics table entry into
     * the given 64-bit register.
     * 
     * @param curInstrLabel
     * @param srcReg
     * @param entry
     */
    public final void writePutStaticsEntry64(Label curInstrLabel, GPR64 srcReg,
            VmIsolatedStaticsEntry entry, GPR tmp) {
        writeLoadIsolatedStatics(curInstrLabel, "ps64", tmp);
        os.writeMOV(BITS64, tmp, getIsolatedStaticsOffset(entry), srcReg);
    }

    /**
     * Gets the offset from the beginning of the shared statics table
     * (this.STATICS) to the given entry.
     * 
     * @param entry
     * @return The byte offset from this.STATICS to the entry.
     */
    public final int getSharedStaticsOffset(VmSharedStaticsEntry entry) {
        if (os.isCode32()) {
            return (VmArray.DATA_OFFSET * 4)
                    + (entry.getSharedStaticsIndex() << 2);
        } else {
            return (VmArray.DATA_OFFSET * 8)
                    + (entry.getSharedStaticsIndex() << 2);
        }
    }

    /**
     * Gets the offset from the beginning of the isolated statics table to the
     * given entry.
     * 
     * @param entry
     * @return The byte offset from the isolated statics table to the entry.
     */
    public final int getIsolatedStaticsOffset(VmIsolatedStaticsEntry entry) {
        if (os.isCode32()) {
            return (VmArray.DATA_OFFSET * 4)
                    + (entry.getIsolatedStaticsIndex() << 2);
        } else {
            return (VmArray.DATA_OFFSET * 8)
                    + (entry.getIsolatedStaticsIndex() << 2);
        }
    }

    public static void assertCondition(boolean condition, String msg) {
        if (!condition) {
            throw new InternalError("Assertion failed: " + msg);
        }
    }
}
