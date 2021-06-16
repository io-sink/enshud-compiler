package enshud.s4.flowglaph;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import enshud.flowgraph.FlowGraphNode;
import enshud.flowgraph.FlowGraphProgram;
import enshud.interlanguage.ilstatement.ILCodeBlockCall;
import enshud.interlanguage.ilstatement.ILConditionalJump;
import enshud.interlanguage.ilstatement.ILLabelDefinition;
import enshud.interlanguage.ilstatement.ILProcedureCallStatement;
import enshud.interlanguage.ilstatement.ILReadStatement;
import enshud.interlanguage.ilstatement.ILReadlnStatement;
import enshud.interlanguage.ilstatement.ILUnconditionalJump;
import enshud.interlanguage.ilstatement.ILWriteStatement;
import enshud.interlanguage.ilstatement.ILWritelnStatement;
import enshud.s3.checker.TypeExpressionMap;
import enshud.s4.ilgenerator.ILCodeBlock;
import enshud.symboltable.SymbolTable;
import enshud.syntaxtree.AbstractSyntaxNode;

public class FlowGraphGenerator {

	private Map<AbstractSyntaxNode, SymbolTable> symbolTables;
	private TypeExpressionMap typeExpressions;

	private HashMap<String, AbstractSyntaxNode> procedureNodeMap;
	private HashMap<AbstractSyntaxNode, ILCodeBlock> nodeILCodeBlockMap;

	public FlowGraphGenerator(
			Map<AbstractSyntaxNode, SymbolTable> symbolTables,
			TypeExpressionMap typeExpressions,

			HashMap<String, AbstractSyntaxNode> procedureNodeMap,
			HashMap<AbstractSyntaxNode, ILCodeBlock> nodeILCodeBlockMap
			) {

		this.symbolTables = symbolTables;
		this.typeExpressions = typeExpressions;
		this.procedureNodeMap = procedureNodeMap;
		this.nodeILCodeBlockMap = nodeILCodeBlockMap;
	}

	public FlowGraphProgram flowGraphs;
	public HashMap<String, FlowGraphNode> labelMap;

	private LinkedList<FlowGraphNode> basicCodeBlocks;

	/*
	 	☆コンパイルに必要な全ての情報を構文木から取り出す
	 		- 手続き名→フローグラフのノード()
	 		- ラベル名→フローグラフのノード
	 */
	public void generate() {
		this.flowGraphs = new FlowGraphProgram();
		this.labelMap = new HashMap<String, FlowGraphNode>();

		this.basicCodeBlocks = new LinkedList<FlowGraphNode>();
		for (String procedureName : this.procedureNodeMap.keySet()) {
			// フローグラフの作成
			var flowGraphHead = generateFlowGraph(this.procedureNodeMap.get(procedureName));
			this.flowGraphs.put(procedureName, flowGraphHead);
		}

		// フローグラフに条件付き/無条件ジャンプの辺を追加
		addFlowGraphEdges();

	}


