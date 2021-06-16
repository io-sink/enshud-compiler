package enshud.interlanguage.ilstatement;

import java.util.HashSet;

import enshud.interlanguage.iloperand.AbstractILOperand;
import enshud.interlanguage.iloperand.AbstractILVariableOperand;
import enshud.interlanguage.iloperand.ILConstantOperand;
import enshud.interlanguage.iloperand.ILSimpleVariableOperand;
import enshud.typeexpression.AbstractType;
import enshud.typeexpression.SimpleType;

public class ILAssign1Statement extends AbstractILStatement {

	public enum OperationType {
		MINUS, NOT
	}

	public AbstractILVariableOperand leftHandSide;
	public OperationType operationType;
	public AbstractILOperand operand;
	public AbstractType typeExpression;


	public ILAssign1Statement(
			AbstractILVariableOperand leftHandSide,
			OperationType operationType,
			AbstractILVariableOperand operand,
			AbstractType typeExpression) {

		this.leftHandSide = leftHandSide;
		this.operationType = operationType;
		this.operand = operand;
		this.typeExpression = typeExpression;
	}

	@Override
	public String toString() {
		String operator = null;
		if (operationType == OperationType.MINUS)
			operator = "-";
		else if (operationType == OperationType.NOT)
			operator = "not";

		return String.format("%s := %s %s", leftHandSide, operator, operand);
	}

	@Override
	public HashSet<ILSimpleVariableOperand> getRefSet() {
		var refSet = new HashSet<ILSimpleVariableOperand>();
		if(operand instanceof ILSimpleVariableOperand)
			refSet.add((ILSimpleVariableOperand)operand);

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
		return true;
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

	@Override
	public void replaceOperandDefinition(AbstractILVariableOperand oldOperand, AbstractILVariableOperand newOperand) {
		if(leftHandSide.equals(oldOperand))
			leftHandSide = newOperand;
	}

	@Override
	public boolean isConstant() {
		return operand instanceof ILConstantOperand;
	}

	@Override
	public ILConstantOperand getConstantValue() {
		if(!isConstant())
			throw new RuntimeException();

		var constantOperand = (ILConstantOperand)operand;

		if(operationType == OperationType.MINUS) {
			int constantValue = -constantOperand.getIntValue();
			return new ILConstantOperand(Integer.toString(constantValue), SimpleType.INTEGER);

		} else /* NOT */ {
			boolean constantValue = !constantOperand.getBooleanValue();
			return new ILConstantOperand(constantValue ? "true" : "false", SimpleType.BOOLEAN);
		}
	}

}
