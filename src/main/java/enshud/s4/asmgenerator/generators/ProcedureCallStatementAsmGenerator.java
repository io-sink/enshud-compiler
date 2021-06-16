package enshud.s4.asmgenerator.generators;

import java.util.HashMap;

import enshud.asm.AsmProgram;
import enshud.asm.instruction.AsmInstruction;
import enshud.asm.operand.ConstantOperand;
import enshud.asm.operand.IAsmOperand;
import enshud.asm.operand.RegisterOperand;
import enshud.interlanguage.iloperand.AbstractILOperand;
import enshud.interlanguage.ilstatement.AbstractILStatement;
import enshud.interlanguage.ilstatement.ILProcedureCallStatement;
import enshud.s4.asmgenerator.BiHashMap;
import enshud.s4.asmgenerator.ProcedureVariableAllocator;
import enshud.s4.asmgenerator.StackPointerRevision;
import enshud.symboltable.SymbolTableStack;

public class ProcedureCallStatementAsmGenerator extends AbstractAsmGenerator {

	public ProcedureCallStatementAsmGenerator(
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

		var specificStatement = (ILProcedureCallStatement)ilStatement;
		String procedureName = specificStatement.procedureName;

		asmProgram.add(new AsmInstruction(AsmInstruction.Operation.CALL,
				new IAsmOperand[] {
						new ConstantOperand(".LEntry" + procedureName)
				}));


		// 引数の領域を取り除く
		var variableAllocator = new ProcedureVariableAllocator(symbolTables, procedureName);
		int tempParameterSizeSum = variableAllocator.tempParameterSize();

		asmProgram.add(new AsmInstruction(
				AsmInstruction.Operation.ADDA,
				new IAsmOperand[] {
						RegisterOperand.GR8,
						new ConstantOperand("=" + tempParameterSizeSum)
						}));
		stackPointerRevision.stackPointerAdded -= tempParameterSizeSum;

	}

}
