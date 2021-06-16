package enshud.s3.checker.semanticchecker;

import java.util.Map;

import enshud.s3.checker.CheckerException;
import enshud.s3.checker.TypeExpressionMap;
import enshud.symboltable.SymbolTable;
import enshud.symboltable.SymbolTableStack;
import enshud.syntaxtree.AbstractSyntaxNode;
import enshud.syntaxtree.AbstractVisitor;
import enshud.syntaxtree.SyntaxNodeStack;
import enshud.typeexpression.AbstractType;
import enshud.typeexpression.SimpleType;

public class FactorChecker extends AbstractVisitor {

	@Override
	public void postorder(
			TypeExpressionMap typeExpressions,
			Map<AbstractSyntaxNode, SymbolTable> symbolTables,

			SyntaxNodeStack syntaxNodeStack,
			SymbolTableStack symbolTableStack
			) throws CheckerException {

		String firstVariable = syntaxNodeStack.getLast().get(0).variableName;
		AbstractType typeExpression;
		if(firstVariable.equals("Variable") || firstVariable.equals("Constant")) {
			typeExpression = typeExpressions.get(syntaxNodeStack.getLast().get(0));
			if(typeExpression == null)
				throw new RuntimeException("");

		} else if(firstVariable.equals("SLPAREN")) {
			typeExpression = typeExpressions.get(syntaxNodeStack.getLast().get(1));

		} else /* SNOT $Expression */ {
			typeExpression = typeExpressions.get(syntaxNodeStack.getLast().get(1));

			// 論理演算子notのオペランドの型をチェック
			if(!typeExpression.equals(SimpleType.BOOLEAN))
				throw new CheckerException("A operand of not operation must be boolean",
						syntaxNodeStack.getLast().get(1));
		}

		typeExpressions.put(syntaxNodeStack.getLast(), typeExpression);
	}
}
