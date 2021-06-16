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

public class ExpressionChecker extends AbstractVisitor {

	@Override
	public void postorder(
			TypeExpressionMap typeExpressions,
			Map<AbstractSyntaxNode, SymbolTable> symbolTables,

			SyntaxNodeStack syntaxNodeStack,
			SymbolTableStack symbolTableStack
			) throws CheckerException {

		var accumType = typeExpressions.get(syntaxNodeStack.getLast().get(0));

		// 左結合で順に型をチェック
		for(int i = 1; i < syntaxNodeStack.getLast().size(); i += 2) {
			var rightType = typeExpressions.get(syntaxNodeStack.getLast().get(i + 1));

			switch (syntaxNodeStack.getLast().get(i).get(0).variableName) {
			case "SEQUAL":
			case "SNOTEQUAL":
			case "SLESS":
			case "SLESSEQUAL":
			case "SGREATEQUAL":
			case "SGREAT":
				accumType = relOperatorChecker(accumType, rightType);
				break;

			default:
				throw new CheckerException("Unexpected Operator", syntaxNodeStack.getLast().get(i));
			}

			if(accumType == null)
				throw new CheckerException("Invalid operand type", syntaxNodeStack.getLast().get(i));
		}

		typeExpressions.put(syntaxNodeStack.getLast(), accumType);
	}

	private AbstractType relOperatorChecker(AbstractType accumType, AbstractType rightType) {
		if(!(accumType instanceof SimpleType) || !accumType.equals(rightType))
			return null;

		return SimpleType.BOOLEAN;
	}
}
