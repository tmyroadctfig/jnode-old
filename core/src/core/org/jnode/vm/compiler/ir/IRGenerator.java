/*
 * $Id$
 *
 * mailto:madhu@madhu.com
 */
package org.jnode.vm.compiler.ir;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.jnode.vm.VmClassLoader;
import org.jnode.vm.bytecode.BytecodeParser;
import org.jnode.vm.bytecode.BytecodeViewer;
import org.jnode.vm.bytecode.BytecodeVisitor;
import org.jnode.vm.classmgr.VmByteCode;
import org.jnode.vm.classmgr.VmConstClass;
import org.jnode.vm.classmgr.VmConstFieldRef;
import org.jnode.vm.classmgr.VmConstIMethodRef;
import org.jnode.vm.classmgr.VmConstMethodRef;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.x86.VmX86Architecture;

/**
 * @author Madhu Siddalingaiah
 *
 */
public class IRGenerator extends BytecodeVisitor {
	private final static Constant NULL_CONSTANT = Constant.getInstance(null);
//	private VmMethod method;
	private int nArgs;
	private int nLocals;
	private int maxStack;
	private int stackOffset;
	private Variable[] variables;
	private int address;
	private ArrayList iopList;
	private Iterator basicBlockIterator;
	private IRBasicBlock currentBlock;

	/**
	 * 
	 */
	public IRGenerator(IRControlFlowGraph cfg) {
		basicBlockIterator = cfg.basicBlockIterator();
		currentBlock = (IRBasicBlock) basicBlockIterator.next();
	}

