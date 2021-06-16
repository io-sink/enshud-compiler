package enshud.s4.optimizer;

import java.util.HashMap;
import java.util.HashSet;

import enshud.flowgraph.FlowGraphNode;
import enshud.flowgraph.FlowGraphProgram;
import enshud.interlanguage.iloperand.ILConstantOperand;
import enshud.interlanguage.ilstatement.ILConditionalJump;
import enshud.interlanguage.ilstatement.ILUnconditionalJump;

public class ControlFlowOptimizer {


	/*
	 * 条件式が定数の条件付きジャンプを除去
	 * 制御フローグラフが変更されたらtrueを返す
	 *
	 */
	public boolean optimize(
			FlowGraphProgram flowGraphs,
			HashMap<String, FlowGraphNode> labelMap) {

		boolean optimized = false;
		FlowGraphNode potentialTail = null;

		for(var block : flowGraphs) {
			if(block.size() == 0)
				continue;

			if(!(block.get(block.size() - 1) instanceof ILConditionalJump))
				continue;

			var jumpStatement = (ILConditionalJump)block.get(block.size() - 1);
			if(!(jumpStatement.condition instanceof ILConstantOperand))
				continue;

			optimized = true;
			System.out.println("@@ jump removed");

			if(((ILConstantOperand)jumpStatement.condition).getBooleanValue()) {
				// 条件式がtrue

				// ジャンプ文を削除
				block.removeLast();

				// 直系の子ノードを削除
				if(block.child != null) {
					block.child.parent = null;
					block.child = null;
				}

				var jumpBlock = labelMap.get(jumpStatement.labelName);

				// 無条件ジャンプ文を追加
				var newJumpStatement = new ILUnconditionalJump(jumpStatement.labelName);
				newJumpStatement.symbolTableStack = jumpStatement.symbolTableStack;
				block.add(newJumpStatement);
				block.conditionalChildren.add(jumpBlock);
				jumpBlock.conditionalParents.add(block);

			} else {
				// 条件式がfalse

				// ジャンプ文を削除
				block.removeLast();

				// フローグラフのジャンプ先との辺を削除
				var jumpBlock = labelMap.get(jumpStatement.labelName);
				block.conditionalChildren.remove(jumpBlock);
				jumpBlock.conditionalParents.remove(block);
			}
		}

		// フローグラフから離れた基本ブロックへのリンクを削除
		var flowGraphNodes = new HashSet<FlowGraphNode>();
		for(var block : flowGraphs)
			flowGraphNodes.add(block);

		for(var block : flowGraphs)
			for(var it = block.conditionalParents.iterator(); it.hasNext();)
				if(!flowGraphNodes.contains(it.next()))
					it.remove();

		return optimized;
	}
}
