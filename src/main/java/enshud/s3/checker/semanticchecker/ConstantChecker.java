package enshud.s3.checker.semanticchecker;

import java.util.Map;

import enshud.s3.checker.CheckerException;
import enshud.s3.checker.TypeExpressionMap;
import enshud.symboltable.SymbolTable;
import enshud.symboltable.SymbolTableStack;
import enshud.syntaxtree.AbstractSyntaxNode;
import enshud.syntaxtree.AbstractVisitor;
import enshud.syntaxtree.SyntaxNodeStack;
import enshud.syntaxtree.TerminalNode;
import enshud.typeexpression.AbstractType;
import enshud.typeexpression.ArrayType;
import enshud.typeexpression.SimpleType;

public class ConstantChecker extends AbstractVisitor {

	@Override
	public void postorder(
			TypeExpressionMap typeExpressions,
			Map<AbstractSyntaxNode, SymbolTable> symbolTables,

			SyntaxNodeStack syntaxNodeStack,
			SymbolTableStack symbolTableStack
			) throws CheckerException {

		var constantValueNode = (TerminalNode)syntaxNodeStack.getLast().get(0);
		AbstractType type = null;

		switch(constantValueNode.variableName) {
		case "SCONSTANT":
			type = SimpleType.INTEGER;
			// 値の範囲をチェック
			SimpleType.parseUnsignedInteger(constantValueNode);
			break;

		case "SFALSE":
		case "STRUE":
			type = SimpleType.BOOLEAN;
			break;

		case "SSTRING":
			int stringLength = constantValueNode.token.content.length();
			if(stringLength == 3) {
				// 文字列の中身が1文字
				type = SimpleType.CHAR;
			} else {
				type = new ArrayType(
						SimpleType.CHAR,
						0, stringLength - 1);
			}
			break;
		}

		typeExpressions.put(syntaxNodeStack.getLast(), type);
	}
}
