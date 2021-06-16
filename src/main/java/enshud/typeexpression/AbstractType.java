package enshud.typeexpression;

import enshud.syntaxtree.AbstractSyntaxNode;

public abstract class AbstractType {

	public boolean equals(AbstractType obj) {
		return false;
	}

	public static AbstractType initialize(AbstractSyntaxNode node) {
		if(node.variableName.equals("SimpleType"))
			return new SimpleType(node);

		if(node.variableName.equals("ArrayType"))
			return new ArrayType(node);

		if(node.get(0).variableName.equals("SimpleType"))
			return new SimpleType(node.get(0));
		else
			return new ArrayType(node.get(0));
	}

}
