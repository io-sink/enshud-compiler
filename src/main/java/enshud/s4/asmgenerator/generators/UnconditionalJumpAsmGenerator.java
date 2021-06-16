package enshud.s4.asmgenerator.generators;

import java.util.HashMap;

import enshud.asm.AsmProgram;
import enshud.asm.instruction.AsmInstruction;
import enshud.asm.operand.ConstantOperand;
import enshud.asm.operand.IAsmOperand;
import enshud.interlanguage.iloperand.AbstractILOperand;
import enshud.interlanguage.ilstatement.AbstractILStatement;
import enshud.interlanguage.ilstatement.ILUnconditionalJump;
import enshud.s4.asmgenerator.BiHashMap;
import enshud.s4.asmgenerator.StackPointerRevision;
import enshud.symboltable.SymbolTableStack;

public class UnconditionalJumpAsmGenerator extends AbstractAsmGenerator {

	public UnconditionalJumpAsmGenerator(
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

		var specificStatement = (ILUnconditionalJump)ilStatement;

		asmProgram.add(new AsmInstruction(
				AsmInstruction.Operation.JUMP,
				new IAsmOperand[] {
						new ConstantOperand(specificStatement.labelName)
				}));
	}

}
