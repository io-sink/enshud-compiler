package enshud.s4.asmgenerator.generators;

import java.util.HashMap;

import enshud.asm.AsmProgram;
import enshud.asm.instruction.AsmInstruction;
import enshud.asm.operand.ConstantOperand;
import enshud.asm.operand.IAsmOperand;
import enshud.asm.operand.RegisterOperand;
import enshud.interlanguage.iloperand.AbstractILOperand;
import enshud.interlanguage.iloperand.ILConstantOperand;
import enshud.interlanguage.iloperand.ILSimpleVariableOperand;
import enshud.interlanguage.ilstatement.AbstractILStatement;
import enshud.interlanguage.ilstatement.ILWriteStatement;
import enshud.s4.asmgenerator.BiHashMap;
import enshud.s4.asmgenerator.StackPointerRevision;
import enshud.symboltable.SymbolTableStack;
import enshud.typeexpression.AbstractType;
import enshud.typeexpression.ArrayType;
import enshud.typeexpression.SimpleType;

public class WriteStatementAsmGenerator extends AbstractAsmGenerator {

	private HashMap<String, String> constantAllocation;

	public WriteStatementAsmGenerator(
			AsmProgram asmProgram,
			BiHashMap<String, Integer> currentRegisterAllocation,
			HashMap<String, String> constantAllocation,
			StackPointerRevision stackPointerRevision) {

		super(asmProgram, currentRegisterAllocation, stackPointerRevision);
		this.constantAllocation = constantAllocation;
	}

	@Override
	public void generate(
			AbstractILStatement ilStatement,
			SymbolTableStack symbolTables,
			HashMap<AbstractILOperand, Integer> allocativePlan,
			BiHashMap<String, Integer> currentAllocation) {

		// 3番地コードのwriteは単独で1つの基本ブロックなので，レジスタは割り付けずに自由に使う

		var specificStatement = (ILWriteStatement)ilStatement;
		var operand = specificStatement.operand;
		if(operand instanceof ILSimpleVariableOperand) {
			// 変数
			var symbolTableEntry = symbolTables.findVariableFromAnyTable(((ILSimpleVariableOperand)operand).variableName);

			AbstractType operandType = symbolTableEntry.typeExpression;

			if(operandType.equals(SimpleType.INTEGER)) {
				// 整数

				// GR2に値をロード
				addVariableInstruction(
						symbolTableEntry,
						AsmInstruction.Operation.LD,
						RegisterOperand.GR2);

				asmProgram.add(new AsmInstruction(
						AsmInstruction.Operation.CALL,
						new IAsmOperand[] {
								new ConstantOperand("WRTINT")
						}));

			} else if(operandType.equals(SimpleType.CHAR)) {
				// 文字

				// GR2に値をロード
				addVariableInstruction(
						symbolTableEntry,
						AsmInstruction.Operation.LD,
						RegisterOperand.GR2);

				asmProgram.add(new AsmInstruction(
						AsmInstruction.Operation.CALL,
						new IAsmOperand[] {
								new ConstantOperand("WRTCH")
						}));

			} else if(operandType instanceof ArrayType &&
					((ArrayType)operandType).contentType.equals(SimpleType.CHAR)) {
				// 文字列

				// GR1に文字数をロード
				asmProgram.add(new AsmInstruction(
						AsmInstruction.Operation.LAD,
						new IAsmOperand[] {
								RegisterOperand.GR1,
								new ConstantOperand(symbolTableEntry.size)
								}));

				// GR2にアドレスをロード
				addVariableInstruction(
						symbolTableEntry,
						AsmInstruction.Operation.LAD,
						RegisterOperand.GR2);

				asmProgram.add(new AsmInstruction(
						AsmInstruction.Operation.CALL,
						new IAsmOperand[] {
								new ConstantOperand("WRTSTR")
						}));
			}

		} else {
			// 定数
			var constantOperand = (ILConstantOperand)operand;

			if(constantOperand.typeExpression.equals(SimpleType.INTEGER)) {

				// GR2に値をロード
				int constantValue = constantOperand.getIntValue();
				asmProgram.add(new AsmInstruction(
						AsmInstruction.Operation.LAD,
						new IAsmOperand[] {
								RegisterOperand.GR2,
								new ConstantOperand(constantValue)
						}));

				asmProgram.add(new AsmInstruction(
						AsmInstruction.Operation.CALL,
						new IAsmOperand[] {
								new ConstantOperand("WRTINT")
						}));

			} else if(constantOperand.typeExpression.equals(SimpleType.CHAR)) {

				// GR2に値をロード
				int constantValue = constantOperand.getIntValue();
				asmProgram.add(new AsmInstruction(
						AsmInstruction.Operation.LAD,
						new IAsmOperand[] {
								RegisterOperand.GR2,
								new ConstantOperand(constantValue)
						}));

				asmProgram.add(new AsmInstruction(
						AsmInstruction.Operation.CALL,
						new IAsmOperand[] {
								new ConstantOperand("WRTCH")
						}));

			} else /* array of char */ {

				String stringLabel = constantAllocation.get(constantOperand.value);
				int stringSize = constantOperand.value.length() - 2;

				// GR1に文字数をロード
				asmProgram.add(new AsmInstruction(
						AsmInstruction.Operation.LAD,
						new IAsmOperand[] {
								RegisterOperand.GR1,
								new ConstantOperand(stringSize)
								}));

				// GR2にアドレスをロード
				asmProgram.add(new AsmInstruction(
						AsmInstruction.Operation.LAD,
						new IAsmOperand[] {
								RegisterOperand.GR2,
								new ConstantOperand(stringLabel)
								}));

				asmProgram.add(new AsmInstruction(
						AsmInstruction.Operation.CALL,
						new IAsmOperand[] {
								new ConstantOperand("WRTSTR")
						}));

			}

		}
	}


}
