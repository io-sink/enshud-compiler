package enshud.s4.ilgenerator.visitors;

import java.util.HashMap;
import java.util.Map;

import enshud.interlanguage.iloperand.ILIndexedVariableOperand;
import enshud.interlanguage.iloperand.ILSimpleVariableOperand;
import enshud.interlanguage.ilstatement.ILCopyStatement;
import enshud.s3.checker.CheckerException;
import enshud.s3.checker.TypeExpressionMap;
import enshud.s4.ilgenerator.ILCodeBlock;
import enshud.s4.ilgenerator.TempVariablePool;
import enshud.symboltable.SymbolTable;
import enshud.symboltable.SymbolTableStack;
import enshud.syntaxtree.AbstractSyntaxNode;
import enshud.syntaxtree.AbstractVisitor;
import enshud.syntaxtree.SyntaxNodeStack;
import enshud.typeexpression.ArrayType;

public class IndexedVariablePropagator extends AbstractVisitor {

	private HashMap<AbstractSyntaxNode, String> variableMap;
	private HashMap<AbstractSyntaxNode, ILCodeBlock> ilCodeBlocks;
	private TempVariablePool tempVariablePool;

	public IndexedVariablePropagator(
			HashMap<AbstractSyntaxNode, String> variableMap,
			HashMap<AbstractSyntaxNode, ILCodeBlock> ilCodeBlocks,
			TempVariablePool tempVariablePool) {

		this.variableMap = variableMap;
		this.ilCodeBlocks = ilCodeBlocks;
		this.tempVariablePool = tempVariablePool;
	}

	// 配列の変数名を伝搬
	@Override
	public void postorder(
			TypeExpressionMap typeExpressions,
			Map<AbstractSyntaxNode, SymbolTable> symbolTables,

			SyntaxNodeStack syntaxNodeStack,
			SymbolTableStack symbolTableStack
			) throws CheckerException {

		// 直近のILコードブロックを取得
		var ilCodeBlock = ilCodeBlocks.get(syntaxNodeStack.getLastVariable("CompoundStatements"));

		var arrayName = variableMap.get(syntaxNodeStack.getLast().get(0));
		var indexName = variableMap.get(syntaxNodeStack.getLast().get(2));

		if(syntaxNodeStack.get(-3).variableName.equals("LeftHandSide") ||
				syntaxNodeStack.get(-3).variableName.equals("Variables") ) {
			// 左辺に含まれる添え字付き変数の場合

			// 配列の形のままで変数名を伝搬させる
			// TODO 添え字付き変数の表現をクラス化
			variableMap.put(syntaxNodeStack.getLast(), String.format("%s[%s]", arrayName, indexName));
		} else {

			// オペランドが一時変数なら解放
			tempVariablePool.free(indexName);
			// 新しい一時変数を取得
			String newTempVariableName = tempVariablePool.get();

			ilCodeBlock.add(new ILCopyStatement(
					new ILSimpleVariableOperand(newTempVariableName),
					new ILIndexedVariableOperand(arrayName, indexName),
					((ArrayType)typeExpressions.get(syntaxNodeStack.getLast().get(0))).contentType
					));

			variableMap.put(syntaxNodeStack.getLast(), newTempVariableName);
		}
	}
}
