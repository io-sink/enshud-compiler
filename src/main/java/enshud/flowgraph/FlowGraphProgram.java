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


	// ある文が参照している変数を定義している文の集合
	public HashMap<AbstractILStatement, HashMap<ILSimpleVariableOperand, HashSet<AbstractILStatement>>> statementVariableInSet;
	// ある文が参照している変数を定義している文の集合
	public HashMap<AbstractILStatement, HashMap<ILSimpleVariableOperand, HashSet<AbstractILStatement>>> statementVariableRefSet;
	// ある文を参照している文の集合
	public HashMap<AbstractILStatement, HashSet<AbstractILStatement>> referringStatements;


	public void calculateProcedureTail() {
		this.procedureTailBlocks = new HashMap<String, FlowGraphNode>();

		// プログラムの出口は高々1つしかない
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

		// 各手続きの最後のブロックを決定
		calculateProcedureTail();

		// 変数ごとに，変数を定義している文の集合を計算
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


		// 文ごとにgen, kill集合を計算
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

		// 基本ブロックごとにgen, kill集合を計算
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


		// 手続きの呼び出しによるフローグラフの辺を整理しておく
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

			// 手続きへ遷移する辺
			if(!procedureCallers.containsKey(procedureHeadBlock))
				procedureCallers.put(procedureHeadBlock, new HashSet<FlowGraphNode>());
			procedureCallers.get(procedureHeadBlock).add(basicBlock);

			// 手続きから戻る辺
			FlowGraphNode procedureHeadTail = this.procedureTailBlocks.get(procedureName);
			if(basicBlock.child != null && procedureHeadTail != null /* 無限ループが生成された */) {
				if(!procedureReturnDests.containsKey(basicBlock.child))
					procedureReturnDests.put(basicBlock.child, new HashSet<FlowGraphNode>());
				procedureReturnDests.get(basicBlock.child).add(procedureHeadTail);
			}

		}


		// 基本ブロックごとにin, out集合を計算
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
					// 手続き呼び出しの直後のブロック
					for(FlowGraphNode predecessor : procedureReturnDests.get(basicBlock))
						blockInSet.get(basicBlock).addAll(blockOutSet.get(predecessor));

				} else if(basicBlock.parent != null) {
					// 手続き呼び出しの直後は直系の親からの辺を考慮しない
					blockInSet.get(basicBlock).addAll(blockOutSet.get(basicBlock.parent));
				}
				// ジャンプによる親
				for(FlowGraphNode conditionalParent : basicBlock.conditionalParents)
					blockInSet.get(basicBlock).addAll(blockOutSet.get(conditionalParent));
				// 手続き呼び出し
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


		// 文ごとにin, out集合を計算
		for(FlowGraphNode basicBlock : this) {

			var curInSet = blockInSet.get(basicBlock);
			for(AbstractILStatement ilStatement : basicBlock) {
				statementInSet.put(ilStatement, new HashSet<AbstractILStatement>(curInSet));

				curInSet.removeAll(statementKillSet.get(ilStatement));
				curInSet.addAll(statementGenSet.get(ilStatement));
				statementOutSet.put(ilStatement, new HashSet<AbstractILStatement>(curInSet));
			}
		}

		// in集合を変数ごとにまとめる
		for(AbstractILStatement ilStatement : statementInSet.keySet())
			for(AbstractILStatement inStatement : statementInSet.get(ilStatement))
				for(ILSimpleVariableOperand defOperand : inStatement.getDefSet()) {
					// 文の環境の記号表に存在する変数
					if(ilStatement.symbolTableStack.findVariableFromAnyTable(defOperand.variableName) != null) {

						if(!statementVariableInSet.containsKey(ilStatement))
							statementVariableInSet.put(ilStatement, new HashMap<ILSimpleVariableOperand, HashSet<AbstractILStatement>>());

						if(!statementVariableInSet.get(ilStatement).containsKey(defOperand))
							statementVariableInSet.get(ilStatement).put(defOperand, new HashSet<AbstractILStatement>());

						statementVariableInSet.get(ilStatement).get(defOperand).add(inStatement);
					}

				}


		// 文ごとに，ref集合(実際に参照されている定義の集合)を計算
		// TODO O(行数^2)時間くらいかかっているが...

		for(FlowGraphNode basicBlock : this)
			for(AbstractILStatement ilStatement : basicBlock) {
				statementVariableRefSet.put(ilStatement, new HashMap<ILSimpleVariableOperand, HashSet<AbstractILStatement>>());

				for(AbstractILStatement inStatement : statementInSet.get(ilStatement)) {

					// ilStatementのrefOperandsと，inStatementのdefOperandsの積集合を取る(スコープまで考慮して記号表エントリで比較)
					var variableIntersectionSet = new HashSet<ILSimpleVariableOperand>();

					for(ILSimpleVariableOperand refVariable : ilStatement.getRefSet())
						for(ILSimpleVariableOperand defVariable : inStatement.getDefSet()) {
							var refVariableEntry = ilStatement.symbolTableStack.findVariableFromAnyTable(refVariable.variableName);
							var defVariableEntry = inStatement.symbolTableStack.findVariableFromAnyTable(defVariable.variableName);

							if(refVariableEntry.equals(defVariableEntry))
								variableIntersectionSet.add(refVariable);
						}


					// 2つの集合が交差していれば，in集合からref集合へコピー
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
		// 3番地コードを出力
		for(FlowGraphNode basicBlock : this) {
			System.out.println("---");
			System.out.println(String.format("[%d]", basicBlock.hashCode()));

			for(AbstractILStatement ilStatement : basicBlock)
				System.out.println(String.format("%d: \t%s", ilStatement.hashCode(), ilStatement.toString()));
		}

		// 文ごとにgen集合を出力
		System.out.println("@@@ statementGenSet");
		for(var ilStatement : statementGenSet.keySet()) {
			System.out.print(String.format("%d -> ", ilStatement.hashCode()));
			for(var genStatement : statementGenSet.get(ilStatement))
				System.out.print(String.format("%d, ", genStatement.hashCode()));
			System.out.println();
		}

		// 文ごとにkill集合を出力
		System.out.println("@@@ statementKillSet");
		for(var ilStatement : statementKillSet.keySet()) {
			System.out.print(String.format("%d -> ", ilStatement.hashCode()));
			for(var genStatement : statementKillSet.get(ilStatement))
				System.out.print(String.format("%d, ", genStatement.hashCode()));
			System.out.println();
		}

		// ブロックごとにgen集合を出力
		System.out.println("@@@ blockGenSet");
		for(var block : blockGenSet.keySet()) {
			System.out.print(String.format("%d -> ", block.hashCode()));
			for(var genStatement : blockGenSet.get(block))
				System.out.print(String.format("%d, ", genStatement.hashCode()));
			System.out.println();
		}

		// ブロックごとにkill集合を出力
		System.out.println("@@@ blockKillSet");
		for(var block : blockKillSet.keySet()) {
			System.out.print(String.format("%d -> ", block.hashCode()));
			for(var genStatement : blockKillSet.get(block))
				System.out.print(String.format("%d, ", genStatement.hashCode()));
			System.out.println();
		}


		// ブロックごとにin集合を出力
		System.out.println("@@@ blockInSet");
		for(var block : blockInSet.keySet()) {
			System.out.print(String.format("%d -> ", block.hashCode()));
			for(var genStatement : blockInSet.get(block))
				System.out.print(String.format("%d, ", genStatement.hashCode()));
			System.out.println();
		}

		// ブロックごとにout集合を出力
		System.out.println("@@@ blockOutSet");
		for(var block : blockOutSet.keySet()) {
			System.out.print(String.format("%d -> ", block.hashCode()));
			for(var genStatement : blockOutSet.get(block))
				System.out.print(String.format("%d, ", genStatement.hashCode()));
			System.out.println();
		}

		// 文ごとにin集合を出力
		System.out.println("@@@ statementInSet");
		for(var ilStatement : statementInSet.keySet()) {
			System.out.print(String.format("%d -> ", ilStatement.hashCode()));
			for(var genStatement : statementInSet.get(ilStatement))
				System.out.print(String.format("%d, ", genStatement.hashCode()));
			System.out.println();
		}

		// 文ごとにout集合を出力
		System.out.println("@@@ statementOutSet");
		for(var ilStatement : statementOutSet.keySet()) {
			System.out.print(String.format("%d -> ", ilStatement.hashCode()));
			for(var genStatement : statementOutSet.get(ilStatement))
				System.out.print(String.format("%d, ", genStatement.hashCode()));
			System.out.println();
		}


		// 文ごとにreferringStatementsを出力
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
