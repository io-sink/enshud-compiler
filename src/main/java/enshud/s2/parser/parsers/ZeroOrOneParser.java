package enshud.s2.parser.parsers;

import java.util.ArrayList;
import java.util.List;

public class ZeroOrOneParser extends AbstractNonterminalParser {

	public ZeroOrOneParser(List<ArrayList<IParser>> rule) {
		variableName = ".ZeroOrOne";
		generativeRule = rule;

		// そのまま終了してもよい
		generativeRule.add(new ArrayList<IParser>());
	}
}
