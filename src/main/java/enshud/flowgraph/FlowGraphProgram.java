package enshud.flowgraph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;

import enshud.interlanguage.iloperand.ILSimpleVariableOperand;
import enshud.interlanguage.ilstatement.AbstractILStatement;
import enshud.interlanguage.ilstatement.ILProcedureCallStatement;
import enshud.symboltable.SymbolTable;

public class FlowGraphProgram extends HashMap<String, FlowGraphNode> implements Iterable<FlowGraphNode> {

	public HashMap<String, FlowGraphNode> procedureTailBlocks;

	public class FlowGraphProgramIterator implements Iterator<FlowGraphNode> {
		FlowGraphNode nextNode = null;

		Iterator<String> procedureNameIterator = null;
		Stack<FlowGraphNode> nodeStack;
		HashSet<FlowGraphNode> visited;

		public FlowGraphProgramIterator() {
			procedureNameIterator = FlowGraphProgram.this.keySet().iterator();
			nodeStack = new Stack<FlowGraphNode>();
			visited = new HashSet<FlowGraphNode>();

			if(procedureNameIterator.hasNext()) {
				nextNode = FlowGraphProgram.this.get(procedureNameIterator.next());
				visited.add(nextNode);

				if(nextNode.child != null && !visited.contains(nextNode.child))
					nodeStack.push(nextNode.child);

				for(var conditionalChild : nextNode.conditionalChildren)
					if(!visited.contains(conditionalChild))
						nodeStack.push(conditionalChild);

			} else
				nextNode = null;
		}

		@Override
		public boolean hasNext() {
			return nextNode != null;
		}

		@Override
		public FlowGraphNode next() {
			var res = nextNode;

			if(!nodeStack.empty()) {
				nextNode = nodeStack.pop();
				visited.add(nextNode);

				if(nextNode.child != null && !visited.contains(nextNode.child))
					nodeStack.push(nextNode.child);

				for(var conditionalChild : nextNode.conditionalChildren)
					if(!visited.contains(conditionalChild))
						nodeStack.push(conditionalChild);

			} else if(procedureNameIterator.hasNext()) {
				nextNode = FlowGraphProgram.this.get(procedureNameIterator.next());
				visited.add(nextNode);

				if(nextNode.child != null && !visited.contains(nextNode.child))
					nodeStack.push(nextNode.child);

				for(var conditionalChild : nextNode.conditionalChildren)
					if(!visited.contains(conditionalChild))
						nodeStack.push(conditionalChild);

			} else {
				nextNode = null;
			}

			return res;
		}

	}

	@Override
	public Iterator<FlowGraphNode> iterator() {
		return new FlowGraphProgramIterator();
	}

	@Override
	public String toString() {
		String res = "";
		for(String procedureName : this.keySet()) {
			if(res.length() > 0)
				res += "\n";
			res += String.format("-----[%s]-----\n", procedureName);

			visited = new HashSet<FlowGraphNode>();
			var headBlock = this.get(procedureName);
			res += flowGraphToString(headBlock);;
		}

		return res;
	}

	private HashSet<FlowGraphNode> visited;
	private String flowGraphToString(FlowGraphNode node) {
		visited.add(node);
		String res = node + "\n";

		if(node.child != null && !visited.contains(node.child))
			res += flowGraphToString(node.child);

		for(var conditionalChild : node.conditionalChildren)
			if(!visited.contains(conditionalChild))
				res += flowGraphToString(conditionalChild);

		return res;
	}


	// ?????????????????????????????????????????????????????????????????????
	public HashMap<AbstractILStatement, HashMap<ILSimpleVariableOperand, HashSet<AbstractILStatement>>> statementVariableInSet;
	// ?????????????????????????????????????????????????????????????????????
	public HashMap<AbstractILStatement, HashMap<ILSimpleVariableOperand, HashSet<AbstractILStatement>>> statementVariableRefSet;
	// ??????????????????????????????????????????
	public HashMap<AbstractILStatement, HashSet<AbstractILStatement>> referringStatements;


	public void calculateProcedureTail() {
		this.procedureTailBlocks = new HashMap<String, FlowGraphNode>();

		// ?????????????????????????????????1???????????????
		for(String procedureName : this.keySet()) {

			FlowGraphNode tailBlock = null;

			var visited = new HashSet<FlowGraphNode>();
			var stack = new Stack<FlowGraphNode>();
			stack.add(this.get(procedureName));

			while(!stack.empty()) {
				var block = stack.pop();
				visited.add(block);

				if(block.child == null &&
						(block.conditionalChildren.size() == 0)) {
					tailBlock = block;
					break;
				}

				if(block.child != null && !visited.contains(block.child))
					stack.add(block.child);
				for(var child : block.conditionalChildren)
					if(!visited.contains(child))
						stack.add(child);
			}

			this.procedureTailBlocks.put(procedureName, tailBlock);
		}
	}

