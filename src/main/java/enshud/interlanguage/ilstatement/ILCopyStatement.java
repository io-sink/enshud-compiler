package enshud.interlanguage.ilstatement;

import java.util.HashSet;

import enshud.interlanguage.iloperand.AbstractILOperand;
import enshud.interlanguage.iloperand.AbstractILVariableOperand;
import enshud.interlanguage.iloperand.ILConstantOperand;
import enshud.interlanguage.iloperand.ILIndexedVariableOperand;
import enshud.interlanguage.iloperand.ILSimpleVariableOperand;
import enshud.typeexpression.AbstractType;

public class ILCopyStatement extends AbstractILStatement {

	public AbstractILVariableOperand leftHandSide;
	public AbstractILOperand rightHandSide;
	public AbstractType typeExpression;


	public ILCopyStatement(
			AbstractILVariableOperand leftHandSide,
			AbstractILOperand rightHandSide,
			AbstractType typeExpression) {

		this.leftHandSide = leftHandSide;
		this.rightHandSide = rightHandSide;
		this.typeExpression = typeExpression;

	}

	@Override
	public String toString() {
		return String.format("%s := %s", leftHandSide, rightHandSide);
	}

	@Override
	public HashSet<ILSimpleVariableOperand> getRefSet() {
		var refSet = new HashSet<ILSimpleVariableOperand>();
		if(rightHandSide instanceof ILSimpleVariableOperand)
			refSet.add((ILSimpleVariableOperand)rightHandSide);
		else if(rightHandSide instanceof ILIndexedVariableOperand)
			refSet.add(((ILIndexedVariableOperand)rightHandSide).index);
		if(leftHandSide instanceof ILIndexedVariableOperand)
			refSet.add(((ILIndexedVariableOperand)leftHandSide).index);

		return refSet;
	}

	@Override
	public HashSet<ILSimpleVariableOperand> getDefSet() {
		var defSet = new HashSet<ILSimpleVariableOperand>();
		if(leftHandSide instanceof ILSimpleVariableOperand)
			defSet.add((ILSimpleVariableOperand)leftHandSide);

		return defSet;
	}

	@Override
	public boolean isRemovable() {
		return leftHandSide instanceof ILSimpleVariableOperand;
	}

	@Override
	public boolean isOperandReplaceableByConstant() {
		return leftHandSide instanceof ILSimpleVariableOperand &&
				rightHandSide instanceof ILSimpleVariableOperand;
	}

	@Override
	public void replaceOperandReference(AbstractILOperand oldOperand, AbstractILOperand newOperand) {
		if(rightHandSide.equals(oldOperand))
			rightHandSide = newOperand;
	}

	@Override
	public void replaceOperandDefinition(AbstractILVariableOperand oldOperand, AbstractILVariableOperand newOperand) {
		if(leftHandSide.equals(oldOperand))
			leftHandSide = newOperand;
	}


	@Override
	public boolean isConstant() {
		return rightHandSide instanceof ILConstantOperand;
	}

	@Override
	public ILConstantOperand getConstantValue() {
		if(!isConstant())
			throw new RuntimeException();

		if(isConstant())
			return (ILConstantOperand)rightHandSide;
		else
			throw new RuntimeException();
	}

}
