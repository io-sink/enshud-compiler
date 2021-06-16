package enshud.s2.parser.parsers;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import enshud.s1.lexer.TokenStruct;
import enshud.s2.parser.ParserException;
import enshud.syntaxtree.AbstractSyntaxNode;
import enshud.syntaxtree.NonterminalNode;

public class ZeroOrMoreParser extends AbstractNonterminalParser {

	public ZeroOrMoreParser(List<ArrayList<IParser>> rule) {
		variableName = ".ZeroOrMore";
		generativeRule = rule;

		// First集合の問い合わせを受けるために，繰り返しなしの生成規則を持っておく(実際の生成規則とは異なる)
		// そのまま終了してもよい
		generativeRule.add(new ArrayList<IParser>());
	}


	@Override
	public AbstractSyntaxNode parse(Queue<TokenStruct> tokenQueue) throws ParserException {

		// スーパークラスとは異なる方法でパースを行う．
		// 左右の結合性を保つためにはこの方法しかない
		AbstractSyntaxNode syntaxNode = new NonterminalNode(variableName);

		if (parseRule == null)
			first();

		for(;;) {
			if (tokenQueue.isEmpty())
				throw new RuntimeException("tokenQueue must not be empty");

			TokenStruct peekToken = tokenQueue.peek();

			if (peekToken.type != EPSILON && parseRule.containsKey(peekToken.type)) {
				for (IParser parser : parseRule.get(peekToken.type))
					syntaxNode.add(parser.parse(tokenQueue));

			} else
				break;
		}

		return syntaxNode;
	}

}
