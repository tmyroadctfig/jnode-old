/**
 * $Id$  
 */
package org.jnode.jnasm.assembler;

import java.util.List;

/**
 * @author Levente S\u00e1ntha (lsantha@users.sourceforge.net)
 */
public class Instruction {
    private int lineNumber;
    private String sizeInfo;
    private String label;
    private String mnemonic;
    private List operands;

    public Instruction(String label) {
        this(label, null, null);
    }

    public Instruction(String mnemonic, List operands) {
        this(null, mnemonic, operands);
    }

    public Instruction(String label, String mnemonic, List operands) {
        this.label = label;
        this.mnemonic = mnemonic;
        this.operands = operands;
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public List getOperands() {
        return operands;
    }

    public void setOperands(List operands) {
        this.operands = operands;
    }

    public String getLabel(){
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getSizeInfo() {
        return sizeInfo;
    }

    public void setSizeInfo(String sizeInfo) {
        this.sizeInfo = sizeInfo;
    }
}
