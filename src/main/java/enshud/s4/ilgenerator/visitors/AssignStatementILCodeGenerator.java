package enshud.s4.ilgenerator.visitors;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

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

public class AssignStatementILCodeGenerator extends AbstractVisitor {

	private HashMap<AbstractSyntaxNode, String> variableMap;
	private HashMap<AbstractSyntaxNode, ILCodeBlock> ilCodeBlocks;
	private TempVariablePool tempVariablePool;

	public AssignStatementILCodeGenerator(
			HashMap<AbstractSyntaxNode, String> variableMap,
			HashMap<AbstractSyntaxNode, ILCodeBlock> ilCodeBlocks,
			TempVariablePool tempVariablePool) {

		this.variableMap = variableMap;
		this.ilCodeBlocks = ilCodeBlocks;
		this.tempVariablePool = tempVariablePool;
	}

	@Override
	public void postorder(
			TypeExpressionMap typeExpressions,
			Map<AbstractSyntaxNode, SymbolTable> symbolTables,

			SyntaxNodeStack syntaxNodeStack,
			SymbolTableStack symbolTableStack
			) throws CheckerException {

		// 直近のILコードブロックを取得
		var ilCodeBlock = ilCodeBlocks.get(syntaxNodeStack.getLastVariable("CompoundStatements"));

		String leftVariableName = variableMap.get(syntaxNodeStack.getLast().get(0));
		String rightVariableName = variableMap.get(syntaxNodeStack.getLast().get(2));

		// 右辺が一時変数なら解放
		tempVariablePool.free(rightVariableName);

		// 左辺が添え字付き引数なら，添え字を解放
		var pattern = Pattern.compile("(?<=\\[).+(?=\\])");
		var match = pattern.matcher(leftVariableName);
		if(match.find()) {
			// 左辺が添え字付き引数
			String arrayName = leftVariableName.split("\\[")[0];
			String indexName = match.group();

			tempVariablePool.free(indexName);

			ilCodeBlock.add(new ILCopyStatement(
					new ILIndexedVariableOperand(arrayName, indexName),
					new ILSimpleVariableOperand(rightVariableName),
					typeExpressions.get(syntaxNodeStack.getLast().get(0))
					));
		} else {

			ilCodeBlock.add(new ILCopyStatement(
					new ILSimpleVariableOperand(leftVariableName),
					new ILSimpleVariableOperand(rightVariableName),
					typeExpressions.get(syntaxNodeStack.getLast().get(0))
					));
		}

	}
}
