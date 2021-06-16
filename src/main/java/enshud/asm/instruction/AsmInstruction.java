package enshud.asm.instruction;

import java.util.ArrayList;

import enshud.asm.operand.IAsmOperand;
import enshud.asm.operand.RegisterOperand;
import enshud.interlanguage.ilstatement.ILAssign2Statement;

public class AsmInstruction implements IAsmInstruction {
	public enum Operation {
		LD, ST, LAD,
		ADDA, ADDL, SUBA, SUBL,
		AND, OR, XOR,
		CPA, CPL,
		SLA, SRA, SLL, SRL,
		JPL, JMI, JNZ, JZE, JOV, JUMP,
		PUSH, POP, RPUSH, RPOP,
		CALL, RET,
		SVC, NOP,
		START, END,
		DS, DC,
		IN, OUT;

		public static Operation initialize(ILAssign2Statement.OperationType ilOperation) {
			switch(ilOperation) {
			case PLUS:
				return Operation.ADDA;
			case MINUS:
				return Operation.SUBA;
			case OR:
				return Operation.OR;
			case AND:
				return Operation.AND;
			case EQ:
			case NEQ:
			case LS:
			case LEQ:
			case GR:
			case GEQ:
				return Operation.CPA;

				default:
					throw new RuntimeException();
			}
		}
	}

	public Operation operation;
	public ArrayList<IAsmOperand> operands;

	public AsmInstruction(Operation operation, IAsmOperand[] operands) {
		this.operation = operation;
		this.operands = new ArrayList<IAsmOperand>();
		if(operands != null)
			for(var operand : operands)
				this.operands.add(operand);
	}

	@Override
	public String toString() {
		String strOperands = "";
		for(var operand : operands) {
			if(strOperands.length() > 0)
				strOperands += ", ";
			strOperands += operand.toString();
		}
		return String.format("%s %s", operation.toString(), strOperands);
	}

	public boolean isMeaningful() {
		// 同じレジスタ同士のLDは無視
		if(operation == Operation.LD &&
				operands.get(0) instanceof RegisterOperand &&
				operands.get(1) instanceof RegisterOperand &&
				((RegisterOperand)operands.get(0)).equals((RegisterOperand)operands.get(1)))
			return false;

		return true;
	}

}
