package enshud.s3.checker.semanticchecker;

import java.util.Map;

import enshud.s3.checker.CheckerException;
import enshud.s3.checker.TypeExpressionMap;
import enshud.symboltable.SymbolTable;
import enshud.symboltable.SymbolTableStack;
import enshud.syntaxtree.AbstractSyntaxNode;
import enshud.syntaxtree.AbstractVisitor;
import enshud.syntaxtree.SyntaxNodeStack;
import enshud.typeexpression.SimpleType;

public class AssignStatementChecker extends AbstractVisitor {

	@Override
	public void postorder(
			TypeExpressionMap typeExpressions,
			Map<AbstractSyntaxNode, SymbolTable> symbolTables,

			SyntaxNodeStack syntaxNodeStack,
			SymbolTableStack symbolTableStack
			) throws CheckerException {

		var leftType = typeExpressions.get(syntaxNodeStack.getLast().get(0));
		var rightType = typeExpressions.get(syntaxNodeStack.getLast().get(2));

		if(!(leftType instanceof SimpleType))
			throw new CheckerException("Can only assign to simple type", syntaxNodeStack.getLast().get(1));

		if(!leftType.equals(rightType))
			throw new CheckerException("Type mismatch", syntaxNodeStack.getLast().get(1));
	}

}
