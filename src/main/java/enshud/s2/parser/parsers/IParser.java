package enshud.s2.parser.parsers;

import java.util.Queue;
import java.util.Set;

import enshud.s1.lexer.TokenStruct;
import enshud.s2.parser.ParserException;
import enshud.syntaxtree.AbstractSyntaxNode;

public interface IParser {

	public static final int EPSILON = TokenStruct.TOKEN_NOTFOUND;

	public AbstractSyntaxNode parse(Queue<TokenStruct> tokenQueue) throws ParserException;
	public Set<Integer> first();

}
