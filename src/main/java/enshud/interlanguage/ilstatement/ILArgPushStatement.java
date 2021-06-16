package enshud.interlanguage.ilstatement;

import java.util.HashSet;

import enshud.interlanguage.iloperand.AbstractILOperand;
import enshud.interlanguage.iloperand.ILSimpleVariableOperand;

public class ILArgPushStatement extends AbstractILStatement {

	public AbstractILOperand operand;


	public ILArgPushStatement(AbstractILOperand operand) {
		this.operand = operand;
	}

	@Override
	public String toString() {
		return String.format("param %s", operand);
	}

	@Override
	public HashSet<ILSimpleVariableOperand> getRefSet() {
		var refSet = new HashSet<ILSimpleVariableOperand>();
		if(operand instanceof ILSimpleVariableOperand)
			refSet.add((ILSimpleVariableOperand)operand);

		return refSet;
	}

	@Override
	public boolean isOperandReplaceableByConstant() {
		return true;
	}

	@Override
	public void replaceOperandReference(AbstractILOperand oldOperand, AbstractILOperand newOperand) {
		if(operand.equals(oldOperand))
			operand = newOperand;
	}
}
