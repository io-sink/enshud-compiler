package enshud.s4.asmgenerator.generators;

import java.util.HashMap;
import java.util.LinkedList;

import enshud.asm.AsmProgram;
import enshud.asm.instruction.AsmInstruction;
import enshud.asm.operand.ConstantOperand;
import enshud.asm.operand.IAsmOperand;
import enshud.asm.operand.RegisterOperand;
import enshud.interlanguage.iloperand.AbstractILOperand;
import enshud.interlanguage.ilstatement.AbstractILStatement;
import enshud.s4.asmgenerator.BiHashMap;
import enshud.s4.asmgenerator.StackPointerRevision;
import enshud.s4.compiler.Compiler;
import enshud.symboltable.SymbolTable;
import enshud.symboltable.SymbolTableStack;
import enshud.typeexpression.ArrayType;

public abstract class AbstractAsmGenerator {


	public AsmProgram asmProgram;
	protected BiHashMap<String, Integer> currentRegisterAllocation;
	protected StackPointerRevision stackPointerRevision;

	protected AbstractAsmGenerator(
			AsmProgram asmProgram,
			BiHashMap<String, Integer> currentRegisterAllocation,
			StackPointerRevision stackPointerRevision) {

		this.asmProgram = asmProgram;
		this.currentRegisterAllocation = currentRegisterAllocation;
		this.stackPointerRevision = stackPointerRevision;
	}

	public void generate(
			AbstractILStatement ilStatement,
			SymbolTableStack symbolTables,
			HashMap<AbstractILOperand, Integer> allocativePlan,
			BiHashMap<String, Integer> currentAllocation) {

		throw new RuntimeException();
	}

	protected void allocateRegister(
			String variableName,
			int registerNumber,
			SymbolTableStack symbolTables,
			boolean copyValue) {

		String oldVariableName = currentRegisterAllocation.getInv(registerNumber);
		if(oldVariableName != null) {
			if(oldVariableName.equals(variableName))
				return;

			// 今レジスタに割り付けられている変数を退避する
			var symbolTableEntry = symbolTables.findVariableFromAnyTable(oldVariableName);

			// ストア命令
			addVariableInstruction(
					symbolTableEntry,
					AsmInstruction.Operation.ST,
					RegisterOperand.registers[registerNumber]);

		}

		if(variableName != null) {

			if(copyValue) {
				var symbolTableEntry = symbolTables.findVariableFromAnyTable(variableName);

				// ロード命令
				addVariableInstruction(
						symbolTableEntry,
						AsmInstruction.Operation.LD,
						RegisterOperand.registers[registerNumber]);

			}

			if(Compiler.allocatable)
				currentRegisterAllocation.put(variableName, registerNumber);
		} else
			currentRegisterAllocation.removeInv(registerNumber);
	}


	// 第一オペランドがレジスタ，第二オペランドが変数の命令を追加する
	public AsmInstruction addVariableInstruction(
			SymbolTable.SymbolTableEntry symbolTableEntry,
			AsmInstruction.Operation operation,
			RegisterOperand registerOperand
			) {

		String allocatedVariable =
				currentRegisterAllocation.getInv(RegisterOperand.registersInv.get(registerOperand));
		if(allocatedVariable != null && allocatedVariable.equals(symbolTableEntry.name) &&
				operation == AsmInstruction.Operation.LD) {
			// 既に割り付けられている変数のロードはしない
			return null;
		}

		var newInstruction = AbstractAsmGenerator.getVariableInstruction(
				symbolTableEntry,
				operation,
				registerOperand,
				stackPointerRevision);

		asmProgram.add(newInstruction);
		return newInstruction;
	}

	public static AsmInstruction getVariableInstruction(
			SymbolTable.SymbolTableEntry symbolTableEntry,
			AsmInstruction.Operation operation,
			RegisterOperand registerOperand,

			StackPointerRevision stackPointerRevision
			) {

		AsmInstruction newInstruction;

		if(symbolTableEntry.entryType == SymbolTable.EntryType.CONSTSTR ||
				symbolTableEntry.entryType == SymbolTable.EntryType.PROCEDURE ||
				symbolTableEntry.entryType == SymbolTable.EntryType.PROGRAM ||
				symbolTableEntry.typeExpression instanceof ArrayType)
			throw new RuntimeException();


		if(symbolTableEntry.isOnStack)
			newInstruction =
					new AsmInstruction(
							operation,
							new IAsmOperand[] {
									registerOperand,
									new ConstantOperand(symbolTableEntry.location + stackPointerRevision.stackPointerAdded),
									RegisterOperand.GR8
							});
		else
			newInstruction =
					new AsmInstruction(
							operation,
							new IAsmOperand[] {
									registerOperand,
									new ConstantOperand(".LGlobal" + symbolTableEntry.name)
							});

		return newInstruction;
	}


	// 第一オペランドがレジスタ，第二オペランドが配列要素の命令を追加する
	// 添え字のレジスタの処理後の値は不定
	public LinkedList<AsmInstruction> addArrayInstruction(
			SymbolTable.SymbolTableEntry arraySymbolTableEntry,
			AsmInstruction.Operation operation,
			RegisterOperand registerOperand,
			RegisterOperand arrayIndexRegister) {


		if(arraySymbolTableEntry.entryType == SymbolTable.EntryType.CONSTSTR ||
				arraySymbolTableEntry.entryType == SymbolTable.EntryType.PROCEDURE ||
				arraySymbolTableEntry.entryType == SymbolTable.EntryType.PROGRAM ||
				!(arraySymbolTableEntry.typeExpression instanceof ArrayType))
			throw new RuntimeException();

		var newInstructions = new LinkedList<AsmInstruction>();
		int arrayStartIndex = ((ArrayType)arraySymbolTableEntry.typeExpression).startIndex;

		if(arraySymbolTableEntry.isOnStack) {
			newInstructions.add(new AsmInstruction(AsmInstruction.Operation.ADDA,
					new IAsmOperand[] {
							arrayIndexRegister,
							RegisterOperand.GR8
					}));

			newInstructions.add(new AsmInstruction(
					operation,
					new IAsmOperand[] {
							registerOperand,
							new ConstantOperand(
									arraySymbolTableEntry.location - arrayStartIndex + stackPointerRevision.stackPointerAdded),
							arrayIndexRegister
					}));

		} else {
			if(arrayStartIndex != 0) {
				newInstructions.add(new AsmInstruction(
						AsmInstruction.Operation.SUBA,
						new IAsmOperand[] {
								arrayIndexRegister,
								new ConstantOperand("=" + arrayStartIndex),
						}));
			}

			newInstructions.add(new AsmInstruction(
					operation,
					new IAsmOperand[] {
							registerOperand,
							new ConstantOperand(".LGlobal" + arraySymbolTableEntry.name),
							arrayIndexRegister
					}));
		}

		asmProgram.addAll(newInstructions);
		return newInstructions;
	}


}