	public ArrayList getIOPList() {
		return iopList;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#setParser(org.jnode.vm.bytecode.BytecodeParser)
	 */
	public void setParser(BytecodeParser parser) {
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#startMethod(org.jnode.vm.classmgr.VmMethod)
	 */
	public void startMethod(VmMethod method) {
//		this.method = method;
		VmByteCode code = method.getBytecode();
		nArgs = method.getNoArgs();
		nLocals = code.getNoLocals();
		maxStack = code.getMaxStack();
		stackOffset = nLocals;
		variables = new Variable[nLocals + maxStack];
		int index = 0;
		for (int i=0; i<nArgs; i+=1) {
			variables[index] = new MethodArgument(Operand.UNKNOWN, index);
			index += 1;
		}
		for (int i=nArgs; i<nLocals; i+=1) {
			variables[index] = new LocalVariable(Operand.UNKNOWN, index);
			index += 1;
		}
		for (int i=0; i<maxStack; i+=1) {
			variables[index] = new StackVariable(Operand.UNKNOWN, index);
			index += 1;
		}
		iopList = new ArrayList(code.getLength() >> 1);
		currentBlock.setVariables(variables);
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#endMethod()
	 */
	public void endMethod() {
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#startInstruction(int)
	 */
	public void startInstruction(int address) {
		this.address = address;
		if (address >= currentBlock.getEndPC()) {
			currentBlock = (IRBasicBlock) basicBlockIterator.next();
			Variable[] prevVars = variables;
			int n = variables.length;
			variables = new Variable[n];
			for (int i=0; i<n; i+=1) {
				variables[i] = prevVars[i];
			}
			currentBlock.setVariables(variables);
		}
		if (address < currentBlock.getStartPC() || address >= currentBlock.getEndPC()) {
			throw new AssertionError("instruction not in basic block!");
		}
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#endInstruction()
	 */
	public void endInstruction() {
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_nop()
	 */
	public void visit_nop() {
	}

	// TODO fix all binary ops to use BinaryOP.getInstance(...)
	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_aconst_null()
	 */
	public void visit_aconst_null() {
		iopList.add(new ConstantRefAssignOP(address, currentBlock, stackOffset,
			NULL_CONSTANT));
		stackOffset += 1;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iconst(int)
	 */
	public void visit_iconst(int value) {
		IOP iop = new ConstantRefAssignOP(address, currentBlock, stackOffset,
					Constant.getInstance(value));
		iopList.add(iop);
		stackOffset += 1;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lconst(long)
	 */
	public void visit_lconst(long value) {
		iopList.add(new ConstantRefAssignOP(address, currentBlock, stackOffset,
			Constant.getInstance(value)));
		stackOffset += 2;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fconst(float)
	 */
	public void visit_fconst(float value) {
		iopList.add(new ConstantRefAssignOP(address, currentBlock, stackOffset,
			Constant.getInstance(value)));
		stackOffset += 1;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dconst(double)
	 */
	public void visit_dconst(double value) {
		iopList.add(new ConstantRefAssignOP(address, currentBlock, stackOffset,
			Constant.getInstance(value)));
		stackOffset += 2;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_bipush(byte)
	 */
	public void visit_bipush(byte value) {
		ConstantRefAssignOP assignOP = new ConstantRefAssignOP(address, currentBlock,
			stackOffset, Constant.getInstance(value));
		iopList.add(assignOP);
		stackOffset += 1;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_sipush(short)
	 */
	public void visit_sipush(short value) {
		iopList.add(new ConstantRefAssignOP(address, currentBlock, stackOffset,
			Constant.getInstance(value)));
		stackOffset += 1;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ldc(int, java.lang.Object)
	 */
	public void visit_ldc(int cpIdx, Object value) {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ldc2(java.lang.Object)
	 */
	public void visit_ldc2(Object value) {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iload(int)
	 */
	public void visit_iload(int index) {
		variables[index].setType(Operand.INT);
		variables[stackOffset].setType(Operand.INT);
		VariableRefAssignOP assignOP = new VariableRefAssignOP(address, currentBlock,
			stackOffset, index);
		iopList.add(assignOP);
		stackOffset += 1;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lload(int)
	 */
	public void visit_lload(int index) {
		variables[index].setType(Operand.LONG);
		variables[stackOffset].setType(Operand.LONG);
		iopList.add(new VariableRefAssignOP(address, currentBlock,
			stackOffset, index));
		stackOffset += 2;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fload(int)
	 */
	public void visit_fload(int index) {
		variables[index].setType(Operand.FLOAT);
		variables[stackOffset].setType(Operand.FLOAT);
		iopList.add(new VariableRefAssignOP(address, currentBlock,
			stackOffset, index));
		stackOffset += 1;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dload(int)
	 */
	public void visit_dload(int index) {
		variables[index].setType(Operand.DOUBLE);
		variables[stackOffset].setType(Operand.DOUBLE);
		iopList.add(new VariableRefAssignOP(address, currentBlock,
			stackOffset, index));
		stackOffset += 2;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_aload(int)
	 */
	public void visit_aload(int index) {
		variables[index].setType(Operand.REFERENCE);
		variables[stackOffset].setType(Operand.REFERENCE);
		iopList.add(new VariableRefAssignOP(address, currentBlock, stackOffset, index));
		stackOffset += 1;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iaload()
	 */
	public void visit_iaload() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_laload()
	 */
	public void visit_laload() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_faload()
	 */
	public void visit_faload() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_daload()
	 */
	public void visit_daload() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_aaload()
	 */
	public void visit_aaload() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_baload()
	 */
	public void visit_baload() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_caload()
	 */
	public void visit_caload() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_saload()
	 */
	public void visit_saload() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_istore(int)
	 */
	public void visit_istore(int index) {
		stackOffset -= 1;
		variables[index].setType(Operand.INT);
		variables[stackOffset].setType(Operand.INT);
		VariableRefAssignOP assignOP = new VariableRefAssignOP(address, currentBlock, index, stackOffset);
		iopList.add(assignOP);
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lstore(int)
	 */
	public void visit_lstore(int index) {
		stackOffset -= 2;
		variables[index].setType(Operand.LONG);
		variables[stackOffset].setType(Operand.LONG);
		iopList.add(new VariableRefAssignOP(address, currentBlock, index, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fstore(int)
	 */
	public void visit_fstore(int index) {
		stackOffset -= 1;
		variables[index].setType(Operand.FLOAT);
		variables[stackOffset].setType(Operand.FLOAT);
		iopList.add(new VariableRefAssignOP(address, currentBlock, index, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dstore(int)
	 */
	public void visit_dstore(int index) {
		stackOffset -= 2;
		variables[index].setType(Operand.DOUBLE);
		variables[stackOffset].setType(Operand.DOUBLE);
		iopList.add(new VariableRefAssignOP(address, currentBlock, index, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_astore(int)
	 */
	public void visit_astore(int index) {
		stackOffset -= 1;
		variables[index].setType(Operand.REFERENCE);
		variables[stackOffset].setType(Operand.REFERENCE);
		iopList.add(new VariableRefAssignOP(address, currentBlock, index, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iastore()
	 */
	public void visit_iastore() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lastore()
	 */
	public void visit_lastore() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fastore()
	 */
	public void visit_fastore() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dastore()
	 */
	public void visit_dastore() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_aastore()
	 */
	public void visit_aastore() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_bastore()
	 */
	public void visit_bastore() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_castore()
	 */
	public void visit_castore() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_sastore()
	 */
	public void visit_sastore() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_pop()
	 */
	public void visit_pop() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_pop2()
	 */
	public void visit_pop2() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dup()
	 */
	public void visit_dup() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dup_x1()
	 */
	public void visit_dup_x1() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dup_x2()
	 */
	public void visit_dup_x2() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dup2()
	 */
	public void visit_dup2() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dup2_x1()
	 */
	public void visit_dup2_x1() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dup2_x2()
	 */
	public void visit_dup2_x2() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_swap()
	 */
	public void visit_swap() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iadd()
	 */
	public void visit_iadd() {
		iopList.add(BinaryOP.getInstance(address, currentBlock, stackOffset,
			BinaryOP.IADD, Operand.INT));
		stackOffset -= 1;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ladd()
	 */
	public void visit_ladd() {
		stackOffset -= 2;
		int s1 = stackOffset - 2;
		variables[s1].setType(Operand.LONG);
		variables[stackOffset].setType(Operand.LONG);
		iopList.add(new BinaryOP(address, currentBlock, s1, s1, BinaryOP.LADD, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fadd()
	 */
	public void visit_fadd() {
		stackOffset -= 1;
		int s1 = stackOffset - 1;
		variables[s1].setType(Operand.FLOAT);
		variables[stackOffset].setType(Operand.FLOAT);
		iopList.add(new BinaryOP(address, currentBlock, s1, s1, BinaryOP.FADD, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dadd()
	 */
	public void visit_dadd() {
		stackOffset -= 2;
		int s1 = stackOffset - 2;
		variables[s1].setType(Operand.INT);
		variables[stackOffset].setType(Operand.INT);
		iopList.add(new BinaryOP(address, currentBlock, s1, s1, BinaryOP.DADD, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_isub()
	 */
	public void visit_isub() {
		iopList.add(BinaryOP.getInstance(address, currentBlock, stackOffset,
			BinaryOP.ISUB, Operand.INT));
		stackOffset -= 1;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lsub()
	 */
	public void visit_lsub() {
		stackOffset -= 2;
		int s1 = stackOffset - 2;
		variables[s1].setType(Operand.LONG);
		variables[stackOffset].setType(Operand.LONG);
		iopList.add(new BinaryOP(address, currentBlock, s1, s1, BinaryOP.LSUB, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fsub()
	 */
	public void visit_fsub() {
		stackOffset -= 1;
		int s1 = stackOffset - 1;
		variables[s1].setType(Operand.FLOAT);
		variables[stackOffset].setType(Operand.FLOAT);
		iopList.add(new BinaryOP(address, currentBlock, s1, s1, BinaryOP.FSUB, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dsub()
	 */
	public void visit_dsub() {
		stackOffset -= 2;
		int s1 = stackOffset - 2;
		variables[s1].setType(Operand.DOUBLE);
		variables[stackOffset].setType(Operand.DOUBLE);
		iopList.add(new BinaryOP(address, currentBlock, s1, s1, BinaryOP.DSUB, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_imul()
	 */
	public void visit_imul() {
		iopList.add(BinaryOP.getInstance(address, currentBlock, stackOffset,
			BinaryOP.IMUL, Operand.INT));
		stackOffset -= 1;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lmul()
	 */
	public void visit_lmul() {
		stackOffset -= 2;
		int s1 = stackOffset - 2;
		variables[s1].setType(Operand.LONG);
		variables[stackOffset].setType(Operand.LONG);
		iopList.add(new BinaryOP(address, currentBlock, s1, s1, BinaryOP.LMUL, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fmul()
	 */
	public void visit_fmul() {
		stackOffset -= 1;
		int s1 = stackOffset - 1;
		variables[s1].setType(Operand.FLOAT);
		variables[stackOffset].setType(Operand.FLOAT);
		iopList.add(new BinaryOP(address, currentBlock, s1, s1, BinaryOP.FMUL, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dmul()
	 */
	public void visit_dmul() {
		stackOffset -= 2;
		int s1 = stackOffset - 2;
		variables[s1].setType(Operand.DOUBLE);
		variables[stackOffset].setType(Operand.DOUBLE);
		iopList.add(new BinaryOP(address, currentBlock, s1, s1, BinaryOP.DMUL, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_idiv()
	 */
	public void visit_idiv() {
		iopList.add(BinaryOP.getInstance(address, currentBlock, stackOffset,
			BinaryOP.IDIV, Operand.INT));
		stackOffset -= 1;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ldiv()
	 */
	public void visit_ldiv() {
		stackOffset -= 2;
		int s1 = stackOffset - 2;
		variables[s1].setType(Operand.LONG);
		variables[stackOffset].setType(Operand.LONG);
		iopList.add(new BinaryOP(address, currentBlock, s1, s1, BinaryOP.LDIV, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fdiv()
	 */
	public void visit_fdiv() {
		stackOffset -= 1;
		int s1 = stackOffset - 1;
		variables[s1].setType(Operand.FLOAT);
		variables[stackOffset].setType(Operand.FLOAT);
		iopList.add(new BinaryOP(address, currentBlock, s1, s1, BinaryOP.FDIV, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ddiv()
	 */
	public void visit_ddiv() {
		stackOffset -= 2;
		int s1 = stackOffset - 2;
		variables[s1].setType(Operand.DOUBLE);
		variables[stackOffset].setType(Operand.DOUBLE);
		iopList.add(new BinaryOP(address, currentBlock, s1, s1, BinaryOP.DDIV, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_irem()
	 */
	public void visit_irem() {
		stackOffset -= 1;
		int s1 = stackOffset - 1;
		variables[s1].setType(Operand.INT);
		variables[stackOffset].setType(Operand.INT);
		iopList.add(new BinaryOP(address, currentBlock, s1, s1, BinaryOP.IREM, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lrem()
	 */
	public void visit_lrem() {
		stackOffset -= 2;
		int s1 = stackOffset - 2;
		variables[s1].setType(Operand.LONG);
		variables[stackOffset].setType(Operand.LONG);
		iopList.add(new BinaryOP(address, currentBlock, s1, s1, BinaryOP.LREM, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_frem()
	 */
	public void visit_frem() {
		stackOffset -= 1;
		int s1 = stackOffset - 1;
		variables[s1].setType(Operand.FLOAT);
		variables[stackOffset].setType(Operand.FLOAT);
		iopList.add(new BinaryOP(address, currentBlock, s1, s1, BinaryOP.FREM, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_drem()
	 */
	public void visit_drem() {
		stackOffset -= 2;
		int s1 = stackOffset - 2;
		variables[s1].setType(Operand.DOUBLE);
		variables[stackOffset].setType(Operand.DOUBLE);
		iopList.add(new BinaryOP(address, currentBlock, s1, s1, BinaryOP.DREM, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ineg()
	 */
	public void visit_ineg() {
		int s1 = stackOffset - 1;
		variables[s1].setType(Operand.INT);
		iopList.add(new UnaryOP(address, currentBlock, s1, UnaryOP.INEG, s1));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lneg()
	 */
	public void visit_lneg() {
		int s1 = stackOffset - 2;
		variables[s1].setType(Operand.LONG);
		iopList.add(new UnaryOP(address, currentBlock, s1, UnaryOP.LNEG, s1));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fneg()
	 */
	public void visit_fneg() {
		int s1 = stackOffset - 1;
		variables[s1].setType(Operand.FLOAT);
		iopList.add(new UnaryOP(address, currentBlock, s1, UnaryOP.FNEG, s1));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dneg()
	 */
	public void visit_dneg() {
		int s1 = stackOffset - 2;
		variables[s1].setType(Operand.DOUBLE);
		iopList.add(new UnaryOP(address, currentBlock, s1, UnaryOP.DNEG, s1));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ishl()
	 */
	public void visit_ishl() {
		stackOffset -= 1;
		int s1 = stackOffset - 1;
		variables[s1].setType(Operand.INT);
		variables[stackOffset].setType(Operand.INT);
		iopList.add(new BinaryOP(address, currentBlock, s1, s1, BinaryOP.ISHL, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lshl()
	 */
	public void visit_lshl() {
		stackOffset -= 1;
		int s1 = stackOffset - 2;
		variables[s1].setType(Operand.LONG);
		variables[stackOffset].setType(Operand.INT);
		iopList.add(new BinaryOP(address, currentBlock, s1, s1, BinaryOP.LSHL, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ishr()
	 */
	public void visit_ishr() {
		stackOffset -= 1;
		int s1 = stackOffset - 1;
		variables[s1].setType(Operand.INT);
		variables[stackOffset].setType(Operand.INT);
		iopList.add(new BinaryOP(address, currentBlock, s1, s1, BinaryOP.ISHR, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lshr()
	 */
	public void visit_lshr() {
		stackOffset -= 1;
		int s1 = stackOffset - 2;
		variables[s1].setType(Operand.LONG);
		variables[stackOffset].setType(Operand.INT);
		iopList.add(new BinaryOP(address, currentBlock, s1, s1, BinaryOP.LSHR, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iushr()
	 */
	public void visit_iushr() {
		stackOffset -= 1;
		int s1 = stackOffset - 1;
		variables[s1].setType(Operand.INT);
		variables[stackOffset].setType(Operand.INT);
		iopList.add(new BinaryOP(address, currentBlock, s1, s1, BinaryOP.IUSHR, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lushr()
	 */
	public void visit_lushr() {
		stackOffset -= 2;
		int s1 = stackOffset - 1;
		variables[s1].setType(Operand.INT);
		variables[stackOffset].setType(Operand.LONG);
		iopList.add(new BinaryOP(address, currentBlock, s1, s1, BinaryOP.LUSHR, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iand()
	 */
	public void visit_iand() {
		stackOffset -= 1;
		int s1 = stackOffset - 1;
		variables[s1].setType(Operand.INT);
		variables[stackOffset].setType(Operand.INT);
		iopList.add(new BinaryOP(address, currentBlock, s1, s1, BinaryOP.IAND, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_land()
	 */
	public void visit_land() {
		stackOffset -= 2;
		int s1 = stackOffset - 2;
		variables[s1].setType(Operand.LONG);
		variables[stackOffset].setType(Operand.LONG);
		iopList.add(new BinaryOP(address, currentBlock, s1, s1, BinaryOP.LAND, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ior()
	 */
	public void visit_ior() {
		stackOffset -= 1;
		int s1 = stackOffset - 1;
		variables[s1].setType(Operand.INT);
		variables[stackOffset].setType(Operand.INT);
		iopList.add(new BinaryOP(address, currentBlock, s1, s1, BinaryOP.IOR, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lor()
	 */
	public void visit_lor() {
		stackOffset -= 2;
		int s1 = stackOffset - 2;
		variables[s1].setType(Operand.INT);
		variables[stackOffset].setType(Operand.INT);
		iopList.add(new BinaryOP(address, currentBlock, s1, s1, BinaryOP.LOR, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ixor()
	 */
	public void visit_ixor() {
		stackOffset -= 1;
		int s1 = stackOffset - 1;
		variables[s1].setType(Operand.INT);
		variables[stackOffset].setType(Operand.INT);
		iopList.add(new BinaryOP(address, currentBlock, s1, s1, BinaryOP.IXOR, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lxor()
	 */
	public void visit_lxor() {
		stackOffset -= 2;
		int s1 = stackOffset - 2;
		variables[s1].setType(Operand.LONG);
		variables[stackOffset].setType(Operand.LONG);
		iopList.add(new BinaryOP(address, currentBlock, s1, s1, BinaryOP.LXOR, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iinc(int, int)
	 */
	public void visit_iinc(int index, int incValue) {
		variables[index].setType(Operand.INT);
		iopList.add(new UnaryOP(address, currentBlock, index, UnaryOP.IINC, incValue));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_i2l()
	 */
	public void visit_i2l() {
		stackOffset -= 1;
		variables[stackOffset].setType(Operand.LONG);
		iopList.add(new UnaryOP(address, currentBlock, stackOffset, UnaryOP.I2L, stackOffset));
		stackOffset += 2;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_i2f()
	 */
	public void visit_i2f() {
		stackOffset -= 1;
		variables[stackOffset].setType(Operand.FLOAT);
		iopList.add(new UnaryOP(address, currentBlock, stackOffset, UnaryOP.I2F, stackOffset));
		stackOffset += 1;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_i2d()
	 */
	public void visit_i2d() {
		stackOffset -= 1;
		variables[stackOffset].setType(Operand.DOUBLE);
		iopList.add(new UnaryOP(address, currentBlock, stackOffset, UnaryOP.I2D, stackOffset));
		stackOffset += 2;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_l2i()
	 */
	public void visit_l2i() {
		stackOffset -= 2;
		variables[stackOffset].setType(Operand.INT);
		iopList.add(new UnaryOP(address, currentBlock, stackOffset, UnaryOP.L2I, stackOffset));
		stackOffset += 1;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_l2f()
	 */
	public void visit_l2f() {
		stackOffset -= 2;
		variables[stackOffset].setType(Operand.FLOAT);
		iopList.add(new UnaryOP(address, currentBlock, stackOffset, UnaryOP.L2F, stackOffset));
		stackOffset += 1;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_l2d()
	 */
	public void visit_l2d() {
		stackOffset -= 2;
		variables[stackOffset].setType(Operand.DOUBLE);
		iopList.add(new UnaryOP(address, currentBlock, stackOffset, UnaryOP.L2D, stackOffset));
		stackOffset += 2;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_f2i()
	 */
	public void visit_f2i() {
		stackOffset -= 1;
		variables[stackOffset].setType(Operand.INT);
		iopList.add(new UnaryOP(address, currentBlock, stackOffset, UnaryOP.F2I, stackOffset));
		stackOffset += 1;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_f2l()
	 */
	public void visit_f2l() {
		stackOffset -= 1;
		variables[stackOffset].setType(Operand.LONG);
		iopList.add(new UnaryOP(address, currentBlock, stackOffset, UnaryOP.F2L, stackOffset));
		stackOffset += 2;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_f2d()
	 */
	public void visit_f2d() {
		stackOffset -= 1;
		variables[stackOffset].setType(Operand.DOUBLE);
		iopList.add(new UnaryOP(address, currentBlock, stackOffset, UnaryOP.F2D, stackOffset));
		stackOffset += 2;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_d2i()
	 */
	public void visit_d2i() {
		stackOffset -= 2;
		variables[stackOffset].setType(Operand.INT);
		iopList.add(new UnaryOP(address, currentBlock, stackOffset, UnaryOP.D2I, stackOffset));
		stackOffset += 1;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_d2l()
	 */
	public void visit_d2l() {
		stackOffset -= 2;
		variables[stackOffset].setType(Operand.LONG);
		iopList.add(new UnaryOP(address, currentBlock, stackOffset, UnaryOP.D2L, stackOffset));
		stackOffset += 2;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_d2f()
	 */
	public void visit_d2f() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_i2b()
	 */
	public void visit_i2b() {
		stackOffset -= 1;
		variables[stackOffset].setType(Operand.BYTE);
		iopList.add(new UnaryOP(address, currentBlock, stackOffset, UnaryOP.I2B, stackOffset));
		stackOffset += 1;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_i2c()
	 */
	public void visit_i2c() {
		stackOffset -= 1;
		variables[stackOffset].setType(Operand.CHAR);
		iopList.add(new UnaryOP(address, currentBlock, stackOffset, UnaryOP.I2C, stackOffset));
		stackOffset += 1;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_i2s()
	 */
	public void visit_i2s() {
		stackOffset -= 1;
		variables[stackOffset].setType(Operand.SHORT);
		iopList.add(new UnaryOP(address, currentBlock, stackOffset, UnaryOP.I2S, stackOffset));
		stackOffset += 1;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lcmp()
	 */
	public void visit_lcmp() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fcmpl()
	 */
	public void visit_fcmpl() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fcmpg()
	 */
	public void visit_fcmpg() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dcmpl()
	 */
	public void visit_dcmpl() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dcmpg()
	 */
	public void visit_dcmpg() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifeq(int)
	 */
	public void visit_ifeq(int address) {
		stackOffset -= 1;
		variables[stackOffset].setType(Operand.INT);
		iopList.add(new ConditionalBranchOP(this.address, currentBlock, stackOffset,
			ConditionalBranchOP.IFEQ, address));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifne(int)
	 */
	public void visit_ifne(int address) {
		stackOffset -= 1;
		variables[stackOffset].setType(Operand.INT);
		iopList.add(new ConditionalBranchOP(this.address, currentBlock, stackOffset,
			ConditionalBranchOP.IFNE, address));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iflt(int)
	 */
	public void visit_iflt(int address) {
		stackOffset -= 1;
		variables[stackOffset].setType(Operand.INT);
		iopList.add(new ConditionalBranchOP(this.address, currentBlock, stackOffset,
			ConditionalBranchOP.IFLT, address));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifge(int)
	 */
	public void visit_ifge(int address) {
		stackOffset -= 1;
		variables[stackOffset].setType(Operand.INT);
		iopList.add(new ConditionalBranchOP(this.address, currentBlock, stackOffset,
			ConditionalBranchOP.IFGE, address));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifgt(int)
	 */
	public void visit_ifgt(int address) {
		stackOffset -= 1;
		variables[stackOffset].setType(Operand.INT);
		iopList.add(new ConditionalBranchOP(this.address, currentBlock, stackOffset,
			ConditionalBranchOP.IFGT, address));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifle(int)
	 */
	public void visit_ifle(int address) {
		stackOffset -= 1;
		variables[stackOffset].setType(Operand.INT);
		iopList.add(new ConditionalBranchOP(this.address, currentBlock, stackOffset,
			ConditionalBranchOP.IFLE, address));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmpeq(int)
	 */
	public void visit_if_icmpeq(int address) {
		stackOffset -= 2;
		int s1 = stackOffset + 1;
		variables[s1].setType(Operand.INT);
		variables[stackOffset].setType(Operand.INT);
		iopList.add(new ConditionalBranchOP(this.address, currentBlock, s1,
			ConditionalBranchOP.IF_ICMPEQ, stackOffset, address));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmpne(int)
	 */
	public void visit_if_icmpne(int address) {
		stackOffset -= 2;
		int s1 = stackOffset + 1;
		variables[s1].setType(Operand.INT);
		variables[stackOffset].setType(Operand.INT);
		iopList.add(new ConditionalBranchOP(this.address, currentBlock, s1,
			ConditionalBranchOP.IF_ICMPNE, stackOffset, address));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmplt(int)
	 */
	public void visit_if_icmplt(int address) {
		stackOffset -= 2;
		int s1 = stackOffset + 1;
		variables[s1].setType(Operand.INT);
		variables[stackOffset].setType(Operand.INT);
		iopList.add(new ConditionalBranchOP(this.address, currentBlock, s1,
			ConditionalBranchOP.IF_ICMPLT, stackOffset, address));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmpge(int)
	 */
	public void visit_if_icmpge(int address) {
		stackOffset -= 2;
		int s1 = stackOffset + 1;
		variables[s1].setType(Operand.INT);
		variables[stackOffset].setType(Operand.INT);
		iopList.add(new ConditionalBranchOP(this.address, currentBlock, s1,
			ConditionalBranchOP.IF_ICMPGE, stackOffset, address));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmpgt(int)
	 */
	public void visit_if_icmpgt(int address) {
		stackOffset -= 2;
		int s1 = stackOffset + 1;
		variables[s1].setType(Operand.INT);
		variables[stackOffset].setType(Operand.INT);
		iopList.add(new ConditionalBranchOP(this.address, currentBlock, s1,
			ConditionalBranchOP.IF_ICMPGT, stackOffset, address));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmple(int)
	 */
	public void visit_if_icmple(int address) {
		stackOffset -= 2;
		int s1 = stackOffset + 1;
		variables[s1].setType(Operand.INT);
		variables[stackOffset].setType(Operand.INT);
		iopList.add(new ConditionalBranchOP(this.address, currentBlock, stackOffset,
			ConditionalBranchOP.IF_ICMPLE, s1, address));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_acmpeq(int)
	 */
	public void visit_if_acmpeq(int address) {
		stackOffset -= 2;
		int s1 = stackOffset + 1;
		variables[s1].setType(Operand.REFERENCE);
		variables[stackOffset].setType(Operand.REFERENCE);
		iopList.add(new ConditionalBranchOP(this.address, currentBlock, stackOffset,
			ConditionalBranchOP.IF_ACMPEQ, s1, address));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_acmpne(int)
	 */
	public void visit_if_acmpne(int address) {
		stackOffset -= 2;
		int s1 = stackOffset + 1;
		variables[s1].setType(Operand.REFERENCE);
		variables[stackOffset].setType(Operand.REFERENCE);
		iopList.add(new ConditionalBranchOP(this.address, currentBlock, stackOffset,
			ConditionalBranchOP.IF_ACMPNE, s1, address));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_goto(int)
	 */
	public void visit_goto(int address) {
		iopList.add(new UnconditionalBranchOP(this.address, currentBlock, address));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_jsr(int)
	 */
	public void visit_jsr(int address) {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ret(int)
	 */
	public void visit_ret(int index) {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_tableswitch(int, int, int, int[])
	 */
	public void visit_tableswitch(int defValue, int lowValue, int highValue, int[] addresses) {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lookupswitch(int, int[], int[])
	 */
	public void visit_lookupswitch(int defValue, int[] matchValues, int[] addresses) {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ireturn()
	 */
	public void visit_ireturn() {
		stackOffset -= 1;
		variables[stackOffset].setType(Operand.INT);
		iopList.add(new VarReturnOP(address, currentBlock, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lreturn()
	 */
	public void visit_lreturn() {
		stackOffset -= 2;
		variables[stackOffset].setType(Operand.LONG);
		iopList.add(new VarReturnOP(address, currentBlock, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_freturn()
	 */
	public void visit_freturn() {
		stackOffset -= 1;
		variables[stackOffset].setType(Operand.FLOAT);
		iopList.add(new VarReturnOP(address, currentBlock, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dreturn()
	 */
	public void visit_dreturn() {
		stackOffset -= 2;
		variables[stackOffset].setType(Operand.DOUBLE);
		iopList.add(new VarReturnOP(address, currentBlock, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_areturn()
	 */
	public void visit_areturn() {
		stackOffset -= 1;
		variables[stackOffset].setType(Operand.REFERENCE);
		iopList.add(new VarReturnOP(address, currentBlock, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_return()
	 */
	public void visit_return() {
		iopList.add(new VoidReturnOP(address, currentBlock));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_getstatic(org.jnode.vm.classmgr.VmConstFieldRef)
	 */
	public void visit_getstatic(VmConstFieldRef fieldRef) {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_putstatic(org.jnode.vm.classmgr.VmConstFieldRef)
	 */
	public void visit_putstatic(VmConstFieldRef fieldRef) {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_getfield(org.jnode.vm.classmgr.VmConstFieldRef)
	 */
	public void visit_getfield(VmConstFieldRef fieldRef) {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_putfield(org.jnode.vm.classmgr.VmConstFieldRef)
	 */
	public void visit_putfield(VmConstFieldRef fieldRef) {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_invokevirtual(org.jnode.vm.classmgr.VmConstMethodRef)
	 */
	public void visit_invokevirtual(VmConstMethodRef methodRef) {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_invokespecial(org.jnode.vm.classmgr.VmConstMethodRef)
	 */
	public void visit_invokespecial(VmConstMethodRef methodRef) {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_invokestatic(org.jnode.vm.classmgr.VmConstMethodRef)
	 */
	public void visit_invokestatic(VmConstMethodRef methodRef) {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_invokeinterface(org.jnode.vm.classmgr.VmConstIMethodRef, int)
	 */
	public void visit_invokeinterface(VmConstIMethodRef methodRef, int count) {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_new(org.jnode.vm.classmgr.VmConstClass)
	 */
	public void visit_new(VmConstClass clazz) {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_newarray(int)
	 */
	public void visit_newarray(int type) {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_anewarray(org.jnode.vm.classmgr.VmConstClass)
	 */
	public void visit_anewarray(VmConstClass clazz) {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_arraylength()
	 */
	public void visit_arraylength() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_athrow()
	 */
	public void visit_athrow() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_checkcast(org.jnode.vm.classmgr.VmConstClass)
	 */
	public void visit_checkcast(VmConstClass clazz) {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_instanceof(org.jnode.vm.classmgr.VmConstClass)
	 */
	public void visit_instanceof(VmConstClass clazz) {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_monitorenter()
	 */
	public void visit_monitorenter() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_monitorexit()
	 */
	public void visit_monitorexit() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_multianewarray(org.jnode.vm.classmgr.VmConstClass, int)
	 */
	public void visit_multianewarray(VmConstClass clazz, int dimensions) {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifnull(int)
	 */
	public void visit_ifnull(int address) {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifnonnull(int)
	 */
	public void visit_ifnonnull(int address) {
		throw new IllegalArgumentException("byte code not yet supported");
	}
	
	public static void main(String args[]) throws SecurityException, IOException, ClassNotFoundException {
		String className = "org.jnode.vm.compiler.ir.IRGenerator";
		if (args.length > 0) {
			className = args[0];
		}
		VmClassLoader vmc = new VmClassLoader(new File(".").toURL(), new VmX86Architecture());
		VmType type = vmc.loadClass(className, true);
		VmMethod arithMethod = null;
		int nMethods = type.getNoDeclaredMethods();
		for (int i=0; i<nMethods; i+=1) {
			VmMethod method = type.getDeclaredMethod(i);
			if ("arithOpt".equals(method.getName())) {
				arithMethod = method;
				break;
			}
		}
		VmByteCode code = arithMethod.getBytecode();
		BytecodeViewer bv = new BytecodeViewer();
		BytecodeParser.parse(code, bv);

		System.out.println();
		IRControlFlowGraph cfg = new IRControlFlowGraph(code);
		System.out.println(cfg.toString());

		System.out.println();		
		IRGenerator irg = new IRGenerator(cfg);
		BytecodeParser.parse(code, irg);
		ArrayList iops = irg.getIOPList();
		int n = iops.size();
		boolean printDeadCode = false;
		boolean printDetail = false;
		for (int i=0; i<n; i+=1) {
			IOP iop = (IOP) iops.get(i);
			if (printDeadCode && iop.isDeadCode()) {
				if (printDetail) {
					printIOPDetail(iop);
				}
				System.out.println(iop);
			}
			if (!iop.isDeadCode()) {
				if (printDetail) {
					printIOPDetail(iop);
				}
				System.out.println(iop);
			}
		}
	}

	public static void printIOPDetail(IOP iop) {
		System.out.print(iop.getBasicBlock());
		System.out.print(" ");
		Variable[] vars = iop.getBasicBlock().getVariables();
		System.out.print("[");
		for (int j=0; j<vars.length; j+=1) {
			System.out.print(vars[j]);
			System.out.print(",");
		}
		System.out.print("] ");
		if (iop.isDeadCode()) {
			System.out.print("(dead) ");
		}
	}

	public static int arithOpt(int a, int b, int c) {
		int result = 1;
		if (a == 0) {
			result = 2;
		}
		if (a == 1) {
			result = 3;
		}
		if (a == 2) {
			result = 3;
		}
		return result;
	}
}
