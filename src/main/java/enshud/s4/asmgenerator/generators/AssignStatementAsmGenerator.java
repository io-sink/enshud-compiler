package enshud.s4.asmgenerator.generators;

import java.util.HashMap;

import enshud.asm.AsmProgram;
import enshud.asm.instruction.AsmInstruction;
import enshud.asm.instruction.AsmLabel;
import enshud.asm.operand.ConstantOperand;
import enshud.asm.operand.IAsmOperand;
import enshud.asm.operand.RegisterOperand;
import enshud.interlanguage.iloperand.AbstractILOperand;
import enshud.interlanguage.iloperand.ILConstantOperand;
import enshud.interlanguage.iloperand.ILSimpleVariableOperand;
import enshud.interlanguage.ilstatement.AbstractILStatement;
import enshud.interlanguage.ilstatement.ILAssign1Statement;
import enshud.interlanguage.ilstatement.ILAssign2Statement;
import enshud.s4.asmgenerator.BiHashMap;
import enshud.s4.asmgenerator.StackPointerRevision;
import enshud.s4.ilgenerator.LabelGenerator;
import enshud.symboltable.SymbolTable;
import enshud.symboltable.SymbolTableStack;
import enshud.typeexpression.AbstractType;
import enshud.typeexpression.SimpleType;

public class AssignStatementAsmGenerator extends AbstractAsmGenerator {

	private LabelGenerator labelGenerator;

	public AssignStatementAsmGenerator(
			AsmProgram asmProgram,
			BiHashMap<String, Integer> currentRegisterAllocation,
			LabelGenerator labelGenerator,
			StackPointerRevision stackPointerRevision) {

		super(asmProgram, currentRegisterAllocation, stackPointerRevision);
		this.labelGenerator = labelGenerator;
	}

