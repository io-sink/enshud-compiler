package enshud.s4.asmgenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

import enshud.asm.AsmProgram;
import enshud.asm.instruction.AsmComment;
import enshud.asm.instruction.AsmInstruction;
import enshud.asm.instruction.AsmLabel;
import enshud.asm.operand.ConstantOperand;
import enshud.asm.operand.IAsmOperand;
import enshud.asm.operand.RegisterOperand;
import enshud.flowgraph.FlowGraphNode;
import enshud.flowgraph.FlowGraphProgram;
import enshud.interlanguage.ilstatement.ILArgPushStatement;
import enshud.interlanguage.ilstatement.ILAssign1Statement;
import enshud.interlanguage.ilstatement.ILAssign2Statement;
import enshud.interlanguage.ilstatement.ILConditionalJump;
import enshud.interlanguage.ilstatement.ILCopyStatement;
import enshud.interlanguage.ilstatement.ILLabelDefinition;
import enshud.interlanguage.ilstatement.ILProcedureCallStatement;
import enshud.interlanguage.ilstatement.ILReadStatement;
import enshud.interlanguage.ilstatement.ILReadlnStatement;
import enshud.interlanguage.ilstatement.ILUnconditionalJump;
import enshud.interlanguage.ilstatement.ILWriteStatement;
import enshud.interlanguage.ilstatement.ILWritelnStatement;
import enshud.s4.asmgenerator.generators.AbstractAsmGenerator;
import enshud.s4.asmgenerator.generators.ArgPushStatementAsmGenerator;
import enshud.s4.asmgenerator.generators.AssignStatementAsmGenerator;
import enshud.s4.asmgenerator.generators.ConditionalJumpAsmGenerator;
import enshud.s4.asmgenerator.generators.CopyStatementAsmGenerator;
import enshud.s4.asmgenerator.generators.LabelDefinitionAsmGenerator;
import enshud.s4.asmgenerator.generators.ProcedureCallStatementAsmGenerator;
import enshud.s4.asmgenerator.generators.ReadStatementAsmGenerator;
import enshud.s4.asmgenerator.generators.ReadlnStatementAsmGenerator;
import enshud.s4.asmgenerator.generators.UnconditionalJumpAsmGenerator;
import enshud.s4.asmgenerator.generators.WriteStatementAsmGenerator;
import enshud.s4.asmgenerator.generators.WritelnStatementAsmGenerator;
import enshud.s4.compiler.Compiler;
import enshud.s4.ilgenerator.LabelGenerator;

public class AsmCodeGenerator {

	private FlowGraphProgram flowGraphs;
	private HashMap<String, String> constantAllocation;
	private LabelGenerator labelGenerator;

	private HashMap<Class<?>, AbstractAsmGenerator> generators;

	public AsmProgram asmProgram;
	private BiHashMap<String, Integer> currentRegisterAllocation;

	public static boolean debugPrint = false;

	public AsmCodeGenerator(
			FlowGraphProgram flowGraphs,
			LabelGenerator labelGenerator
			) {

		this.flowGraphs = flowGraphs;
		this.labelGenerator = labelGenerator;
	}

	String libbufLabel;
	public AsmProgram generate() {

		var globalSymbolTables = flowGraphs.get(".main").symbolTableStack;
		String ProgramName = globalSymbolTables.getProgramName();

		// ????????????????????????????????????????????????????????????
		constantAllocation = (new ConstantAllocator()).allocate(flowGraphs);

		// ?????????????????????????????????
		var variableAllocator = new ProcedureVariableAllocator(globalSymbolTables, ".main");
		int grobalVariablesSizeSum = variableAllocator.allocate();


		asmProgram = new AsmProgram(ProgramName);

		// ?????????????????????????????????????????????????????????????????????
		libbufLabel = labelGenerator.get();

		// START??????
		asmProgram.add(new AsmLabel(".LProcedure" + asmProgram));
		String entryLabel = ".LEntry" + ".main";
		asmProgram.add(new AsmInstruction(AsmInstruction.Operation.START,
				new IAsmOperand[] {
						new ConstantOperand(entryLabel)
				}));

		// ???????????????
		generateProcedure(".main");
		for (String procedureName : flowGraphs.keySet())
			if(!(procedureName.equals(".main")))
				generateProcedure(procedureName);


		// ??????????????????????????????
		for(var it = globalSymbolTables.visibleVariableIterator(); it.hasNext();) {
			String variableName = it.next();
			String labelName = ".LGlobal" + variableName;

			asmProgram.add(new AsmComment(variableName));

			asmProgram.add(new AsmLabel(labelName));
			asmProgram.add(new AsmInstruction(AsmInstruction.Operation.DS,
					new IAsmOperand[] {
							new ConstantOperand(globalSymbolTables.findVariableFromLastTable(variableName).size)
					}));

		}

		// ????????????????????????
		for(var constantValue : constantAllocation.keySet()) {
			asmProgram.add(new AsmLabel(constantAllocation.get(constantValue)));
			asmProgram.add(new AsmInstruction(AsmInstruction.Operation.DC,
					new IAsmOperand[] {
							new ConstantOperand(constantValue)
					}));
		}

		// ?????????????????????????????????????????????????????????
		asmProgram.add(new AsmLabel(libbufLabel));
		asmProgram.add(new AsmInstruction(
				AsmInstruction.Operation.DS,
				new IAsmOperand[] {
						new ConstantOperand(256)
						}));

		// END??????
		asmProgram.add(new AsmInstruction(AsmInstruction.Operation.END, null));


		return asmProgram;
	}


