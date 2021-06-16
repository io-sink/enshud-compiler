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

public class TermChecker extends AbstractVisitor {

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
			case "SSTAR":
				accumType = mulChecker(accumType, rightType);
				break;

			case "SDIVD":
				accumType = divChecker(accumType, rightType);
				break;

			case "SMOD":
				accumType = modChecker(accumType, rightType);
				break;

			case "SAND":
				accumType = andChecker(accumType, rightType);
				break;

			default:
				throw new CheckerException("Unexpected Operator", syntaxNodeStack.getLast().get(i));
			}

			if(accumType == null)
				throw new CheckerException("Invalid operand type", syntaxNodeStack.getLast().get(i));
		}

		typeExpressions.put(syntaxNodeStack.getLast(), accumType);
	}

	private AbstractType mulChecker(AbstractType accumType, AbstractType rightType) {
		if(!accumType.equals(SimpleType.INTEGER) || !rightType.equals(SimpleType.INTEGER))
			return null;
		return SimpleType.INTEGER;
	}

	private AbstractType divChecker(AbstractType accumType, AbstractType rightType) {
		if(!accumType.equals(SimpleType.INTEGER) || !rightType.equals(SimpleType.INTEGER))
			return null;
		return SimpleType.INTEGER;
	}

	private AbstractType modChecker(AbstractType accumType, AbstractType rightType) {
		if(!accumType.equals(SimpleType.INTEGER) || !rightType.equals(SimpleType.INTEGER))
			return null;
		return SimpleType.INTEGER;
	}

	private AbstractType andChecker(AbstractType accumType, AbstractType rightType) {
		if(!accumType.equals(SimpleType.BOOLEAN) || !rightType.equals(SimpleType.BOOLEAN))
			return null;
		return SimpleType.BOOLEAN;
	}
}