	public void calculateDataFlow() {

		var statementGenSet = new HashMap<AbstractILStatement, HashSet<AbstractILStatement>>();
		var statementKillSet = new HashMap<AbstractILStatement, HashSet<AbstractILStatement>>();
		var statementInSet = new HashMap<AbstractILStatement, HashSet<AbstractILStatement>>();
		var statementOutSet = new HashMap<AbstractILStatement, HashSet<AbstractILStatement>>();

		var blockGenSet = new HashMap<FlowGraphNode, HashSet<AbstractILStatement>>();
		var blockKillSet = new HashMap<FlowGraphNode, HashSet<AbstractILStatement>>();
		var blockInSet = new HashMap<FlowGraphNode, HashSet<AbstractILStatement>>();
		var blockOutSet = new HashMap<FlowGraphNode, HashSet<AbstractILStatement>>();

		statementVariableInSet = new HashMap<AbstractILStatement, HashMap<ILSimpleVariableOperand, HashSet<AbstractILStatement>>>();
		statementVariableRefSet = new HashMap<AbstractILStatement, HashMap<ILSimpleVariableOperand, HashSet<AbstractILStatement>>>();
		referringStatements = new HashMap<AbstractILStatement, HashSet<AbstractILStatement>>();

		// ?????????????????????????????????????????????
		calculateProcedureTail();

		// ??????????????????????????????????????????????????????????????????
		var variableDefinitionsMap = new HashMap<SymbolTable.SymbolTableEntry, HashSet<AbstractILStatement>>();
		for(FlowGraphNode basicBlock : this) {
			var symbolTables = basicBlock.symbolTableStack;

			for(AbstractILStatement ilStatement : basicBlock)
				for(ILSimpleVariableOperand defVariable : ilStatement.getDefSet()) {
					var symbolTableEntry = symbolTables.findVariableFromAnyTable(defVariable.variableName);

					if(!variableDefinitionsMap.containsKey(symbolTableEntry))
						variableDefinitionsMap.put(symbolTableEntry, new HashSet<AbstractILStatement>());
					variableDefinitionsMap.get(symbolTableEntry).add(ilStatement);
				}
		}


		// ????????????gen, kill???????????????
		for(FlowGraphNode basicBlock : this) {
			var symbolTables = basicBlock.symbolTableStack;

			for(AbstractILStatement ilStatement : basicBlock) {
				statementGenSet.put(ilStatement, new HashSet<AbstractILStatement>());
				statementKillSet.put(ilStatement, new HashSet<AbstractILStatement>());

				if(ilStatement.getDefSet().size() > 0)
					statementGenSet.get(ilStatement).add(ilStatement);
				else
					continue;

				for(ILSimpleVariableOperand defVariable : ilStatement.getDefSet()) {
					var symbolTableEntry = symbolTables.findVariableFromAnyTable(defVariable.variableName);

					var variableDefinitions = new HashSet<AbstractILStatement>(variableDefinitionsMap.get(symbolTableEntry));
					variableDefinitions.remove(ilStatement);
					statementKillSet.get(ilStatement).addAll(variableDefinitions);
				}

			}
		}

		// ???????????????????????????gen, kill???????????????
		for(FlowGraphNode basicBlock : this) {
			blockGenSet.put(basicBlock, new HashSet<AbstractILStatement>());
			blockKillSet.put(basicBlock, new HashSet<AbstractILStatement>());

			int currentLine = -1;
			for(AbstractILStatement ilStatement : basicBlock) {
				var genSet = statementGenSet.get(ilStatement);
				var killSet = statementKillSet.get(ilStatement);

				if(++currentLine == 0) {
					blockGenSet.get(basicBlock).addAll(genSet);
					blockKillSet.get(basicBlock).addAll(killSet);

				} else {
					blockGenSet.get(basicBlock).removeAll(killSet);
					blockGenSet.get(basicBlock).addAll(genSet);

					blockKillSet.get(basicBlock).removeAll(genSet);
					blockKillSet.get(basicBlock).addAll(killSet);
				}
			}
		}


		// ??????????????????????????????????????????????????????????????????????????????
		var procedureCallers = new HashMap<FlowGraphNode, HashSet<FlowGraphNode>>();
		var procedureReturnDests = new HashMap<FlowGraphNode, HashSet<FlowGraphNode>>();

		for(FlowGraphNode basicBlock : this) {
			if(basicBlock.size() == 0)
				continue;

			var lastStatement = basicBlock.get(basicBlock.size() - 1);
			if(!(lastStatement instanceof ILProcedureCallStatement))
				continue;

			String procedureName = ((ILProcedureCallStatement)lastStatement).procedureName;
			FlowGraphNode procedureHeadBlock = this.get(procedureName);

			// ???????????????????????????
			if(!procedureCallers.containsKey(procedureHeadBlock))
				procedureCallers.put(procedureHeadBlock, new HashSet<FlowGraphNode>());
			procedureCallers.get(procedureHeadBlock).add(basicBlock);

			// ????????????????????????
			FlowGraphNode procedureHeadTail = this.procedureTailBlocks.get(procedureName);
			if(basicBlock.child != null && procedureHeadTail != null /* ????????????????????????????????? */) {
				if(!procedureReturnDests.containsKey(basicBlock.child))
					procedureReturnDests.put(basicBlock.child, new HashSet<FlowGraphNode>());
				procedureReturnDests.get(basicBlock.child).add(procedureHeadTail);
			}

		}


		// ???????????????????????????in, out???????????????
		for(FlowGraphNode basicBlock : this) {
			blockInSet.put(basicBlock, new HashSet<AbstractILStatement>());
			blockOutSet.put(basicBlock, new HashSet<AbstractILStatement>(blockGenSet.get(basicBlock)));
		}

		boolean updated;
		do {
			updated = false;

			for(FlowGraphNode basicBlock : this) {
				blockInSet.put(basicBlock, new HashSet<AbstractILStatement>());

				if(procedureReturnDests.containsKey(basicBlock)) {
					// ?????????????????????????????????????????????
					for(FlowGraphNode predecessor : procedureReturnDests.get(basicBlock))
						blockInSet.get(basicBlock).addAll(blockOutSet.get(predecessor));

				} else if(basicBlock.parent != null) {
					// ???????????????????????????????????????????????????????????????????????????
					blockInSet.get(basicBlock).addAll(blockOutSet.get(basicBlock.parent));
				}
				// ????????????????????????
				for(FlowGraphNode conditionalParent : basicBlock.conditionalParents)
					blockInSet.get(basicBlock).addAll(blockOutSet.get(conditionalParent));
				// ?????????????????????
				if(procedureCallers.containsKey(basicBlock))
					for(FlowGraphNode procedureCaller : procedureCallers.get(basicBlock))
						blockInSet.get(basicBlock).addAll(blockOutSet.get(procedureCaller));

				var newOutSet = new HashSet<AbstractILStatement>(blockInSet.get(basicBlock));
				newOutSet.removeAll(blockKillSet.get(basicBlock));
				newOutSet.addAll(blockGenSet.get(basicBlock));

				if(!newOutSet.equals(blockOutSet.get(basicBlock)))
					updated = true;

				blockOutSet.put(basicBlock, newOutSet);
			}
		} while(updated);


		// ????????????in, out???????????????
		for(FlowGraphNode basicBlock : this) {

			var curInSet = blockInSet.get(basicBlock);
			for(AbstractILStatement ilStatement : basicBlock) {
				statementInSet.put(ilStatement, new HashSet<AbstractILStatement>(curInSet));

				curInSet.removeAll(statementKillSet.get(ilStatement));
				curInSet.addAll(statementGenSet.get(ilStatement));
				statementOutSet.put(ilStatement, new HashSet<AbstractILStatement>(curInSet));
			}
		}

		// in????????????????????????????????????
		for(AbstractILStatement ilStatement : statementInSet.keySet())
			for(AbstractILStatement inStatement : statementInSet.get(ilStatement))
				for(ILSimpleVariableOperand defOperand : inStatement.getDefSet()) {
					// ?????????????????????????????????????????????
					if(ilStatement.symbolTableStack.findVariableFromAnyTable(defOperand.variableName) != null) {

						if(!statementVariableInSet.containsKey(ilStatement))
							statementVariableInSet.put(ilStatement, new HashMap<ILSimpleVariableOperand, HashSet<AbstractILStatement>>());

						if(!statementVariableInSet.get(ilStatement).containsKey(defOperand))
							statementVariableInSet.get(ilStatement).put(defOperand, new HashSet<AbstractILStatement>());

						statementVariableInSet.get(ilStatement).get(defOperand).add(inStatement);
					}

				}


		// ???????????????ref??????(?????????????????????????????????????????????)?????????
		// TODO O(??????^2)????????????????????????????????????...

		for(FlowGraphNode basicBlock : this)
			for(AbstractILStatement ilStatement : basicBlock) {
				statementVariableRefSet.put(ilStatement, new HashMap<ILSimpleVariableOperand, HashSet<AbstractILStatement>>());

				for(AbstractILStatement inStatement : statementInSet.get(ilStatement)) {

					// ilStatement???refOperands??????inStatement???defOperands?????????????????????(????????????????????????????????????????????????????????????)
					var variableIntersectionSet = new HashSet<ILSimpleVariableOperand>();

					for(ILSimpleVariableOperand refVariable : ilStatement.getRefSet())
						for(ILSimpleVariableOperand defVariable : inStatement.getDefSet()) {
							var refVariableEntry = ilStatement.symbolTableStack.findVariableFromAnyTable(refVariable.variableName);
							var defVariableEntry = inStatement.symbolTableStack.findVariableFromAnyTable(defVariable.variableName);

							if(refVariableEntry.equals(defVariableEntry))
								variableIntersectionSet.add(refVariable);
						}


					// 2???????????????????????????????????????in????????????ref??????????????????
					for(ILSimpleVariableOperand operand : variableIntersectionSet) {
						if(!statementVariableRefSet.get(ilStatement).containsKey(operand))
							statementVariableRefSet.get(ilStatement).put(operand, new HashSet<AbstractILStatement>());

						statementVariableRefSet.get(ilStatement).get(operand).add(inStatement);
					}

					if(variableIntersectionSet.size() > 0) {
						if(!referringStatements.containsKey(inStatement))
							referringStatements.put(inStatement, new HashSet<AbstractILStatement>());
						referringStatements.get(inStatement).add(ilStatement);
					}


				}
			}


		/*
		// 3????????????????????????
		for(FlowGraphNode basicBlock : this) {
			System.out.println("---");
			System.out.println(String.format("[%d]", basicBlock.hashCode()));

			for(AbstractILStatement ilStatement : basicBlock)
				System.out.println(String.format("%d: \t%s", ilStatement.hashCode(), ilStatement.toString()));
		}

		// ????????????gen???????????????
		System.out.println("@@@ statementGenSet");
		for(var ilStatement : statementGenSet.keySet()) {
			System.out.print(String.format("%d -> ", ilStatement.hashCode()));
			for(var genStatement : statementGenSet.get(ilStatement))
				System.out.print(String.format("%d, ", genStatement.hashCode()));
			System.out.println();
		}

		// ????????????kill???????????????
		System.out.println("@@@ statementKillSet");
		for(var ilStatement : statementKillSet.keySet()) {
			System.out.print(String.format("%d -> ", ilStatement.hashCode()));
			for(var genStatement : statementKillSet.get(ilStatement))
				System.out.print(String.format("%d, ", genStatement.hashCode()));
			System.out.println();
		}

		// ?????????????????????gen???????????????
		System.out.println("@@@ blockGenSet");
		for(var block : blockGenSet.keySet()) {
			System.out.print(String.format("%d -> ", block.hashCode()));
			for(var genStatement : blockGenSet.get(block))
				System.out.print(String.format("%d, ", genStatement.hashCode()));
			System.out.println();
		}

		// ?????????????????????kill???????????????
		System.out.println("@@@ blockKillSet");
		for(var block : blockKillSet.keySet()) {
			System.out.print(String.format("%d -> ", block.hashCode()));
			for(var genStatement : blockKillSet.get(block))
				System.out.print(String.format("%d, ", genStatement.hashCode()));
			System.out.println();
		}


		// ?????????????????????in???????????????
		System.out.println("@@@ blockInSet");
		for(var block : blockInSet.keySet()) {
			System.out.print(String.format("%d -> ", block.hashCode()));
			for(var genStatement : blockInSet.get(block))
				System.out.print(String.format("%d, ", genStatement.hashCode()));
			System.out.println();
		}

		// ?????????????????????out???????????????
		System.out.println("@@@ blockOutSet");
		for(var block : blockOutSet.keySet()) {
			System.out.print(String.format("%d -> ", block.hashCode()));
			for(var genStatement : blockOutSet.get(block))
				System.out.print(String.format("%d, ", genStatement.hashCode()));
			System.out.println();
		}

		// ????????????in???????????????
		System.out.println("@@@ statementInSet");
		for(var ilStatement : statementInSet.keySet()) {
			System.out.print(String.format("%d -> ", ilStatement.hashCode()));
			for(var genStatement : statementInSet.get(ilStatement))
				System.out.print(String.format("%d, ", genStatement.hashCode()));
			System.out.println();
		}

		// ????????????out???????????????
		System.out.println("@@@ statementOutSet");
		for(var ilStatement : statementOutSet.keySet()) {
			System.out.print(String.format("%d -> ", ilStatement.hashCode()));
			for(var genStatement : statementOutSet.get(ilStatement))
				System.out.print(String.format("%d, ", genStatement.hashCode()));
			System.out.println();
		}


		// ????????????referringStatements?????????
		System.out.println("@@@ referringStatements");
		for(var ilStatement : referringStatements.keySet()) {
			System.out.print(String.format("%d -> ", ilStatement.hashCode()));
			for(var genStatement : referringStatements.get(ilStatement))
				System.out.print(String.format("%d, ", genStatement.hashCode()));
			System.out.println();
		}
		*/

	}

}
