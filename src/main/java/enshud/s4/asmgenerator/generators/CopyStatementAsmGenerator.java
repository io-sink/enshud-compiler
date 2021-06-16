package enshud.s4.asmgenerator.generators;

import java.util.HashMap;

import enshud.asm.AsmProgram;
import enshud.asm.instruction.AsmInstruction;
import enshud.asm.operand.ConstantOperand;
import enshud.asm.operand.IAsmOperand;
import enshud.asm.operand.RegisterOperand;
import enshud.interlanguage.iloperand.AbstractILOperand;
import enshud.interlanguage.iloperand.ILConstantOperand;
import enshud.interlanguage.iloperand.ILIndexedVariableOperand;
import enshud.interlanguage.iloperand.ILSimpleVariableOperand;
import enshud.interlanguage.ilstatement.AbstractILStatement;
import enshud.interlanguage.ilstatement.ILCopyStatement;
import enshud.s4.asmgenerator.BiHashMap;
import enshud.s4.asmgenerator.StackPointerRevision;
import enshud.s4.compiler.Compiler;
import enshud.symboltable.SymbolTable;
import enshud.symboltable.SymbolTableStack;

public class CopyStatementAsmGenerator extends AbstractAsmGenerator {


	public CopyStatementAsmGenerator(
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

		var specificStatement = (ILCopyStatement)ilStatement;

		int leftHandSideRegister = -1;
		if(specificStatement.leftHandSide instanceof ILSimpleVariableOperand &&
				currentAllocation.get(((ILSimpleVariableOperand)specificStatement.leftHandSide).variableName) != null)
			leftHandSideRegister = currentAllocation.get(((ILSimpleVariableOperand)specificStatement.leftHandSide).variableName);
		else if(allocativePlan.get(specificStatement.leftHandSide) != null)
			leftHandSideRegister = allocativePlan.get(specificStatement.leftHandSide);


		int rightHandSideRegister = -1;
		if(specificStatement.rightHandSide instanceof ILSimpleVariableOperand &&
				currentAllocation.get(((ILSimpleVariableOperand)specificStatement.rightHandSide).variableName) != null)
			rightHandSideRegister = currentAllocation.get(((ILSimpleVariableOperand)specificStatement.rightHandSide).variableName);
		else if(allocativePlan.get(specificStatement.rightHandSide) != null)
			rightHandSideRegister = allocativePlan.get(specificStatement.rightHandSide);


		if(specificStatement.leftHandSide instanceof ILIndexedVariableOperand) {
			// 左辺が配列の要素
			var leftHandSide = (ILIndexedVariableOperand)specificStatement.leftHandSide;
			var rightHandSide = (ILSimpleVariableOperand)specificStatement.rightHandSide;

			// 左辺に使うレジスタに割り付けられている変数を退避し，割り付けを削除
			allocateRegister(
					null,
					leftHandSideRegister,
					symbolTables,
					false
					);
			currentAllocation.removeInv(leftHandSideRegister);

			// 右辺をレジスタに代入
			if(allocativePlan.containsKey(rightHandSide) || currentAllocation.containsKey(rightHandSide.variableName)) {
				// 右辺を割り付けるとき
				rightHandSideRegister = allocativePlan.containsKey(rightHandSide) ?
						allocativePlan.get(rightHandSide) : currentAllocation.get(rightHandSide.variableName);

				// 右辺の変数を割り付け
				allocateRegister(
						rightHandSide.variableName,
						rightHandSideRegister,
						symbolTables,
						true
						);

			} else {
				// 右辺を割り付けなかった時はGR0を使う
				rightHandSideRegister = 8;	// GR0

				var symbolTableEntry = symbolTables.findVariableFromAnyTable(rightHandSide.variableName);
				addVariableInstruction(
						symbolTableEntry,
						AsmInstruction.Operation.LD,
						RegisterOperand.registers[rightHandSideRegister]);
			}

			// TODO 添え字はレジスタに割り付けないのか？
			// 左辺の添え字の値を左辺のレジスタに代入
			if(currentAllocation.containsKey(leftHandSide.index.variableName)) {
				asmProgram.add(new AsmInstruction(AsmInstruction.Operation.LD,
						new IAsmOperand[] {
								RegisterOperand.registers[leftHandSideRegister],
								RegisterOperand.registers[currentAllocation.get(leftHandSide.index.variableName)]
						}));

			} else {
				var symbolTableEntry = symbolTables.findVariableFromAnyTable(leftHandSide.index.variableName);
				addVariableInstruction(
						symbolTableEntry,
						AsmInstruction.Operation.LD,
						RegisterOperand.registers[leftHandSideRegister]);
			}

			// 配列の要素へレジスタの値をストア
			var arraySymbolTableEntry = symbolTables.findVariableFromAnyTable(leftHandSide.variableName);
			addArrayInstruction(
					arraySymbolTableEntry,
					AsmInstruction.Operation.ST,
					RegisterOperand.registers[rightHandSideRegister],
					RegisterOperand.registers[leftHandSideRegister]);

		} else if(specificStatement.rightHandSide instanceof ILIndexedVariableOperand) {
			// 右辺が配列の要素
			var leftHandSide = (ILSimpleVariableOperand)specificStatement.leftHandSide;
			var rightHandSide = (ILIndexedVariableOperand)specificStatement.rightHandSide;

			// 左辺のレジスタにある値を退避
			allocateRegister(
					leftHandSide.variableName,
					leftHandSideRegister,
					symbolTables,
					false
					);


			// 右辺の添え字の値をレジスタに代入
			if(currentAllocation.containsKey(rightHandSide.index.variableName)) {
				asmProgram.add(new AsmInstruction(AsmInstruction.Operation.LD,
						new IAsmOperand[] {
								RegisterOperand.registers[leftHandSideRegister],
								RegisterOperand.registers[currentAllocation.get(rightHandSide.index.variableName)]
						}));

			} else {
				var symbolTableEntry = symbolTables.findVariableFromAnyTable(rightHandSide.index.variableName);
				addVariableInstruction(
						symbolTableEntry,
						AsmInstruction.Operation.LD,
						RegisterOperand.registers[leftHandSideRegister]);
			}

			// 配列の要素から左辺のレジスタへロード
			var arraySymbolTableEntry = symbolTables.findVariableFromAnyTable(rightHandSide.variableName);
			addArrayInstruction(
					arraySymbolTableEntry,
					AsmInstruction.Operation.LD,
					RegisterOperand.registers[leftHandSideRegister],
					RegisterOperand.registers[leftHandSideRegister]);

			if(!Compiler.allocatable) {
				// 左辺にレジスタに割り付けていない場合はメモリにストア
				var symbolTableEntry = symbolTables.findVariableFromAnyTable(leftHandSide.variableName);
				addVariableInstruction(
						symbolTableEntry,
						AsmInstruction.Operation.ST,
						RegisterOperand.registers[leftHandSideRegister]);
			}

		} else if(specificStatement.rightHandSide instanceof ILConstantOperand) {
			// 右辺が定数
			var leftHandSide = (ILSimpleVariableOperand)specificStatement.leftHandSide;
			var rightHandSide = (ILConstantOperand)specificStatement.rightHandSide;

			if(leftHandSideRegister != -1) {
				// 左辺を割り付けたとき
				// 左辺のレジスタにある値を退避
				allocateRegister(
						leftHandSide.variableName,
						leftHandSideRegister,
						symbolTables,
						false
						);
			} else {

				leftHandSideRegister = 8;	// GR0
			}

			// 定数の値を得る
			int rightHandSideValue = rightHandSide.getIntValue();

			asmProgram.add(new AsmInstruction(
					AsmInstruction.Operation.LAD,
					new IAsmOperand[] {
							RegisterOperand.registers[leftHandSideRegister],
							new ConstantOperand(rightHandSideValue)
					}));

			if(RegisterOperand.registers[leftHandSideRegister] == RegisterOperand.GR0) {
				// 左辺にレジスタに割り付けていない場合はメモリにストア
				var symbolTableEntry = symbolTables.findVariableFromAnyTable(leftHandSide.variableName);
				addVariableInstruction(
						symbolTableEntry,
						AsmInstruction.Operation.ST,
						RegisterOperand.registers[leftHandSideRegister]);
			}

		} else {
			// 左辺も右辺も純変数
			var leftHandSide = (ILSimpleVariableOperand)specificStatement.leftHandSide;
			var rightHandSide = (ILSimpleVariableOperand)specificStatement.rightHandSide;

			// 左右が同じ変数なら何もしない
			if(leftHandSide.equals(rightHandSide))
				return;


			if(rightHandSideRegister != -1) {

				// 右辺を割り付け
				allocateRegister(
						rightHandSide.variableName,
						rightHandSideRegister,
						symbolTables,
						true
						);

				if(leftHandSideRegister != -1) {
					// 右辺も左辺も割り付けた

					// 左辺を割り付け
					allocateRegister(
							leftHandSide.variableName,
							leftHandSideRegister,
							symbolTables,
							false
							);

					asmProgram.add(new AsmInstruction(
							AsmInstruction.Operation.LD,
							new IAsmOperand[] {
									RegisterOperand.registers[leftHandSideRegister],
									RegisterOperand.registers[rightHandSideRegister]
							}));

				} else {
					// 右辺は割り付けたが，左辺は割り付けなかった

					var symbolTableEntry = symbolTables.findVariableFromAnyTable(leftHandSide.variableName);
					addVariableInstruction(
							symbolTableEntry,
							AsmInstruction.Operation.ST,
							RegisterOperand.registers[rightHandSideRegister]);

				}
			} else
				if(leftHandSideRegister != -1) {
					// 左辺は割り付けたが，右辺は割り付けなかった

					allocateRegister(
							leftHandSide.variableName,
							leftHandSideRegister,
							symbolTables,
							false
							);

					var symbolTableEntry = symbolTables.findVariableFromAnyTable(rightHandSide.variableName);
					addVariableInstruction(
							symbolTableEntry,
							AsmInstruction.Operation.LD,
							RegisterOperand.registers[leftHandSideRegister]);

				} else {
					// 右辺も左辺も割り付けなかった

					var symbolTableEntry = symbolTables.findVariableFromAnyTable(rightHandSide.variableName);
					addVariableInstruction(
							symbolTableEntry,
							AsmInstruction.Operation.LD,
							RegisterOperand.GR0);	// GR0を使う

					symbolTableEntry = symbolTables.findVariableFromAnyTable(leftHandSide.variableName);
					addVariableInstruction(
							symbolTableEntry,
							AsmInstruction.Operation.ST,
							RegisterOperand.GR0);

				}



		}

		// 左辺が一時変数である場合，記号表の型を書き換える
		if(specificStatement.leftHandSide instanceof ILSimpleVariableOperand) {
			var tempVariableEntry = symbolTables.findVariableFromAnyTable(((ILSimpleVariableOperand)specificStatement.leftHandSide).variableName);

			if(tempVariableEntry.entryType == SymbolTable.EntryType.TEMPVARIABLE)
				tempVariableEntry.typeExpression = specificStatement.typeExpression;
		}


	}
}
