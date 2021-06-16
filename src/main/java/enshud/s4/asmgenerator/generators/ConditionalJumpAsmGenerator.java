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
import enshud.interlanguage.ilstatement.ILConditionalJump;
import enshud.s4.asmgenerator.BiHashMap;
import enshud.s4.asmgenerator.StackPointerRevision;
import enshud.symboltable.SymbolTableStack;

public class ConditionalJumpAsmGenerator extends AbstractAsmGenerator {

	public ConditionalJumpAsmGenerator(
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

		var specificStatement = (ILConditionalJump)ilStatement;
		var condition = specificStatement.condition;

		int conditionRegister = -1;
		if(condition instanceof ILSimpleVariableOperand &&
				currentAllocation.get(((ILSimpleVariableOperand)condition).variableName) != null)
			conditionRegister = currentAllocation.get(((ILSimpleVariableOperand)condition).variableName);
		else if(allocativePlan.get(condition) != null)
			conditionRegister = allocativePlan.get(condition);


		if(conditionRegister != -1 && condition instanceof ILSimpleVariableOperand) {
			// 条件を割り付けたとき
			allocateRegister(
					((ILSimpleVariableOperand)condition).variableName,
					conditionRegister,
					symbolTables,
					true
					);

		} else if(condition instanceof ILSimpleVariableOperand) {
			// 条件が純変数で，割り付けなかったとき
			conditionRegister = 8;	// GR0

			var symbolTableEntry = symbolTables.findVariableFromAnyTable(((ILSimpleVariableOperand)condition).variableName);
			addVariableInstruction(
					symbolTableEntry,
					AsmInstruction.Operation.LD,
					RegisterOperand.registers[conditionRegister]);

		} else {
			// 条件が定数
			conditionRegister = 8;	// GR0

			int conditionValue = ((ILConstantOperand)condition).getIntValue();

			asmProgram.add(new AsmInstruction(
					AsmInstruction.Operation.LAD,
					new IAsmOperand[] {
							RegisterOperand.registers[conditionRegister],
							new ConstantOperand(conditionValue)
					}));
		}

		asmProgram.add(new AsmInstruction(
				AsmInstruction.Operation.CPA,
				new IAsmOperand[] {
						RegisterOperand.registers[conditionRegister],
						new ConstantOperand("=#0000")	// false
				}));

		asmProgram.add(new AsmInstruction(
				AsmInstruction.Operation.JNZ,
				new IAsmOperand[] {
						new ConstantOperand(specificStatement.labelName)
				}));
	}

}
