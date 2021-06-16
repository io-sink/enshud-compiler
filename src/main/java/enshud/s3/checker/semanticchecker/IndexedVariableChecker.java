package enshud.s3.checker.semanticchecker;

import java.util.Map;

import enshud.s3.checker.CheckerException;
import enshud.s3.checker.TypeExpressionMap;
import enshud.symboltable.SymbolTable;
import enshud.symboltable.SymbolTableStack;
import enshud.syntaxtree.AbstractSyntaxNode;
import enshud.syntaxtree.AbstractVisitor;
import enshud.syntaxtree.SyntaxNodeStack;
import enshud.typeexpression.ArrayType;
import enshud.typeexpression.SimpleType;

public class IndexedVariableChecker extends AbstractVisitor {

	@Override
	public void postorder(
			TypeExpressionMap typeExpressions,
			Map<AbstractSyntaxNode, SymbolTable> symbolTables,

			SyntaxNodeStack syntaxNodeStack,
			SymbolTableStack symbolTableStack
			) throws CheckerException {


		var arrayType = typeExpressions.get(syntaxNodeStack.getLast().get(0));

		if(!(arrayType instanceof ArrayType))
			throw new CheckerException("Variable cannot be indexed",
					syntaxNodeStack.getLast().get(0));

		var indexType = typeExpressions.get(syntaxNodeStack.getLast().get(2));

		if(!indexType.equals(SimpleType.INTEGER))
			throw new CheckerException("Index must be INTEGER",
					syntaxNodeStack.getLast().get(2));

		// 要素の型を伝搬させる
		typeExpressions.put(syntaxNodeStack.getLast(), ((ArrayType)arrayType).contentType);
	}

}