	StackPointerRevision stackPointerRevision;
	private void generateProcedure(String procedureName) {
		// ???????????????????????????????????????
		var localSymbolTables = flowGraphs.get(procedureName).symbolTableStack;
		var variableAllocator = new ProcedureVariableAllocator(localSymbolTables, procedureName);
		int localVariablesSizeSum = variableAllocator.allocate();
		stackPointerRevision = new StackPointerRevision();

		if(debugPrint) {
			// ??????????????????
			System.out.println(String.format("%s: %d, %d",
					procedureName,
					flowGraphs.get(procedureName).hashCode(),
					flowGraphs.procedureTailBlocks.get(procedureName).hashCode()));
			System.out.println(localSymbolTables.get(localSymbolTables.size() - 1));
		}

		// ???????????????????????????????????????????????????
		var basicBlockHeads = new ArrayList<FlowGraphNode>();

		var stack = new Stack<FlowGraphNode>();
		var visited = new HashSet<FlowGraphNode>();
		stack.push(flowGraphs.get(procedureName));
		while(!stack.empty()) {
			var basicBlock = stack.pop();
			visited.add(basicBlock);

			if(basicBlock.parent == null)
				basicBlockHeads.add(basicBlock);

			for(var child : basicBlock.conditionalChildren)
				if(!visited.contains(child))
					stack.add(child);

			if(basicBlock.child != null && !visited.contains(basicBlock.child))
				stack.add(basicBlock.child);
		}


		asmProgram.add(new AsmComment(""));
		asmProgram.add(new AsmComment(String.format("[%s]", procedureName)));

		boolean retGenerated = false;

		// ??????????????????(???????????????????????????)
		for(var basicBlockHead : basicBlockHeads)
			if(!(basicBlockHead.equals(flowGraphs.get(procedureName))))
				if(generateBasicBlockFlow(procedureName, basicBlockHead, localVariablesSizeSum)) {

					// ?????????????????????????????????
					generateProcedureTail(localVariablesSizeSum);
					retGenerated = true;
				}

		// ?????????????????????????????????
		String entryLabel = ".LEntry" + procedureName;
		asmProgram.add(new AsmLabel(entryLabel));

		if(procedureName.equals(".main")) {
			// ????????????????????????????????????????????????GR6, GR7????????????
			asmProgram.add(new AsmInstruction(
					AsmInstruction.Operation.LAD,
					new IAsmOperand[] {
							RegisterOperand.GR6,
							new ConstantOperand(0)
							}));
			asmProgram.add(new AsmInstruction(
					AsmInstruction.Operation.LAD,
					new IAsmOperand[] {
							RegisterOperand.GR7,
							new ConstantOperand(libbufLabel)
							}));
		}



		// ?????????????????????????????????????????????
		asmProgram.add(new AsmInstruction(
				AsmInstruction.Operation.SUBA,
				new IAsmOperand[] {
						RegisterOperand.GR8,
						new ConstantOperand("=" + localVariablesSizeSum)
						}));

		// ???????????????(?????????????????????)
		if(generateBasicBlockFlow(procedureName, flowGraphs.get(procedureName), localVariablesSizeSum)) {

			// ?????????????????????????????????
			generateProcedureTail(localVariablesSizeSum);
			retGenerated = true;
		}

		if(!retGenerated) {
			// RET????????????????????????????????? <=> ???????????????????????????
			// ????????????????????????????????????????????????????????????????????????????????????????????????RET???????????????????????????
			generateProcedureTail(localVariablesSizeSum);
		}

	}

	// RET????????????????????????????????????
	private boolean generateBasicBlockFlow(
			String procedureName,
			FlowGraphNode basicBlockHead,
			int localVariablesSizeSum) {

		FlowGraphNode basicBlock = basicBlockHead;
		FlowGraphNode lastBlock = basicBlock;
		while(basicBlock != null) {
			lastBlock = basicBlock;
			generateBasicBlock(procedureName, basicBlock);

			basicBlock = basicBlock.child;
		}

		// ?????????????????????????????????
		var tailBlock = flowGraphs.procedureTailBlocks.get(procedureName);

		return tailBlock != null && tailBlock.equals(lastBlock);
	}

	private void generateProcedureTail(int localVariablesSizeSum) {
		// ??????????????????????????????????????????????????????
		asmProgram.add(new AsmInstruction(
				AsmInstruction.Operation.ADDA,
				new IAsmOperand[] {
						RegisterOperand.GR8,
						new ConstantOperand("=" + localVariablesSizeSum)
						}));

		// END??????
		asmProgram.add(new AsmInstruction(AsmInstruction.Operation.RET, null));
	}

