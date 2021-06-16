package enshud.s2.parser.parsers;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import enshud.s1.lexer.TokenStruct;
import enshud.s2.parser.ParserException;
import enshud.syntaxtree.AbstractSyntaxNode;
import enshud.syntaxtree.TerminalNode;

public class TerminalParser implements IParser {
	private int tokenType;
	private Set<Integer> firstSet;

	public TerminalParser(int tokenType) {
		this.tokenType = tokenType;
	}

	@Override
	public final AbstractSyntaxNode parse(Queue<TokenStruct> tokenQueue) throws ParserException {

		TokenStruct peekToken = tokenQueue.peek();
		if(peekToken.type == EPSILON)
			throw new ParserException("Unexpected EOF", peekToken);

		if(peekToken.type != this.tokenType)
			throw new ParserException(String.format("Unexpected token (expected %d)", tokenType), peekToken);

		tokenQueue.remove();

		return new TerminalNode(peekToken);
	}

	@Override
	public final Set<Integer> first() {
		if(firstSet == null)
			firstSet = new HashSet<Integer>(){{
				add(tokenType);
			}};

		return firstSet;
	}
}
