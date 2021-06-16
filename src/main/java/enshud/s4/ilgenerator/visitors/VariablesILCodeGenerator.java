package enshud.s4.ilgenerator.visitors;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import enshud.interlanguage.iloperand.ILIndexedVariableOperand;
import enshud.interlanguage.iloperand.ILSimpleVariableOperand;
import enshud.interlanguage.ilstatement.ILReadStatement;
import enshud.s3.checker.TypeExpressionMap;
import enshud.s4.ilgenerator.ILCodeBlock;
import enshud.s4.ilgenerator.TempVariablePool;
import enshud.symboltable.SymbolTable;
import enshud.symboltable.SymbolTableStack;
import enshud.syntaxtree.AbstractSyntaxNode;
import enshud.syntaxtree.AbstractVisitor;
import enshud.syntaxtree.SyntaxNodeStack;

public class VariablesILCodeGenerator extends AbstractVisitor {

	private HashMap<AbstractSyntaxNode, String> variableMap;
	private HashMap<AbstractSyntaxNode, ILCodeBlock> ilCodeBlocks;
	private TempVariablePool tempVariablePool;

	public VariablesILCodeGenerator(
			HashMap<AbstractSyntaxNode, String> variableMap,
			HashMap<AbstractSyntaxNode, ILCodeBlock> ilCodeBlocks,
			TempVariablePool tempVariablePool) {

		this.variableMap = variableMap;
		this.ilCodeBlocks = ilCodeBlocks;
		this.tempVariablePool = tempVariablePool;
	}

	// nodeIndex(>0)番目の子ノードを訪問する前に呼び出される
	@Override
	public void inorder(
			int nodeIndex,
			TypeExpressionMap typeExpressions,
			Map<AbstractSyntaxNode, SymbolTable> symbolTables,

			SyntaxNodeStack syntaxNodeStack,
			SymbolTableStack symbolTableStack) throws Exception {

		// 直近のILコードブロックを取得
		var ilCodeBlock = ilCodeBlocks.get(syntaxNodeStack.getLastVariable("CompoundStatements"));


		AbstractSyntaxNode expressionNode = syntaxNodeStack.getLast().get(nodeIndex - 1);
		if(expressionNode.variableName.equals("Variable")) {

			String argumentName = variableMap.get(expressionNode);

			var pattern = Pattern.compile("(?<=\\[).+(?=\\])");
			var match = pattern.matcher(argumentName);
			if(match.find()) {
				// 添え字付き変数
				String arrayName = argumentName.split("\\[")[0];
				String indexName = match.group();

				tempVariablePool.free(indexName);

				ilCodeBlock.add(new ILReadStatement(
						new ILIndexedVariableOperand(arrayName, indexName)
						));
			} else {
				// 純変数のとき

				tempVariablePool.free(argumentName);

				ilCodeBlock.add(new ILReadStatement(
						new ILSimpleVariableOperand(argumentName)
						));
			}


		}


	}
}
