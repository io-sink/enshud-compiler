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

public class StatementChecker extends AbstractVisitor {

	@Override
	public void postorder(
			TypeExpressionMap typeExpressions,
			Map<AbstractSyntaxNode, SymbolTable> symbolTables,

			SyntaxNodeStack syntaxNodeStack,
			SymbolTableStack symbolTableStack
			) throws CheckerException {

		String firstNodeName = syntaxNodeStack.getLast().get(0).variableName;

		if(firstNodeName.equals("SIF") || firstNodeName.equals("SWHILE")) {
			var conditionType = typeExpressions.get(syntaxNodeStack.getLast().get(1));

			if(conditionType == null)
				throw new RuntimeException("");

			if(!conditionType.equals(SimpleType.BOOLEAN))
				throw new CheckerException("Condition must be BOOLEAN", syntaxNodeStack.getLast().get(1));
		}

	}


}
