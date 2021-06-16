package enshud.s4.optimizer;

import java.util.HashSet;
import java.util.ListIterator;

import enshud.flowgraph.FlowGraphNode;
import enshud.flowgraph.FlowGraphProgram;
import enshud.interlanguage.iloperand.ILConstantOperand;
import enshud.interlanguage.iloperand.ILSimpleVariableOperand;
import enshud.interlanguage.ilstatement.AbstractILStatement;

public class DataFlowOptimizer {

	public boolean optimize(FlowGraphProgram flowGraphs) {

		boolean optimized = false;

		// あとで削除する文の集合
		var meaninglessStatements = new HashSet<AbstractILStatement>();

		var checkedStatements = new HashSet<AbstractILStatement>();
		for(FlowGraphNode basicBlock : flowGraphs)
			for(AbstractILStatement ilStatement : basicBlock)
				checkedStatements.add(ilStatement);

		boolean updated;
		// 文を削除したことによって新たに冗長になる文も削除
		do {
			updated = false;
			var nextCheckedStatements = new HashSet<AbstractILStatement>();

			for(AbstractILStatement ilStatement : checkedStatements) {

				// 定数に置換できるオペランドを置換
				if(ilStatement.isOperandReplaceableByConstant()) {

					for(var it = flowGraphs.statementVariableRefSet.get(ilStatement).entrySet().iterator(); it.hasNext();) {
						var currentEntry = it.next();
						ILSimpleVariableOperand operand = currentEntry.getKey();
						var refStatements = currentEntry.getValue();

						if(refStatements.size() != 1)
							continue;

						AbstractILStatement refStatement = refStatements.iterator().next();
						if(!refStatement.isConstant())
							continue;

						updated = true;
						optimized = true;
						// オペランドの定義が一意に定まり，その値が定数である場合
						ILConstantOperand constantOperand = refStatement.getConstantValue();

						System.out.print(String.format("@@ replace constant %d: \"%s\"", ilStatement.hashCode(), ilStatement));

						// 複写文の右辺を定数で置き換える(アセンブラが優秀なのでオーバーフローの心配はない！)
						ilStatement.replaceOperandReference(operand, constantOperand);

						System.out.println(String.format(" to \"%s\"", ilStatement));

						// データフローの依存関係を削除
						flowGraphs.referringStatements.get(refStatement).remove(ilStatement);
						it.remove();

						// 削除した依存先を次にチェック
						nextCheckedStatements.add(refStatement);

						// この文を参照している文もチェックする
						if(ilStatement.isConstant() && flowGraphs.referringStatements.containsKey(ilStatement))
							nextCheckedStatements.addAll(flowGraphs.referringStatements.get(ilStatement));
					}
				}


				boolean isMeaningless = false;
				if(!flowGraphs.referringStatements.containsKey(ilStatement))
					isMeaningless = true;
				else {

					// どの文からも参照されていない
					isMeaningless |= flowGraphs.referringStatements.get(ilStatement).size() == 0;

					// 自分自身からしか参照されていない
					isMeaningless |= flowGraphs.referringStatements.get(ilStatement).size() == 1 &&
							flowGraphs.referringStatements.get(ilStatement).iterator().next().equals(ilStatement);
				}

				// 削除可能でないとダメ
				isMeaningless &= ilStatement.isRemovable();

				// 冗長な文を削除
				if(isMeaningless) {

					// どの文からも参照されていない．かつ削除可能

					System.out.println(String.format("@@ meaningless statement %d: \"%s\"", ilStatement.hashCode(), ilStatement));
					meaninglessStatements.add(ilStatement);
					nextCheckedStatements.remove(ilStatement);	// 一度削除した文を再度検査しないため

					updated = true;
					optimized = true;
					// 削除した文が依存していた文を次に検査する
					for(ILSimpleVariableOperand refOperand : flowGraphs.statementVariableRefSet.get(ilStatement).keySet())
						for(AbstractILStatement refStatement : flowGraphs.statementVariableRefSet.get(ilStatement).get(refOperand)) {
							if(!ilStatement.equals(ilStatement))
								nextCheckedStatements.add(refStatement);
							// データフローの依存を削除
							flowGraphs.referringStatements.get(refStatement).remove(ilStatement);
						}
				}

			}

			checkedStatements = nextCheckedStatements;
		} while (updated);


		/*
		 * TODO
		 * - 左辺が配列でも，右辺を定数に置換できるようにする
		 */


		// 不要な文を削除
		for(FlowGraphNode basicBlock : flowGraphs)
			for(ListIterator<AbstractILStatement> it = basicBlock.listIterator(); it.hasNext();) {

				var ilStatement = it.next();
				if(meaninglessStatements.contains(ilStatement))
					it.remove();

			}

		return optimized;
	}

}
