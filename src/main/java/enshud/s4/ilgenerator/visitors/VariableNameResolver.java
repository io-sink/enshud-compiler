package enshud.s4.ilgenerator.visitors;

import java.util.HashMap;
import java.util.Map;

import enshud.s3.checker.TypeExpressionMap;
import enshud.symboltable.SymbolTable;
import enshud.symboltable.SymbolTableStack;
import enshud.syntaxtree.AbstractSyntaxNode;
import enshud.syntaxtree.AbstractVisitor;
import enshud.syntaxtree.SyntaxNodeStack;
import enshud.syntaxtree.TerminalNode;

public class VariableNameResolver extends AbstractVisitor {

	private HashMap<AbstractSyntaxNode, String> variableMap;

	public VariableNameResolver(HashMap<AbstractSyntaxNode, String> variableMap) {
		this.variableMap = variableMap;
	}

	// 構文木から変数名を取得して伝搬させる
	public void postorder(
			TypeExpressionMap typeExpressions,
			Map<AbstractSyntaxNode, SymbolTable> symbolTables,

			SyntaxNodeStack syntaxNodeStack,
			SymbolTableStack symbolTableStack) {

		String variableName = ((TerminalNode)syntaxNodeStack.getLast().get(0)).token.content;

		variableMap.put(syntaxNodeStack.getLast(), variableName);
	}
}
