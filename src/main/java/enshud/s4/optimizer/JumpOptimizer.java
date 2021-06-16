package enshud.s4.optimizer;

import java.util.HashMap;

import enshud.flowgraph.FlowGraphNode;
import enshud.flowgraph.FlowGraphProgram;
import enshud.interlanguage.ilstatement.ILConditionalJump;
import enshud.interlanguage.ilstatement.ILLabelDefinition;
import enshud.interlanguage.ilstatement.ILUnconditionalJump;

public class JumpOptimizer {

	public boolean optimize(
			FlowGraphProgram flowGraphs,
			HashMap<String, FlowGraphNode> labelMap) {

		boolean optimized = false;

		// ジャンプ命令へジャンプする命令を簡約化
		var labeledUnconditionalJumps = new HashMap<String, ILUnconditionalJump>();

		// ラベル直後のジャンプを取得
		for(var block : flowGraphs) {
			if(block.size() < 2)
				continue;

			var label = block.get(0);
			var jump = block.get(1);


			if(!(label instanceof ILLabelDefinition) || !(jump instanceof ILUnconditionalJump))
				continue;

			if(jump instanceof ILUnconditionalJump)
				labeledUnconditionalJumps.put(((ILLabelDefinition)label).labelName, (ILUnconditionalJump)jump);
		}


		for(var block : flowGraphs) {
			if(block.size() == 0)
				continue;

			var jump = block.get(block.size() - 1);

			String oldForeignLabel = null;
			FlowGraphNode oldForeignBlock = null;

			if(jump instanceof ILUnconditionalJump) {
				oldForeignLabel = ((ILUnconditionalJump)jump).labelName;
				oldForeignBlock = labelMap.get(oldForeignLabel);

			} else if(jump instanceof ILConditionalJump) {
				oldForeignLabel = ((ILConditionalJump)jump).labelName;
				oldForeignBlock = labelMap.get(oldForeignLabel);
			} else
				continue;

			optimized = true;

			if(labeledUnconditionalJumps.containsKey(oldForeignLabel)) {
				String newForeignLabel = labeledUnconditionalJumps.get(oldForeignLabel).labelName;
				var newForeignBlock = labelMap.get(newForeignLabel);

				// 飛び先を変更
				((ILUnconditionalJump)jump).labelName = newForeignLabel;

				// フローグラフの辺を付け替える
				block.conditionalChildren.remove(oldForeignBlock);
				block.conditionalChildren.add(newForeignBlock);
				oldForeignBlock.conditionalParents.remove(block);
				newForeignBlock.conditionalParents.add(block);

				System.out.println("@@ jump optimized");

			}

		}

		return optimized;
	}

}
