package enshud.s3.checker.semanticchecker;

import java.util.Map;

import enshud.s3.checker.CheckerException;
import enshud.s3.checker.TypeExpressionMap;
import enshud.symboltable.SymbolTable;
import enshud.symboltable.SymbolTableStack;
import enshud.syntaxtree.AbstractSyntaxNode;
import enshud.syntaxtree.AbstractVisitor;
import enshud.syntaxtree.SyntaxNodeStack;
import enshud.syntaxtree.TerminalNode;

public class VariableNameChecker extends AbstractVisitor {

	@Override
	public void preorder(
			TypeExpressionMap typeExpressions,
			Map<AbstractSyntaxNode, SymbolTable> symbolTables,

			SyntaxNodeStack syntaxNodeStack,
			SymbolTableStack symbolTableStack
			) throws CheckerException {

		var syntaxNode = syntaxNodeStack.getLast();
		String variableName = ((TerminalNode)syntaxNode.get(0)).token.content;

		// 変数宣言時もチェックが行われるが問題ない
		// 変数がいずれかの記号表に定義済みでなければエラー

		var variableEntry = symbolTableStack.findVariableFromAnyTable(variableName);
		if(variableEntry == null)
			throw new CheckerException(
					String.format("Variable '%s' is not declared", variableName),
					syntaxNode);
	}

	@Override
	public void postorder(
			TypeExpressionMap typeExpressions,
			Map<AbstractSyntaxNode, SymbolTable> symbolTables,

			SyntaxNodeStack syntaxNodeStack,
			SymbolTableStack symbolTableStack
			) throws CheckerException {


		// 変数宣言時もチェックが行われるが問題ない
		// 記号表に登録されている型を取り出し，変数の型として登録
		String variableName = ((TerminalNode)syntaxNodeStack.getLast().get(0)).token.content;
		var variableTableEntry = symbolTableStack.findVariableFromAnyTable(variableName);

		typeExpressions.put(syntaxNodeStack.getLast(), variableTableEntry.typeExpression);
	}

}
