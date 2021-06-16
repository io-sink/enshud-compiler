package enshud.s4.ilgenerator.visitors;

import java.util.HashMap;
import java.util.Map;

import enshud.interlanguage.iloperand.ILConstantOperand;
import enshud.interlanguage.iloperand.ILSimpleVariableOperand;
import enshud.interlanguage.ilstatement.ILArgPushStatement;
import enshud.interlanguage.ilstatement.ILWriteStatement;
import enshud.s3.checker.TypeExpressionMap;
import enshud.s4.ilgenerator.ILCodeBlock;
import enshud.s4.ilgenerator.TempVariablePool;
import enshud.symboltable.SymbolTable;
import enshud.symboltable.SymbolTableStack;
import enshud.syntaxtree.AbstractSyntaxNode;
import enshud.syntaxtree.AbstractVisitor;
import enshud.syntaxtree.SyntaxNodeStack;
import enshud.typeexpression.ArrayType;
import enshud.typeexpression.SimpleType;

public class ExpressionsILCodeGenerator extends AbstractVisitor {

	private HashMap<AbstractSyntaxNode, String> variableMap;
	private HashMap<AbstractSyntaxNode, ILCodeBlock> ilCodeBlocks;
	private TempVariablePool tempVariablePool;

	public ExpressionsILCodeGenerator(
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
		if(expressionNode.variableName.equals("Expression")) {

			String argumentName = variableMap.get(expressionNode);

			// オペランドが一時変数なら解放
			tempVariablePool.free(argumentName);

			if(syntaxNodeStack.get(-2).variableName.equals("IOStatement")) {

				if(argumentName.charAt(0) == '\'') {
					// 文字列定数をwritelnの引数に渡す場合
					ilCodeBlock.add(new ILWriteStatement(
							new ILConstantOperand(argumentName, new ArrayType(SimpleType.CHAR, 0, argumentName.length() - 3))
							));
				} else {
					ilCodeBlock.add(new ILWriteStatement(
							new ILSimpleVariableOperand(argumentName)
							));
				}

			} else {
				ilCodeBlock.add(new ILArgPushStatement(
						new ILSimpleVariableOperand(argumentName)
						));

			}


		}


	}
}