	// フローグラフを作成(要addFlowGraphEdges)
	private FlowGraphNode generateFlowGraph(AbstractSyntaxNode syntaxNode) {
		var ilCodeBlock = this.nodeILCodeBlockMap.get(syntaxNode);

		var firstBasicBlock = new FlowGraphNode(ilCodeBlock.symbolTableStack);
		basicCodeBlocks.add(firstBasicBlock);

		// 次に来るコードが追加される基本ブロックのインデックス
		int currentBasicBlockIndex = basicCodeBlocks.size() - 1;
		var currentBasicBlock = basicCodeBlocks.get(currentBasicBlockIndex);

		for (var ilStatement : ilCodeBlock) {
			currentBasicBlockIndex = basicCodeBlocks.size() - 1;
			currentBasicBlock = basicCodeBlocks.get(currentBasicBlockIndex);

			if((ilStatement instanceof ILLabelDefinition ||
					ilStatement instanceof ILProcedureCallStatement ||
					ilStatement instanceof ILReadStatement ||
					ilStatement instanceof ILWriteStatement ||
					ilStatement instanceof ILReadlnStatement ||
					ilStatement instanceof ILWritelnStatement) &&
					currentBasicBlock.size() > 0) {

				// 新しい基本ブロックに移るべき命令，かつ直前にあらたな基本ブロックに切り替わっていない
				var nextBasicBlock = new FlowGraphNode(ilCodeBlock.symbolTableStack);
				basicCodeBlocks.add(nextBasicBlock);

				currentBasicBlock.setChild(nextBasicBlock);
				nextBasicBlock.setParent(currentBasicBlock);

				currentBasicBlockIndex = basicCodeBlocks.size() - 1;
				currentBasicBlock = basicCodeBlocks.get(currentBasicBlockIndex);
			}

			// ラベルと基本ブロックの対応付けを追加
			if(ilStatement instanceof ILLabelDefinition)
				this.labelMap.put(((ILLabelDefinition)ilStatement).labelName, currentBasicBlock);

			if((ilStatement instanceof ILCodeBlockCall)) {

				var foreignBlockHead = generateFlowGraph(((ILCodeBlockCall)ilStatement).node);
				var foreignBlockTail = basicCodeBlocks.get(basicCodeBlocks.size() - 1);
				currentBasicBlock.setChild(foreignBlockHead);
				foreignBlockHead.setParent(currentBasicBlock);

				// 次に新しい基本ブロックに移るべき命令
				var nextBasicBlock = new FlowGraphNode(ilCodeBlock.symbolTableStack);
				foreignBlockTail.setChild(nextBasicBlock);
				nextBasicBlock.setParent(foreignBlockTail);

				basicCodeBlocks.add(nextBasicBlock);

				// リストの最後のフローグラフノードが空なら削除
				if(currentBasicBlock.size() == 0) {
					basicCodeBlocks.remove(currentBasicBlockIndex);
					currentBasicBlock.removeMyself();
				}

				currentBasicBlockIndex = basicCodeBlocks.size() - 1;
				currentBasicBlock = basicCodeBlocks.get(currentBasicBlockIndex);


			} else {
				// 文に記号票の情報を付加して，現在の基本ブロックに末尾に追加
				ilStatement.symbolTableStack = ilCodeBlock.symbolTableStack;
				currentBasicBlock.add(ilStatement);
			}

			if((ilStatement instanceof ILConditionalJump ||
					ilStatement instanceof ILUnconditionalJump ||
					ilStatement instanceof ILProcedureCallStatement ||
					ilStatement instanceof ILReadStatement ||
					ilStatement instanceof ILWriteStatement ||
					ilStatement instanceof ILReadlnStatement ||
					ilStatement instanceof ILWritelnStatement
					) && currentBasicBlock.size() > 0) {

				// 次に新しい基本ブロックに移るべき命令
				var nextBasicBlock = new FlowGraphNode(ilCodeBlock.symbolTableStack);

				// 前後の基本ブロックに辺を張る
				if (!(ilStatement instanceof ILUnconditionalJump)) {
					currentBasicBlock.setChild(nextBasicBlock);
					nextBasicBlock.setParent(currentBasicBlock);
				}

				basicCodeBlocks.add(nextBasicBlock);

				currentBasicBlockIndex = basicCodeBlocks.size() - 1;
				currentBasicBlock = basicCodeBlocks.get(currentBasicBlockIndex);
			}
		}


		// リストの最後のフローグラフノードが空なら削除
		if(currentBasicBlock.size() == 0) {
			basicCodeBlocks.remove(currentBasicBlockIndex);
			currentBasicBlock.removeMyself();
		}

		return firstBasicBlock;
	}

	// フローグラフに条件付き/無条件ジャンプの辺を追加
	private void addFlowGraphEdges() {
		for(var basicBlock : basicCodeBlocks) {
			var lastStatement = basicBlock.get(basicBlock.size() - 1);

			if(lastStatement instanceof ILConditionalJump) {
				String labelName = ((ILConditionalJump)lastStatement).labelName;
				basicBlock.conditionalChildren.add(this.labelMap.get(labelName));
				this.labelMap.get(labelName).conditionalParents.add(basicBlock);
			}

			if(lastStatement instanceof ILUnconditionalJump) {
				String labelName = ((ILUnconditionalJump)lastStatement).labelName;
				basicBlock.conditionalChildren.add(this.labelMap.get(labelName));
				this.labelMap.get(labelName).conditionalParents.add(basicBlock);
			}
		}
	}

}
