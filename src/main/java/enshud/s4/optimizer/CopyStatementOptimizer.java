package enshud.s4.optimizer;

import java.util.HashSet;

import enshud.flowgraph.FlowGraphNode;
import enshud.flowgraph.FlowGraphProgram;
import enshud.interlanguage.iloperand.ILSimpleVariableOperand;
import enshud.interlanguage.ilstatement.AbstractILStatement;
import enshud.interlanguage.ilstatement.ILCopyStatement;
import enshud.symboltable.SymbolTable;

public class CopyStatementOptimizer {

	/*
	 * x := ...
	 * y := x
	 * を，
	 * y := ...
	 * に置き換える(これら2つの文は連続している必要がある)．
	 */
	public boolean optimize(FlowGraphProgram flowGraphs) {

		boolean optimized = false;

		for(FlowGraphNode basicBlock : flowGraphs) {

			AbstractILStatement prevStatement = null;
			for(var it = basicBlock.listIterator(); it.hasNext();) {
				AbstractILStatement ilStatement = it.next();

				try {

					if(!(ilStatement instanceof ILCopyStatement &&
							((ILCopyStatement)ilStatement).leftHandSide instanceof ILSimpleVariableOperand &&
							((ILCopyStatement)ilStatement).rightHandSide instanceof ILSimpleVariableOperand))
						throw new Exception("skip");

					// 両辺が純変数の複写文]
					var leftHandSide = (ILSimpleVariableOperand)((ILCopyStatement)ilStatement).leftHandSide;
					var rightHandSide = (ILSimpleVariableOperand)((ILCopyStatement)ilStatement).rightHandSide;

					var leftHandSideEntry = basicBlock.symbolTableStack.findVariableFromAnyTable(leftHandSide.variableName);
					var rightHandSideEntry = basicBlock.symbolTableStack.findVariableFromAnyTable(rightHandSide.variableName);

					// 仮パラメータは置き換えられない
					if(rightHandSideEntry.entryType == SymbolTable.EntryType.TEMPPARAMETER)
						throw new Exception("skip");

					int leftHandSideScopeDepth = basicBlock.symbolTableStack.getScopeDepth(leftHandSideEntry);
					int rightHandSideScopeDepth = basicBlock.symbolTableStack.getScopeDepth(rightHandSideEntry);


					if(leftHandSideScopeDepth > rightHandSideScopeDepth)
						throw new Exception("skip");
					// 左辺のスコープのほうが広い場合のみ適用

					// 右辺の変数の定義を探す
					if(flowGraphs.statementVariableRefSet.get(ilStatement).get(rightHandSide).size() != 1)
						// 定義が一意でなければ中止
						throw new Exception("skip");

					// 右辺の唯一の定義
					var rightHandSideRefStatement =
							flowGraphs.statementVariableRefSet.get(ilStatement).get(rightHandSide).iterator().next();

					// **rightHandSideRefStatementとilStatementが連続している必要がある**
					if(!rightHandSideRefStatement.equals(prevStatement))
						throw new Exception("skip");

					// 右辺の変数の参照を探す
					for(AbstractILStatement referringStatement : flowGraphs.referringStatements.get(rightHandSideRefStatement)) {
						if(referringStatement.equals(ilStatement))
							continue;

						/*

						// 右辺の変数を参照している文に入る左辺の定義が一意でない場合は中止
						if(flowGraphs.statementVariableInSet.get(referringStatement).get(leftHandSide).size() != 1)
							throw new Exception("skip");

						// 唯一の定義がilStatementでない場合は中止
						var inStatement =
								flowGraphs.statementVariableInSet.get(referringStatement).get(leftHandSide).iterator().next();
						if(!inStatement.equals(ilStatement))
							throw new Exception("skip");

						*/

						// 右辺の変数を参照している文に入る右辺の定義が一意でない場合は中止
						if(flowGraphs.statementVariableRefSet.get(referringStatement).get(rightHandSide).size() != 1)
							throw new Exception("skip");

						// 唯一の定義がrightHandSideRefStatementでない場合は中止
						var refStatement =
								flowGraphs.statementVariableRefSet.get(referringStatement).get(rightHandSide).iterator().next();
						if(!refStatement.equals(rightHandSideRefStatement))
							throw new Exception("skip");
					}


					System.out.println(String.format("@@ copy statement optimized %d, %d: \"%s\", \"%s\"",
							rightHandSideRefStatement.hashCode(), ilStatement.hashCode(),
							rightHandSideRefStatement, ilStatement));

					optimized = true;

					rightHandSideRefStatement.replaceOperandDefinition(rightHandSide, leftHandSide);

					// 右辺の変数の参照を左辺の変数に変更
					for(AbstractILStatement referringStatement : flowGraphs.referringStatements.get(rightHandSideRefStatement)) {
						referringStatement.replaceOperandReference(rightHandSide, leftHandSide);

						flowGraphs.statementVariableRefSet.get(referringStatement).remove(rightHandSide);
						if(!flowGraphs.statementVariableRefSet.get(referringStatement).containsKey(leftHandSide))
							flowGraphs.statementVariableRefSet.get(referringStatement).put(leftHandSide, new HashSet<AbstractILStatement>());
						flowGraphs.statementVariableRefSet.get(referringStatement).get(leftHandSide).add(rightHandSideRefStatement);
					}


					// 右辺の変数の定義を左辺の変数に変更
					if(flowGraphs.referringStatements.containsKey(ilStatement)) {
						var refStatements = flowGraphs.referringStatements.get(ilStatement);

						flowGraphs.referringStatements.get(rightHandSideRefStatement).addAll(refStatements);
						for(AbstractILStatement referringStatement : refStatements) {
							flowGraphs.statementVariableRefSet.get(referringStatement).get(leftHandSide).remove(ilStatement);
							flowGraphs.statementVariableRefSet.get(referringStatement).get(leftHandSide).add(rightHandSideRefStatement);
						}
					}

					// 削除を実行
					it.remove();
					flowGraphs.referringStatements.get(rightHandSideRefStatement).remove(ilStatement);


				} catch(Exception ex) {
					if(ex.getMessage() == null || !ex.getMessage().equals("skip"))
						throw new RuntimeException(ex.getMessage());

				}

				prevStatement = ilStatement;
			}
		}

		return optimized;
	}

}
