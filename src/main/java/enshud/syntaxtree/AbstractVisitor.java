package enshud.syntaxtree;

import java.util.Map;

import enshud.s3.checker.TypeExpressionMap;
import enshud.symboltable.SymbolTable;
import enshud.symboltable.SymbolTableStack;

public abstract class AbstractVisitor {

	public void preorder(
			TypeExpressionMap typeExpressions,
			Map<AbstractSyntaxNode, SymbolTable> symbolTables,

			SyntaxNodeStack syntaxNodeStack,
			SymbolTableStack symbolTableStack) throws Exception {
		return;
	}

	// nodeIndex(:0-オリジンで1～Nの範囲)番目の子ノードを訪問する前に呼び出される
	public void inorder(
			int nodeIndex,
			TypeExpressionMap typeExpressions,
			Map<AbstractSyntaxNode, SymbolTable> symbolTables,

			SyntaxNodeStack syntaxNodeStack,
			SymbolTableStack symbolTableStack) throws Exception {
		return;
	}

	public void postorder(
			TypeExpressionMap typeExpressions,
			Map<AbstractSyntaxNode, SymbolTable> symbolTables,

			SyntaxNodeStack syntaxNodeStack,
			SymbolTableStack symbolTableStack) throws Exception {
		return;
	}

}