	@Override
	public void generate(
			AbstractILStatement ilStatement,
			SymbolTableStack symbolTables,
			HashMap<AbstractILOperand, Integer> allocativePlan,
			BiHashMap<String, Integer> currentAllocation) {

		ILSimpleVariableOperand leftHandSide = null;
		AbstractILOperand rightOperand = null;
		AbstractILOperand leftOperand = null;
		AbstractType leftHandSideType = null;
		AbstractType rightOperandType = null;

		if(ilStatement instanceof ILAssign2Statement) {
			var specificStatement = (ILAssign2Statement)ilStatement;

			leftHandSide = (ILSimpleVariableOperand)specificStatement.leftHandSide;
			leftOperand = specificStatement.leftOperand;
			rightOperand = specificStatement.rightOperand;
			leftHandSideType = specificStatement.typeExpression;

		} else if(ilStatement instanceof ILAssign1Statement) {
			// ????????????????????????????????????
			var specificStatement = (ILAssign1Statement)ilStatement;

			leftHandSide = (ILSimpleVariableOperand)specificStatement.leftHandSide;
			rightOperand = specificStatement.operand;
			leftHandSideType = specificStatement.typeExpression;

			if(specificStatement.operationType == ILAssign1Statement.OperationType.MINUS)
				leftOperand = new ILConstantOperand("0", SimpleType.INTEGER);
			else /* NOT */
				leftOperand = new ILConstantOperand("-1", SimpleType.INTEGER);
		}

		if(rightOperand instanceof ILSimpleVariableOperand) {
			var rightOperandEntry = ilStatement.symbolTableStack.findVariableFromAnyTable(((ILSimpleVariableOperand)rightOperand).variableName);
			rightOperandType = rightOperandEntry.typeExpression;
		} else
			rightOperandType = ((ILConstantOperand)rightOperand).typeExpression;



		// ???????????????????????????
		int leftHandSideRegister = -1;
		if(currentAllocation.get(leftHandSide.variableName) != null)
			leftHandSideRegister = currentAllocation.get(leftHandSide.variableName);
		else if(allocativePlan.get(leftHandSide) != null)
			leftHandSideRegister = allocativePlan.get(leftHandSide);

		int leftOperandRegister = -1;
		if(leftOperand instanceof ILSimpleVariableOperand &&
				currentAllocation.get(((ILSimpleVariableOperand)leftOperand).variableName) != null)
			leftOperandRegister = currentAllocation.get(((ILSimpleVariableOperand)leftOperand).variableName);
		else if(allocativePlan.get(leftOperand) != null)
			leftOperandRegister = allocativePlan.get(leftOperand);


		int rightOperandRegister = -1;
		if(rightOperand instanceof ILSimpleVariableOperand &&
				currentAllocation.get(((ILSimpleVariableOperand)rightOperand).variableName) != null)
			rightOperandRegister = currentAllocation.get(((ILSimpleVariableOperand)rightOperand).variableName);
		else if(allocativePlan.get(rightOperand) != null)
			rightOperandRegister = allocativePlan.get(rightOperand);


		int tmpRegister = leftHandSideRegister == -1 || leftHandSideRegister == rightOperandRegister ?
				8 /* GR0 */ : leftHandSideRegister;


		// ?????????????????????????????????????????????
		if(RegisterOperand.registers[tmpRegister] != RegisterOperand.GR0) {

			String oldVariableName = currentRegisterAllocation.getInv(tmpRegister);
			if(oldVariableName != null && !leftHandSide.variableName.equals(oldVariableName))
				allocateRegister(
						null,
						leftHandSideRegister,
						symbolTables,
						false
						);
		}


		// tmpRegister ??? ??????????????????
		if(leftOperandRegister != -1) {
			// ???????????????????????????????????????????????????
			allocateRegister(
					((ILSimpleVariableOperand)leftOperand).variableName,
					leftOperandRegister,
					symbolTables,
					true
					);

			asmProgram.add(new AsmInstruction(
					AsmInstruction.Operation.LD,
					new IAsmOperand[] {
							RegisterOperand.registers[tmpRegister],
							RegisterOperand.registers[leftOperandRegister]
					}));

		} else if(leftOperand instanceof ILSimpleVariableOperand) {
			// ???????????????????????????????????????????????????????????????????????????
			var symbolTableEntry = symbolTables.findVariableFromAnyTable(((ILSimpleVariableOperand)leftOperand).variableName);
			addVariableInstruction(
					symbolTableEntry,
					AsmInstruction.Operation.LD,
					RegisterOperand.registers[tmpRegister]);

		} else {
			// ???????????????????????????

			// ?????????????????????
			int leftOperandValue = ((ILConstantOperand)leftOperand).getIntValue();

			asmProgram.add(new AsmInstruction(
					AsmInstruction.Operation.LAD,
					new IAsmOperand[] {
							RegisterOperand.registers[tmpRegister],
							new ConstantOperand(leftOperandValue)
					}));
		}

		if(ilStatement instanceof ILAssign2Statement && (
				((ILAssign2Statement)ilStatement).operationType == ILAssign2Statement.OperationType.MUL ||
				((ILAssign2Statement)ilStatement).operationType == ILAssign2Statement.OperationType.DIV ||
				((ILAssign2Statement)ilStatement).operationType == ILAssign2Statement.OperationType.MOD
				)) {
			// ???????????????

			var operationType = ((ILAssign2Statement)ilStatement).operationType;

			if(rightOperand instanceof ILSimpleVariableOperand &&
					rightOperandRegister != -1) {
				// ?????????????????????????????????????????????????????????

				allocateRegister(
						((ILSimpleVariableOperand)rightOperand).variableName,
						rightOperandRegister,
						symbolTables,
						true
						);

				// GR1???????????????
				if(RegisterOperand.registers[tmpRegister] != RegisterOperand.GR1) {
					asmProgram.add(new AsmInstruction(
							AsmInstruction.Operation.PUSH,
							new IAsmOperand[] {
									new ConstantOperand(0),
									RegisterOperand.GR1
							}));
					stackPointerRevision.stackPointerAdded++;
				}


				// GR2???????????????
				if(RegisterOperand.registers[tmpRegister] != RegisterOperand.GR2) {
					asmProgram.add(new AsmInstruction(
							AsmInstruction.Operation.PUSH,
							new IAsmOperand[] {
									new ConstantOperand(0),
									RegisterOperand.GR2
							}));
					stackPointerRevision.stackPointerAdded++;
				}


				// GR8???push, pop??????????????????????????????
				if(RegisterOperand.registers[tmpRegister] != RegisterOperand.GR0) {

					// ??????????????????????????????????????????
					asmProgram.add(new AsmInstruction(
							AsmInstruction.Operation.PUSH,
							new IAsmOperand[] {
									new ConstantOperand(0),
									RegisterOperand.registers[tmpRegister]
							}));
					stackPointerRevision.stackPointerAdded++;

					// ?????????????????????GR2?????????
					asmProgram.add(new AsmInstruction(
							AsmInstruction.Operation.LD,
							new IAsmOperand[] {
									RegisterOperand.GR2,
									RegisterOperand.registers[rightOperandRegister]
							}));

				} else {

					// ?????????????????????GR2?????????
					asmProgram.add(new AsmInstruction(
							AsmInstruction.Operation.LD,
							new IAsmOperand[] {
									RegisterOperand.GR2,
									RegisterOperand.registers[rightOperandRegister]
							}));

					// ?????????????????????GR1?????????
					asmProgram.add(new AsmInstruction(
							AsmInstruction.Operation.LD,
							new IAsmOperand[] {
									RegisterOperand.GR1,
									RegisterOperand.registers[tmpRegister]
							}));

				}


			} else if(rightOperand instanceof ILSimpleVariableOperand) {
				// ???????????????????????????????????????????????????

				// GR1???????????????
				if(RegisterOperand.registers[tmpRegister] != RegisterOperand.GR1) {
					asmProgram.add(new AsmInstruction(
							AsmInstruction.Operation.PUSH,
							new IAsmOperand[] {
									new ConstantOperand(0),
									RegisterOperand.GR1
							}));
					stackPointerRevision.stackPointerAdded++;
				}


				// GR2???????????????
				if(RegisterOperand.registers[tmpRegister] != RegisterOperand.GR2) {
					asmProgram.add(new AsmInstruction(
							AsmInstruction.Operation.PUSH,
							new IAsmOperand[] {
									new ConstantOperand(0),
									RegisterOperand.GR2
							}));
					stackPointerRevision.stackPointerAdded++;
				}

				// GR8???push, pop??????????????????????????????
				if(RegisterOperand.registers[tmpRegister] != RegisterOperand.GR0) {

					// ??????????????????????????????????????????
					asmProgram.add(new AsmInstruction(
							AsmInstruction.Operation.PUSH,
							new IAsmOperand[] {
									new ConstantOperand(0),
									RegisterOperand.registers[tmpRegister]
							}));
					stackPointerRevision.stackPointerAdded++;

					// ?????????????????????GR2?????????
					var symbolTableEntry = symbolTables.findVariableFromAnyTable(((ILSimpleVariableOperand)rightOperand).variableName);
					addVariableInstruction(
							symbolTableEntry,
							AsmInstruction.Operation.LD,
							RegisterOperand.GR2);

				} else {

					// ?????????????????????GR2?????????
					var symbolTableEntry = symbolTables.findVariableFromAnyTable(((ILSimpleVariableOperand)rightOperand).variableName);
					addVariableInstruction(
							symbolTableEntry,
							AsmInstruction.Operation.LD,
							RegisterOperand.GR2);

					// ?????????????????????GR1?????????
					asmProgram.add(new AsmInstruction(
							AsmInstruction.Operation.LD,
							new IAsmOperand[] {
									RegisterOperand.GR1,
									RegisterOperand.registers[tmpRegister]
							}));

				}

			} else {
				// ????????????????????????????????????

				// GR1???????????????
				if(RegisterOperand.registers[tmpRegister] != RegisterOperand.GR1) {
					asmProgram.add(new AsmInstruction(
							AsmInstruction.Operation.PUSH,
							new IAsmOperand[] {
									new ConstantOperand(0),
									RegisterOperand.GR1
							}));
					stackPointerRevision.stackPointerAdded++;
				}


				// GR2???????????????
				if(RegisterOperand.registers[tmpRegister] != RegisterOperand.GR2) {
					asmProgram.add(new AsmInstruction(
							AsmInstruction.Operation.PUSH,
							new IAsmOperand[] {
									new ConstantOperand(0),
									RegisterOperand.GR2
							}));
					stackPointerRevision.stackPointerAdded++;
				}


				// ?????????????????????GR1?????????
				asmProgram.add(new AsmInstruction(
						AsmInstruction.Operation.LD,
						new IAsmOperand[] {
								RegisterOperand.GR1,
								RegisterOperand.registers[tmpRegister]
						}));


				// ?????????????????????
				int rightOperandValue = ((ILConstantOperand)rightOperand).getIntValue();
				// ?????????????????????GR2?????????
				asmProgram.add(new AsmInstruction(
						AsmInstruction.Operation.LAD,
						new IAsmOperand[] {
								RegisterOperand.GR2,
								new ConstantOperand(rightOperandValue)
						}));

			}

			// ????????????????????????????????????????????????GR1???
			if(rightOperand instanceof ILSimpleVariableOperand &&
					RegisterOperand.registers[tmpRegister] != RegisterOperand.GR0) {

				// ?????????????????????GR1?????????
				asmProgram.add(new AsmInstruction(
						AsmInstruction.Operation.POP,
						new IAsmOperand[] {
								RegisterOperand.GR1
						}));
				stackPointerRevision.stackPointerAdded--;

			}


			// ?????????????????????????????????
			if(operationType == ILAssign2Statement.OperationType.MUL)
				asmProgram.add(new AsmInstruction(
						AsmInstruction.Operation.CALL,
						new IAsmOperand[] {
								new ConstantOperand("MULT")
						}));
			else
				asmProgram.add(new AsmInstruction(
						AsmInstruction.Operation.CALL,
						new IAsmOperand[] {
								new ConstantOperand("DIV")
						}));

			// ???????????????????????????????????????????????????????????????
			if(operationType == ILAssign2Statement.OperationType.MOD)
				asmProgram.add(new AsmInstruction(
						AsmInstruction.Operation.LD,
						new IAsmOperand[] {
								RegisterOperand.registers[tmpRegister],
								RegisterOperand.GR1
						}));
			else
				asmProgram.add(new AsmInstruction(
						AsmInstruction.Operation.LD,
						new IAsmOperand[] {
								RegisterOperand.registers[tmpRegister],
								RegisterOperand.GR2
						}));

			// ???????????????????????????????????????
			if(RegisterOperand.registers[tmpRegister] != RegisterOperand.GR2) {
				asmProgram.add(new AsmInstruction(
						AsmInstruction.Operation.POP,
						new IAsmOperand[] {
								RegisterOperand.GR2
						}));
				stackPointerRevision.stackPointerAdded--;
			}

			if(RegisterOperand.registers[tmpRegister] != RegisterOperand.GR1) {
				asmProgram.add(new AsmInstruction(
						AsmInstruction.Operation.POP,
						new IAsmOperand[] {
								RegisterOperand.GR1
						}));
				stackPointerRevision.stackPointerAdded--;
			}


		} else {
			// ????????????????????????

			AsmInstruction.Operation operation = null;
			if(ilStatement instanceof ILAssign2Statement) {
				var specificStatement = (ILAssign2Statement)ilStatement;
				operation = AsmInstruction.Operation.initialize(specificStatement.operationType);

			} else if(ilStatement instanceof ILAssign1Statement) {
				var specificStatement = (ILAssign1Statement)ilStatement;

				if(specificStatement.operationType == ILAssign1Statement.OperationType.MINUS)
					operation = AsmInstruction.Operation.SUBA;
				else /* NOT */
					operation = AsmInstruction.Operation.XOR;
			}

			// tmpRegister???????????????????????????????????????
			if(rightOperand instanceof ILSimpleVariableOperand &&
					rightOperandRegister != -1) {

				// ???????????????????????????????????????
				allocateRegister(
						((ILSimpleVariableOperand)rightOperand).variableName,
						rightOperandRegister,
						symbolTables,
						true
						);

				asmProgram.add(new AsmInstruction(
						operation,
						new IAsmOperand[] {
								RegisterOperand.registers[tmpRegister],
								RegisterOperand.registers[rightOperandRegister]
						}));

			} else if(rightOperand instanceof ILSimpleVariableOperand) {

				// ??????????????????????????????????????????????????????
				var symbolTableEntry = symbolTables.findVariableFromAnyTable(((ILSimpleVariableOperand)rightOperand).variableName);
				addVariableInstruction(
						symbolTableEntry,
						operation,
						RegisterOperand.registers[tmpRegister]);

			} else {
				// ????????????????????????????????????

				// ?????????????????????
				int rightOperandValue = ((ILConstantOperand)rightOperand).getIntValue();

				asmProgram.add(new AsmInstruction(
						operation,
						new IAsmOperand[] {
								RegisterOperand.registers[tmpRegister],
								new ConstantOperand("=" + rightOperandValue)
						}));

			}


			if(operation == AsmInstruction.Operation.CPA) {
				// ????????????????????????????????????
				String label1 = labelGenerator.get();
				String label2 = labelGenerator.get();

				var specificStatement = (ILAssign2Statement)ilStatement;

				// boolean???????????????????????????(false:0 < true:-1)
				boolean jumpTrue = true;
				AsmInstruction.Operation jumpOperation = null;
				if(specificStatement.operationType == ILAssign2Statement.OperationType.EQ)
					jumpOperation = AsmInstruction.Operation.JZE;

				else if(specificStatement.operationType == ILAssign2Statement.OperationType.NEQ)
					jumpOperation = AsmInstruction.Operation.JNZ;

				else if(specificStatement.operationType == ILAssign2Statement.OperationType.LS)
					if(rightOperandType.equals(SimpleType.BOOLEAN))
						jumpOperation = AsmInstruction.Operation.JPL;
					else
						jumpOperation = AsmInstruction.Operation.JMI;

				else if(specificStatement.operationType == ILAssign2Statement.OperationType.GR)
					if(rightOperandType.equals(SimpleType.BOOLEAN))
						jumpOperation = AsmInstruction.Operation.JMI;
					else
						jumpOperation = AsmInstruction.Operation.JPL;

				else if(specificStatement.operationType == ILAssign2Statement.OperationType.LEQ) {
					jumpTrue = false;
					if(rightOperandType.equals(SimpleType.BOOLEAN))
						jumpOperation = AsmInstruction.Operation.JMI;
					else
						jumpOperation = AsmInstruction.Operation.JPL;

				} else if(specificStatement.operationType == ILAssign2Statement.OperationType.GEQ) {
					jumpTrue = false;
					if(rightOperandType.equals(SimpleType.BOOLEAN))
						jumpOperation = AsmInstruction.Operation.JPL;
					else
						jumpOperation = AsmInstruction.Operation.JMI;
				}


				asmProgram.add(new AsmInstruction(
						jumpOperation,
						new IAsmOperand[] {
								new ConstantOperand(label1)
						}));

				// ????????????????????????????????????
				asmProgram.add(new AsmInstruction(
						AsmInstruction.Operation.LAD,
						new IAsmOperand[] {
								RegisterOperand.registers[tmpRegister],
								new ConstantOperand(jumpTrue ? 0x0000 : 0xFFFF)
						}));

				asmProgram.add(new AsmInstruction(
						AsmInstruction.Operation.JUMP,
						new IAsmOperand[] {
								new ConstantOperand(label2)
						}));

				asmProgram.add(new AsmLabel(label1));

				// ???????????????????????????
				asmProgram.add(new AsmInstruction(
						AsmInstruction.Operation.LAD,
						new IAsmOperand[] {
								RegisterOperand.registers[tmpRegister],
								new ConstantOperand(jumpTrue ? 0xFFFF : 0x0000)
						}));

				asmProgram.add(new AsmLabel(label2));
			}

		}


		// ?????????????????????
		if(leftHandSideRegister != -1) {
			// ??????????????????????????????

			// ?????????????????????
			allocateRegister(
					leftHandSide.variableName,
					leftHandSideRegister,
					symbolTables,
					false
					);

			// GR0?????????????????????????????????
			if(leftHandSideRegister != tmpRegister)
				asmProgram.add(new AsmInstruction(
						AsmInstruction.Operation.LD,
						new IAsmOperand[] {
								RegisterOperand.registers[leftHandSideRegister],
								RegisterOperand.registers[tmpRegister]
						}));
		} else {
			// ???????????????????????????????????????
			var symbolTableEntry = symbolTables.findVariableFromAnyTable(leftHandSide.variableName);
			addVariableInstruction(
					symbolTableEntry,
					AsmInstruction.Operation.ST,
					RegisterOperand.registers[tmpRegister]);
		}


		// ????????????????????????????????????????????????????????????????????????
		if(leftHandSide instanceof ILSimpleVariableOperand) {
			var tempVariableEntry = symbolTables.findVariableFromAnyTable(leftHandSide.variableName);

			if(tempVariableEntry.entryType == SymbolTable.EntryType.TEMPVARIABLE)
				tempVariableEntry.typeExpression = leftHandSideType;
		}



	}


}
