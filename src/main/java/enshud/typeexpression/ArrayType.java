package enshud.typeexpression;

import enshud.s3.checker.CheckerException;
import enshud.syntaxtree.AbstractSyntaxNode;

public class ArrayType extends AbstractType {
	public SimpleType contentType;
	public int startIndex;
	public int endIndex;

	public ArrayType(SimpleType contentType, int startIndex, int endIndex) {
		this.contentType = contentType;
		this.startIndex = startIndex;
		this.endIndex = endIndex;
	}

	public ArrayType(AbstractSyntaxNode node) {
		if(!node.variableName.equals("ArrayType"))
			throw new RuntimeException("");

		this.startIndex = SimpleType.parseSignedInteger(node.get(2).get(0));
		this.endIndex = SimpleType.parseSignedInteger(node.get(4).get(0));

		if(this.endIndex - this.startIndex <= 0)
			throw new CheckerException("Invalid range", node.get(2));

		this.contentType = new SimpleType(node.get(7));
	}


	@Override
	public boolean equals(AbstractType obj) {
		if(obj instanceof ArrayType)
			return contentType.equals(((ArrayType)obj).contentType) &&
					endIndex - startIndex == ((ArrayType)obj).endIndex - ((ArrayType)obj).startIndex;
		else
			return false;
	}

	@Override
	public String toString() {
		return String.format("%s[%d..%d]", contentType.toString(), startIndex, endIndex);
	}
}
