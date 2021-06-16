package enshud.s4.asmgenerator.generators;

import java.util.HashMap;

import enshud.asm.AsmProgram;
import enshud.asm.instruction.AsmInstruction;
import enshud.asm.operand.ConstantOperand;
import enshud.asm.operand.IAsmOperand;
import enshud.asm.operand.RegisterOperand;
import enshud.interlanguage.iloperand.AbstractILOperand;
import enshud.interlanguage.iloperand.ILIndexedVariableOperand;
import enshud.interlanguage.iloperand.ILSimpleVariableOperand;
import enshud.interlanguage.ilstatement.AbstractILStatement;
import enshud.interlanguage.ilstatement.ILReadStatement;
import enshud.s4.asmgenerator.BiHashMap;
import enshud.s4.asmgenerator.StackPointerRevision;
import enshud.symboltable.SymbolTableStack;
import enshud.typeexpression.ArrayType;
import enshud.typeexpression.SimpleType;

public class ReadStatementAsmGenerator extends AbstractAsmGenerator {


	public ReadStatementAsmGenerator(
			AsmProgram asmProgram,
			BiHashMap<String, Integer> currentRegisterAllocation,
			StackPointerRevision stackPointerRevision) {

		super(asmProgram, currentRegisterAllocation, stackPointerRevision);
	}

	@Override
	public void generate(
			AbstractILStatement ilStatement,
			SymbolTableStack symbolTables,
			HashMap<AbstractILOperand, Integer> allocativePlan,
			BiHashMap<String, Integer> currentAllocation) {

		// 3番地コードのreadは単独で1つの基本ブロックなので，レジスタは割り付けずに自由に使う

		var specificStatement = (ILReadStatement)ilStatement;
		var operand = specificStatement.operand;

		if(operand instanceof ILSimpleVariableOperand) {

			// 添え字付きでない
			String variableName = ((ILSimpleVariableOperand)operand).variableName;
			var symbolTableEntry = symbolTables.findVariableFromAnyTable(variableName);
			if(symbolTableEntry.typeExpression instanceof SimpleType) {
				// 純変数

				if(symbolTableEntry.typeExpression.equals(SimpleType.INTEGER)) {

					asmProgram.add(new AsmInstruction(
							AsmInstruction.Operation.CALL,
							new IAsmOperand[] {
									new ConstantOperand("RDINT")
							}));

					addVariableInstruction(
							symbolTableEntry,
							AsmInstruction.Operation.ST,
							RegisterOperand.GR2);

				} else if(symbolTableEntry.typeExpression.equals(SimpleType.CHAR)) {

					asmProgram.add(new AsmInstruction(
							AsmInstruction.Operation.CALL,
							new IAsmOperand[] {
									new ConstantOperand("RDCH")
							}));

					addVariableInstruction(
							symbolTableEntry,
							AsmInstruction.Operation.ST,
							RegisterOperand.GR2);
				}

			} else {
				// 配列
				var arrayTypeExpression = (ArrayType)symbolTableEntry.typeExpression;
				if(!arrayTypeExpression.contentType.equals(SimpleType.CHAR))
					throw new RuntimeException();

				// GR1に文字数をロード
				asmProgram.add(new AsmInstruction(
						AsmInstruction.Operation.LAD,
						new IAsmOperand[] {
								RegisterOperand.GR1,
								new ConstantOperand(symbolTableEntry.size)
								}));

				addVariableInstruction(
						symbolTableEntry,
						AsmInstruction.Operation.LAD,
						RegisterOperand.GR2);

				asmProgram.add(new AsmInstruction(
						AsmInstruction.Operation.CALL,
						new IAsmOperand[] {
								new ConstantOperand("RDSTR")
						}));

			}

		} else {
			// 添え字付き
			var indexedOperand = (ILIndexedVariableOperand)operand;
			var arraySymbolTableEntry = symbolTables.findVariableFromAnyTable(indexedOperand.variableName);
			var arrayElementType = ((ArrayType)arraySymbolTableEntry.typeExpression).contentType;

			if(arrayElementType.equals(SimpleType.INTEGER)) {

				asmProgram.add(new AsmInstruction(
						AsmInstruction.Operation.CALL,
						new IAsmOperand[] {
								new ConstantOperand("RDINT")
						}));

			} else if(arrayElementType.equals(SimpleType.CHAR)) {

				asmProgram.add(new AsmInstruction(
						AsmInstruction.Operation.CALL,
						new IAsmOperand[] {
								new ConstantOperand("RDCHAR")
						}));
			} else
				throw new RuntimeException();

			// GR1に添え字の値を代入
			var indexSymbolTableEntry = symbolTables.findVariableFromAnyTable(indexedOperand.index.variableName);

			addVariableInstruction(
					indexSymbolTableEntry,
					AsmInstruction.Operation.LD,
					RegisterOperand.GR1);

			// GR2の値を配列の要素にストア
			addArrayInstruction(
					arraySymbolTableEntry,
					AsmInstruction.Operation.ST,
					RegisterOperand.GR2,
					RegisterOperand.GR1);

		}


	}

}
