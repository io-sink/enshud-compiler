package enshud.interlanguage.ilstatement;

import java.util.HashSet;

import enshud.interlanguage.iloperand.AbstractILOperand;
import enshud.interlanguage.iloperand.AbstractILVariableOperand;
import enshud.interlanguage.iloperand.ILConstantOperand;
import enshud.interlanguage.iloperand.ILSimpleVariableOperand;
import enshud.typeexpression.AbstractType;
import enshud.typeexpression.SimpleType;

public class ILAssign2Statement extends AbstractILStatement {

	public enum OperationType {
		PLUS, MINUS, OR, MUL, DIV, MOD, AND,
		EQ, NEQ, LS, LEQ, GR, GEQ
	}

	public AbstractILVariableOperand leftHandSide;
	public OperationType operationType;
	public AbstractILOperand leftOperand;
	public AbstractILOperand rightOperand;
	public AbstractType typeExpression;


	public ILAssign2Statement(
			AbstractILVariableOperand leftHandSide,
			OperationType operationType,
			AbstractILVariableOperand leftOperand,
			AbstractILVariableOperand rightOperand,
			AbstractType typeExpression) {

		this.leftHandSide = leftHandSide;
		this.operationType = operationType;
		this.leftOperand = leftOperand;
		this.rightOperand = rightOperand;
		this.typeExpression = typeExpression;

	}

	@Override
	public String toString() {
		String operator = null;
		if (operationType == OperationType.PLUS)
			operator = "+";
		else if (operationType == OperationType.MINUS)
			operator = "-";
		else if (operationType == OperationType.OR)
			operator = "||";
		else if (operationType == OperationType.MUL)
			operator = "*";
		else if (operationType == OperationType.DIV)
			operator = "/";
		else if (operationType == OperationType.MOD)
			operator = "%";
		else if (operationType == OperationType.AND)
			operator = "&&";
		else if (operationType == OperationType.EQ)
			operator = "==";
		else if (operationType == OperationType.NEQ)
			operator = "!=";
		else if (operationType == OperationType.LS)
			operator = "<";
		else if (operationType == OperationType.LEQ)
			operator = "<=";
		else if (operationType == OperationType.GR)
			operator = ">";
		else if (operationType == OperationType.GEQ)
			operator = ">=";

		return String.format("%s := %s %s %s", leftHandSide, leftOperand, operator, rightOperand);
	}

	@Override
	public HashSet<ILSimpleVariableOperand> getRefSet() {
		var refSet = new HashSet<ILSimpleVariableOperand>();
		if(leftOperand instanceof ILSimpleVariableOperand)
			refSet.add((ILSimpleVariableOperand)leftOperand);
		if(rightOperand instanceof ILSimpleVariableOperand)
			refSet.add((ILSimpleVariableOperand)rightOperand);

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
		if(leftOperand.equals(oldOperand))
			leftOperand = newOperand;
		if(rightOperand.equals(oldOperand))
			rightOperand = newOperand;

	}

	@Override
	public void replaceOperandDefinition(AbstractILVariableOperand oldOperand, AbstractILVariableOperand newOperand) {
		if(leftHandSide.equals(oldOperand))
			leftHandSide = newOperand;
	}

	@Override
	public boolean isConstant() {
		return leftOperand instanceof ILConstantOperand &&
				rightOperand instanceof ILConstantOperand;
	}

