package enshud.interlanguage.ilstatement;

import java.util.HashSet;

import enshud.interlanguage.iloperand.AbstractILOperand;
import enshud.interlanguage.iloperand.ILSimpleVariableOperand;

public class ILConditionalJump extends AbstractILStatement {

	public AbstractILOperand condition;
	public String labelName;

	public ILConditionalJump(AbstractILOperand condition, String labelName) {
		this.condition = condition;
		this.labelName = labelName;
	}

	@Override
	public String toString() {
		return String.format("if %s goto %s", condition, labelName);
	}

	@Override
	public HashSet<ILSimpleVariableOperand> getRefSet() {
		var refSet = new HashSet<ILSimpleVariableOperand>();
		if(condition instanceof ILSimpleVariableOperand)
			refSet.add((ILSimpleVariableOperand)condition);

		return refSet;
	}

	@Override
	public boolean isOperandReplaceableByConstant() {
		return true;
	}

	@Override
	public void replaceOperandReference(AbstractILOperand oldOperand, AbstractILOperand newOperand) {
		if(condition.equals(oldOperand))
			condition = newOperand;
	}

}
