package enshud.s2.parser.parsers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import enshud.minipascal.SyntaxSetting;

public class NonterminalParser extends AbstractNonterminalParser {

	private static HashMap<String, Map<Integer, ArrayList<IParser>>> parseRuleMemo;

	public NonterminalParser(String variableName) {
		this.variableName = variableName;
		this.generativeRule = SyntaxSetting.generativeRule.get(variableName);

		parseRuleMemo = new HashMap<String, Map<Integer, ArrayList<IParser>>>();
	}

	@Override
	public Set<Integer> first() {
		if(parseRule == null && !parseRuleMemo.containsKey(this.variableName)) {
			super.first();
			parseRuleMemo.put(this.variableName, this.parseRule);
		}

		this.parseRule = parseRuleMemo.get(this.variableName);
		return parseRule.keySet();
	}
}
