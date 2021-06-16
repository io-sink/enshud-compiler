package enshud.s2.parser.parsers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import enshud.s1.lexer.TokenStruct;
import enshud.s2.parser.ParserException;
import enshud.syntaxtree.AbstractSyntaxNode;
import enshud.syntaxtree.NonterminalNode;

public class AbstractNonterminalParser implements IParser {
	public List<ArrayList<IParser>> generativeRule; // プログラマが事前に与える
	protected Map<Integer, ArrayList<IParser>> parseRule; // プログラムが生成する

	protected String variableName; // プログラマが事前に与える

	@Override
	public AbstractSyntaxNode parse(Queue<TokenStruct> tokenQueue) throws ParserException {
		AbstractSyntaxNode syntaxNode = new NonterminalNode(variableName);

		if (parseRule == null)
			first();

		if (tokenQueue.isEmpty())
			throw new RuntimeException("tokenQueue must not be empty");

		TokenStruct peekToken = tokenQueue.peek();

		if (parseRule.containsKey(peekToken.type)) {
			for (IParser parser : parseRule.get(peekToken.type)) {
				var childNode = parser.parse(tokenQueue);
				if(syntaxNode.size() == 0)
					syntaxNode.startLineNumber = childNode.startLineNumber;
				syntaxNode.add(childNode);
			}

		} else if (parseRule.containsKey(EPSILON)) {
			for (IParser parser : parseRule.get(EPSILON)) {
				var childNode = parser.parse(tokenQueue);
				if(syntaxNode.size() == 0)
					syntaxNode.startLineNumber = childNode.startLineNumber;
				syntaxNode.add(childNode);
			}

		} else if (peekToken.type == EPSILON)
			throw new ParserException("Unexpected EOF", peekToken);
		else {
			throw new ParserException("Unexpected token", peekToken);
		}

		return syntaxNode;
	}


	@Override
	public Set<Integer> first() {
		// parseRuleの生成をコンストラクタで行うと，継承関係でparseRuleの生成順がおかしくなるので．
		if (parseRule == null) {
			if (generativeRule == null)
				throw new RuntimeException("generativeRule is not defined");

			// parseRuleの生成
			parseRule = calculateParseRule(generativeRule);

			/*
			 try {
				parseRule = calculateParseRule(generativeRule);
			} catch(Exception ex) {
				System.out.println(((NonterminalParser)this).variableName);
				ex.printStackTrace();
			}
			*/
		}

		return parseRule.keySet();
	}

	// 生成規則から，FIRST集合に基づくパーサの処理規則を計算する
	protected final static Map<Integer, ArrayList<IParser>> calculateParseRule(
			List<ArrayList<IParser>> generativeRule) {

		Map<Integer, ArrayList<IParser>> res = new HashMap<Integer, ArrayList<IParser>>();

		generationLoop: for (ArrayList<IParser> parsers : generativeRule) {
			for (IParser parser : parsers) {
				Set<Integer> firstSet = parser.first();
				boolean conatinsEpsilon = firstSet.contains(EPSILON);

				for (int first : firstSet) {
					if (first == EPSILON)
						continue;

					if (res.containsKey(first))
						throw new RuntimeException("The grammar is not LL(1)");

					res.put(first, parsers);
				}

				if (!conatinsEpsilon)
					continue generationLoop;
			}

			if (res.containsKey(EPSILON))
				throw new RuntimeException("The grammar is not LL(1)");
			res.put(EPSILON, parsers);
		}

		return res;
	}

}
