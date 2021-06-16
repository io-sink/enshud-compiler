package enshud.s3.checker;

import enshud.syntaxtree.AbstractSyntaxNode;

public class CheckerException extends RuntimeException {
	public String message;
	public AbstractSyntaxNode node;

	public CheckerException(String message, AbstractSyntaxNode node) {
		this.message = message;
		this.node = node;
	}
}
