package enshud.s4.ilgenerator.visitors;

import java.util.HashMap;
import java.util.Map;

import enshud.s3.checker.TypeExpressionMap;
import enshud.symboltable.SymbolTable;
import enshud.symboltable.SymbolTableStack;
import enshud.syntaxtree.AbstractSyntaxNode;
import enshud.syntaxtree.AbstractVisitor;
import enshud.syntaxtree.SyntaxNodeStack;

public class VariableNamePropagator extends AbstractVisitor {

	private int sourceIndex;
	private HashMap<AbstractSyntaxNode, String> variableMap;

	public VariableNamePropagator(HashMap<AbstractSyntaxNode, String> variableMap, int sourceIndex) {
		this.sourceIndex = sourceIndex;
		this.variableMap = variableMap;
	}

	public VariableNamePropagator(HashMap<AbstractSyntaxNode, String> variableMap) {
		this(variableMap, 0);
	}


	// 対応する変数を構文木の上へ伝搬させる
	public void postorder(
			TypeExpressionMap typeExpressions,
			Map<AbstractSyntaxNode, SymbolTable> symbolTables,

			SyntaxNodeStack syntaxNodeStack,
			SymbolTableStack symbolTableStack) {

		String variable = variableMap.get(syntaxNodeStack.getLast().get(sourceIndex));

		variableMap.put(syntaxNodeStack.getLast(), variable);
	}

}
