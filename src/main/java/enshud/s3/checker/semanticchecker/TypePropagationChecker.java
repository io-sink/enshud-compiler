package enshud.s3.checker.semanticchecker;

import java.util.Map;

import enshud.s3.checker.CheckerException;
import enshud.s3.checker.TypeExpressionMap;
import enshud.symboltable.SymbolTable;
import enshud.symboltable.SymbolTableStack;
import enshud.syntaxtree.AbstractSyntaxNode;
import enshud.syntaxtree.AbstractVisitor;
import enshud.syntaxtree.SyntaxNodeStack;

// 構文木のsourceIndex番目の子ノードの型を伝搬させる
public class TypePropagationChecker extends AbstractVisitor {

	int sourceIndex;
	public TypePropagationChecker(int sourceIndex) {
		this.sourceIndex = sourceIndex;
	}

	public TypePropagationChecker() {
		this(0);
	}


	@Override
	public void postorder(
			TypeExpressionMap typeExpressions,
			Map<AbstractSyntaxNode, SymbolTable> symbolTables,

			SyntaxNodeStack syntaxNodeStack,
			SymbolTableStack symbolTableStack
			) throws CheckerException {


		var typeExpression = typeExpressions.get(syntaxNodeStack.getLast().get(sourceIndex));

		typeExpressions.put(syntaxNodeStack.getLast(), typeExpression);
	}
}
