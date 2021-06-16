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
import enshud.interlanguage.ilstatement.ILArgPushStatement;
import enshud.s4.asmgenerator.BiHashMap;
import enshud.s4.asmgenerator.StackPointerRevision;
import enshud.symboltable.SymbolTableStack;

public class ArgPushStatementAsmGenerator extends AbstractAsmGenerator {

	public ArgPushStatementAsmGenerator(
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

		var specificStatement = (ILArgPushStatement)ilStatement;
		var operand = specificStatement.operand;

		// 割り付けたレジスタ
		System.out.println("***" + currentAllocation);
		int operandRegister = allocativePlan.containsKey(operand) ?
				allocativePlan.get(operand) : currentAllocation.get(((ILSimpleVariableOperand)operand).variableName);


		if(operand instanceof ILSimpleVariableOperand) {
			// オペランドが純変数(割り付けは必須)

			// 割り付けを行う
			allocateRegister(
					((ILSimpleVariableOperand)operand).variableName,
					operandRegister,
					symbolTables,
					true
					);

		} else {
			// オペランドが定数
			// レジスタに割り付けられている値をメモリへ退避
			allocateRegister(
					null,
					operandRegister,
					symbolTables,
					true
					);

			int operandValue = ((ILConstantOperand)operand).getIntValue();
			asmProgram.add(new AsmInstruction(
					AsmInstruction.Operation.LAD,
					new IAsmOperand[] {
							RegisterOperand.registers[operandRegister],
							new ConstantOperand(operandValue)
					}));
		}


		asmProgram.add(new AsmInstruction(AsmInstruction.Operation.PUSH,
				new IAsmOperand[] {
						new ConstantOperand(0),
						RegisterOperand.registers[operandRegister]
				}));
		stackPointerRevision.stackPointerAdded++;


	}
}
