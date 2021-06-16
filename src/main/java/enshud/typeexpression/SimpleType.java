package enshud.typeexpression;

import enshud.minipascal.TokenSetting;
import enshud.s3.checker.CheckerException;
import enshud.syntaxtree.AbstractSyntaxNode;
import enshud.syntaxtree.TerminalNode;

public class SimpleType extends AbstractType {
	public static SimpleType INTEGER = new SimpleType(Type.INTEGER);
	public static SimpleType CHAR = new SimpleType(Type.CHAR);
	public static SimpleType BOOLEAN = new SimpleType(Type.BOOLEAN);

	private static enum Type {
		INTEGER, CHAR, BOOLEAN;
	}

	public Type type;

	private SimpleType(Type type) {
		this.type = type;
	}

	public SimpleType(AbstractSyntaxNode node) {
		if(!node.variableName.equals("SimpleType"))
			throw new RuntimeException("");

		int tokenType = ((TerminalNode)node.get(0)).token.type;
		if(tokenType == TokenSetting.getIDFromTokenName("SINTEGER"))
			this.type = Type.INTEGER;
		else if(tokenType == TokenSetting.getIDFromTokenName("SCHAR"))
			this.type = Type.CHAR;
		else /* SBOOLEAN */
			this.type = Type.BOOLEAN;
	}

	@Override
	public boolean equals(AbstractType obj) {
		if(obj instanceof SimpleType)
			return this.type == ((SimpleType)obj).type;
		else
			return false;
	}

	public static int parseSignedInteger(AbstractSyntaxNode node) {
		if(!node.variableName.equals("SignedInteger"))
			throw new RuntimeException("");

		boolean sign = false;
		int unsignedVal = 0;
		AbstractSyntaxNode unsignedNode;

		if(node.size() == 1)
			unsignedNode = node.get(0);
		else {
			sign = node.get(0).get(0).variableName.equals("SMINUS");
			unsignedNode = node.get(1);
		}

		try {
			unsignedVal = Integer.parseInt(((TerminalNode)unsignedNode).token.content);
		} catch (NumberFormatException e) {
			throw new CheckerException("Invalid integer", node);
		}

		if(unsignedVal < -(1 << 15) || unsignedVal >= (1 << 15)) {
			throw new CheckerException("Integer out of range", node);
		}

		return sign ? -unsignedVal : unsignedVal;
	}

	public static int parseUnsignedInteger(AbstractSyntaxNode node) {
		if(!node.variableName.equals("SCONSTANT"))
			throw new RuntimeException("");

		int val;
		try {
			val = Integer.parseInt(((TerminalNode)node).token.content);
		} catch (NumberFormatException e) {
			throw new CheckerException("Invalid integer", node);
		}

		if(val < 0 || val >= (1 << 15)) {
			throw new CheckerException("Integer out of range", node);
		}

		return val;
	}

	@Override
	public String toString() {
		return type.toString();
	}
}