	private void generateBasicBlock(String procedureName, FlowGraphNode basicBlock) {
		if(basicBlock.size() == 0)
			return;

		currentRegisterAllocation = new BiHashMap<String, Integer>();

		// 3??????????????????????????????????????????????????????????????????????????????????????????
		generators = new HashMap<Class<?>, AbstractAsmGenerator>();
		generators.put(ILCopyStatement.class,
				new CopyStatementAsmGenerator(asmProgram, currentRegisterAllocation, stackPointerRevision));
		generators.put(ILAssign1Statement.class,
				new AssignStatementAsmGenerator(asmProgram, currentRegisterAllocation, labelGenerator, stackPointerRevision));
		generators.put(ILAssign2Statement.class,
				new AssignStatementAsmGenerator(asmProgram, currentRegisterAllocation, labelGenerator, stackPointerRevision));
		generators.put(ILArgPushStatement.class,
				new ArgPushStatementAsmGenerator(asmProgram, currentRegisterAllocation, stackPointerRevision));
		generators.put(ILConditionalJump.class,
				new ConditionalJumpAsmGenerator(asmProgram, currentRegisterAllocation, stackPointerRevision));
		generators.put(ILLabelDefinition.class,
				new LabelDefinitionAsmGenerator(asmProgram, currentRegisterAllocation, stackPointerRevision));
		generators.put(ILProcedureCallStatement.class,
				new ProcedureCallStatementAsmGenerator(asmProgram, currentRegisterAllocation, stackPointerRevision));
		generators.put(ILUnconditionalJump.class,
				new UnconditionalJumpAsmGenerator(asmProgram, currentRegisterAllocation, stackPointerRevision));
		generators.put(ILWriteStatement.class,
				new WriteStatementAsmGenerator(asmProgram, currentRegisterAllocation, constantAllocation, stackPointerRevision));
		generators.put(ILReadStatement.class,
				new ReadStatementAsmGenerator(asmProgram, currentRegisterAllocation, stackPointerRevision));
		generators.put(ILWritelnStatement.class,
				new WritelnStatementAsmGenerator(asmProgram, currentRegisterAllocation, stackPointerRevision));
		generators.put(ILReadlnStatement.class,
				new ReadlnStatementAsmGenerator(asmProgram, currentRegisterAllocation, stackPointerRevision));


		var registerAllocator = new RegisterAllocator();
		basicBlock.allocativePlan = registerAllocator.allocate(basicBlock);

		if(debugPrint)
			System.out.println(basicBlock);

		asmProgram.add(new AsmComment("---"));

		// 3???????????????????????????????????????????????????????????????
		boolean variableStored = false;
		int currentLine = -1;
		for(var ilStatement : basicBlock) {
			++currentLine;


			// ???????????????????????????????????????????????????????????????
			if(debugPrint) {
				System.out.print(String.format("%d: %s\t\t(", currentLine, ilStatement));
				for(var operand : basicBlock.allocativePlan.get(currentLine).keySet())
					System.out.print(String.format("%s: %s, ", operand,
							RegisterOperand.registers[basicBlock.allocativePlan.get(currentLine).get(operand)]));
				System.out.println(")");
			}


			asmProgram.add(new AsmComment(ilStatement.toString()));

			if(!generators.containsKey(ilStatement.getClass()))
				continue;


			// ??????????????????????????????????????????????????????
			if(ilStatement instanceof ILConditionalJump ||
					ilStatement instanceof ILUnconditionalJump) {
				if(Compiler.allocatable)
					storeVariables(procedureName);
				variableStored = true;
			}


			generators.get(ilStatement.getClass()).generate(
					ilStatement,
					flowGraphs.get(procedureName).symbolTableStack,
					basicBlock.allocativePlan.get(currentLine),
					currentRegisterAllocation);


			if(debugPrint) {
				// ??????????????????????????????
				System.out.print("{");
				for(String variable : currentRegisterAllocation.keySet())
					System.out.print(String.format("%s: %d, ", variable, currentRegisterAllocation.get(variable)));
				System.out.println("}");
			}


		}
		if(debugPrint)
			System.out.println("\n");

		if(!variableStored && Compiler.allocatable)
			 storeVariables(procedureName);

	}

	private void storeVariables(String procedureName) {

		asmProgram.add(new AsmComment("store variables"));

		// ?????????????????????????????????????????????????????????
		for(String variableName : currentRegisterAllocation.keySet()) {
			if(variableName == null)
				continue;

			var symbolTableEntry = flowGraphs.get(procedureName).symbolTableStack
					.findVariableFromAnyTable(variableName);
			int registerNumber = currentRegisterAllocation.get(variableName);

			// ???????????????
			var newInstruction = AbstractAsmGenerator.getVariableInstruction(
							symbolTableEntry,
							AsmInstruction.Operation.ST,
							RegisterOperand.registers[registerNumber],
							stackPointerRevision);
			asmProgram.add(newInstruction);
		}
	}

}