	@Override
	public ILConstantOperand getConstantValue() {
		if(!isConstant())
			throw new RuntimeException();

		var leftConstantOperand = (ILConstantOperand)leftOperand;
		var rightConstantOperand = (ILConstantOperand)rightOperand;
		var operandType = leftConstantOperand.typeExpression;

		ILConstantOperand resultConstant = null;

		if (operationType == OperationType.PLUS) {
			int constantValue = leftConstantOperand.getIntValue() + rightConstantOperand.getIntValue();
			resultConstant = new ILConstantOperand(Integer.toString(constantValue), SimpleType.INTEGER);

		} else if (operationType == OperationType.MINUS) {
			int constantValue = leftConstantOperand.getIntValue() - rightConstantOperand.getIntValue();
			resultConstant = new ILConstantOperand(Integer.toString(constantValue), SimpleType.INTEGER);

		} else if (operationType == OperationType.MUL) {
			int constantValue = leftConstantOperand.getIntValue() * rightConstantOperand.getIntValue();
			resultConstant = new ILConstantOperand(Integer.toString(constantValue), SimpleType.INTEGER);

		} else if (operationType == OperationType.DIV) {
			int constantValue = leftConstantOperand.getIntValue() / rightConstantOperand.getIntValue();
			resultConstant = new ILConstantOperand(Integer.toString(constantValue), SimpleType.INTEGER);

		} else if (operationType == OperationType.MOD) {
			int constantValue = leftConstantOperand.getIntValue() % rightConstantOperand.getIntValue();
			resultConstant = new ILConstantOperand(Integer.toString(constantValue), SimpleType.INTEGER);

		} else if (operationType == OperationType.OR) {
			boolean constantValue = leftConstantOperand.getBooleanValue() || rightConstantOperand.getBooleanValue();
			resultConstant = new ILConstantOperand(constantValue ? "true" : "false", SimpleType.BOOLEAN);

		} else if (operationType == OperationType.AND) {
			boolean constantValue = leftConstantOperand.getBooleanValue() && rightConstantOperand.getBooleanValue();
			resultConstant = new ILConstantOperand(constantValue ? "true" : "false", SimpleType.BOOLEAN);

		} else if (operationType == OperationType.EQ &&
				(operandType.equals(SimpleType.INTEGER) || operandType.equals(SimpleType.CHAR))) {
			boolean constantValue = leftConstantOperand.getIntValue() == rightConstantOperand.getIntValue();
			resultConstant = new ILConstantOperand(constantValue ? "true" : "false", SimpleType.BOOLEAN);

		} else if (operationType == OperationType.NEQ &&
				(operandType.equals(SimpleType.INTEGER) || operandType.equals(SimpleType.CHAR))) {
			boolean constantValue = leftConstantOperand.getIntValue() != rightConstantOperand.getIntValue();
			resultConstant = new ILConstantOperand(constantValue ? "true" : "false", SimpleType.BOOLEAN);

		} else if (operationType == OperationType.LS &&
				(operandType.equals(SimpleType.INTEGER) || operandType.equals(SimpleType.CHAR))) {
			boolean constantValue = leftConstantOperand.getIntValue() < rightConstantOperand.getIntValue();
			resultConstant = new ILConstantOperand(constantValue ? "true" : "false", SimpleType.BOOLEAN);

		} else if (operationType == OperationType.LEQ &&
				(operandType.equals(SimpleType.INTEGER) || operandType.equals(SimpleType.CHAR))) {
			boolean constantValue = leftConstantOperand.getIntValue() <= rightConstantOperand.getIntValue();
			resultConstant = new ILConstantOperand(constantValue ? "true" : "false", SimpleType.BOOLEAN);

		} else if (operationType == OperationType.GR &&
				(operandType.equals(SimpleType.INTEGER) || operandType.equals(SimpleType.CHAR))) {
			boolean constantValue = leftConstantOperand.getIntValue() > rightConstantOperand.getIntValue();
			resultConstant = new ILConstantOperand(constantValue ? "true" : "false", SimpleType.BOOLEAN);

		} else if (operationType == OperationType.GEQ &&
				(operandType.equals(SimpleType.INTEGER) || operandType.equals(SimpleType.CHAR))) {
			boolean constantValue = leftConstantOperand.getIntValue() >= rightConstantOperand.getIntValue();
			resultConstant = new ILConstantOperand(constantValue ? "true" : "false", SimpleType.BOOLEAN);

		} else if (operationType == OperationType.EQ && operandType.equals(SimpleType.BOOLEAN)) {
			boolean constantValue = !(leftConstantOperand.getBooleanValue() ^ rightConstantOperand.getBooleanValue());
			resultConstant = new ILConstantOperand(constantValue ? "true" : "false", SimpleType.BOOLEAN);

		} else if (operationType == OperationType.NEQ && operandType.equals(SimpleType.BOOLEAN)) {
			boolean constantValue = leftConstantOperand.getBooleanValue() ^ rightConstantOperand.getBooleanValue();
			resultConstant = new ILConstantOperand(constantValue ? "true" : "false", SimpleType.BOOLEAN);

		} else if (operationType == OperationType.LS && operandType.equals(SimpleType.BOOLEAN)) {
			boolean constantValue = !leftConstantOperand.getBooleanValue() && rightConstantOperand.getBooleanValue();
			resultConstant = new ILConstantOperand(constantValue ? "true" : "false", SimpleType.BOOLEAN);

		} else if (operationType == OperationType.LEQ && operandType.equals(SimpleType.BOOLEAN)) {
			boolean constantValue = !leftConstantOperand.getBooleanValue() || rightConstantOperand.getBooleanValue();
			resultConstant = new ILConstantOperand(constantValue ? "true" : "false", SimpleType.BOOLEAN);

		} else if (operationType == OperationType.GR && operandType.equals(SimpleType.BOOLEAN)) {
			boolean constantValue = leftConstantOperand.getBooleanValue() && !rightConstantOperand.getBooleanValue();
			resultConstant = new ILConstantOperand(constantValue ? "true" : "false", SimpleType.BOOLEAN);

		} else if (operationType == OperationType.GEQ && operandType.equals(SimpleType.BOOLEAN)) {
			boolean constantValue = leftConstantOperand.getBooleanValue() || !rightConstantOperand.getBooleanValue();
			resultConstant = new ILConstantOperand(constantValue ? "true" : "false", SimpleType.BOOLEAN);

		}

		if(resultConstant == null)
			throw new RuntimeException();
		return resultConstant;
	}
}
