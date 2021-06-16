package enshud.interlanguage.ilstatement;

import java.util.HashSet;

import enshud.interlanguage.iloperand.AbstractILOperand;
import enshud.interlanguage.iloperand.AbstractILVariableOperand;
import enshud.interlanguage.iloperand.ILIndexedVariableOperand;
import enshud.interlanguage.iloperand.ILSimpleVariableOperand;

public class ILReadStatement extends AbstractILStatement {

	public AbstractILOperand operand;

	public ILReadStatement(AbstractILOperand operand) {
		this.operand = operand;

	}

	@Override
	public String toString() {
		return String.format("read %s", operand);
	}

	@Override
	public HashSet<ILSimpleVariableOperand> getRefSet() {
		var refSet = new HashSet<ILSimpleVariableOperand>();
		if(operand instanceof ILIndexedVariableOperand)
			refSet.add(((ILIndexedVariableOperand)operand).index);

		return refSet;
	}

	@Override
	public HashSet<ILSimpleVariableOperand> getDefSet() {
		var defSet = new HashSet<ILSimpleVariableOperand>();
		if(operand instanceof ILSimpleVariableOperand)
			defSet.add((ILSimpleVariableOperand)operand);

		return defSet;
	}

	@Override
	public void replaceOperandDefinition(AbstractILVariableOperand oldOperand, AbstractILVariableOperand newOperand) {
		if(operand.equals(oldOperand))
			operand = newOperand;
	}

}
