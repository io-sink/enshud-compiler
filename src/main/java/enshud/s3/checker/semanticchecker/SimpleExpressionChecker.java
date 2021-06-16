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

public class SimpleExpressionChecker extends AbstractVisitor {

	@Override
	public void postorder(
			TypeExpressionMap typeExpressions,
			Map<AbstractSyntaxNode, SymbolTable> symbolTables,

			SyntaxNodeStack syntaxNodeStack,
			SymbolTableStack symbolTableStack
			) throws CheckerException {

		int firstOperatorIndex = syntaxNodeStack.getLast().size() % 2;

		AbstractType accumType;
		if(firstOperatorIndex == 1) {
			// 先頭に符号なし
			accumType = typeExpressions.get(syntaxNodeStack.getLast().get(0));

		} else {
			// 先頭に符号あり
			accumType = SimpleType.INTEGER;
			String operator = syntaxNodeStack.getLast().get(0).get(0).variableName;
			if(!operator.equals("SPLUS") && !operator.equals("SMINUS"))
				throw new CheckerException("Unexpected Operator", syntaxNodeStack.getLast().get(0));
		}

		// 左結合で順に型をチェック
		for(int i = firstOperatorIndex; i < syntaxNodeStack.getLast().size(); i += 2) {
			var rightType = typeExpressions.get(syntaxNodeStack.getLast().get(i + 1));

			switch (syntaxNodeStack.getLast().get(i).get(0).variableName) {
			case "SPLUS":
				accumType = addChecker(accumType, rightType);
				break;

			case "SMINUS":
				accumType = subChecker(accumType, rightType);
				break;

			case "SOR":
				accumType = orChecker(accumType, rightType);
				break;

			default:
				throw new CheckerException("Unexpected Operator", syntaxNodeStack.getLast().get(i));
			}

			if(accumType == null)
				throw new CheckerException("Invalid operand type", syntaxNodeStack.getLast().get(i));
		}

		typeExpressions.put(syntaxNodeStack.getLast(), accumType);
	}

	private AbstractType addChecker(AbstractType accumType, AbstractType rightType) {
		if(!accumType.equals(SimpleType.INTEGER) || !rightType.equals(SimpleType.INTEGER))
			return null;
		return SimpleType.INTEGER;
	}

	private AbstractType subChecker(AbstractType accumType, AbstractType rightType) {
		if(!accumType.equals(SimpleType.INTEGER) || !rightType.equals(SimpleType.INTEGER))
			return null;
		return SimpleType.INTEGER;
	}

	private AbstractType orChecker(AbstractType accumType, AbstractType rightType) {
		if(!accumType.equals(SimpleType.BOOLEAN) || !rightType.equals(SimpleType.BOOLEAN))
			return null;
		return SimpleType.BOOLEAN;
	}
}
