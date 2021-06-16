package enshud.minipascal;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import enshud.Pair;
import enshud.s2.parser.parsers.IParser;
import enshud.s2.parser.parsers.NonterminalParser;
import enshud.s2.parser.parsers.TerminalParser;
import enshud.s2.parser.parsers.ZeroOrMoreParser;
import enshud.s2.parser.parsers.ZeroOrOneParser;

public class SyntaxSetting {

	public static String initialVariable;
	public static Map<String, ArrayList<ArrayList<IParser>>> generativeRule;

	// EBNFの文法をファイルから読み取ってルールを作成
	/*ただし，|は最も外側にある必要がある(括りだし後の文法)
		・if文
		・変数の添え字部分
		・基本文の部分
	 */
	public static void initialize(String syntaxFileName) throws IOException {
		generativeRule = new HashMap<String, ArrayList<ArrayList<IParser>>>();

		// 文法ファイルを読み込み
		var lines = new ArrayList<Pair<String, String>>();
		var inputFileReader = new BufferedReader(new FileReader(syntaxFileName));

		String line;
		while ((line = inputFileReader.readLine()) != null) {
			String[] splitted = line.split("[ \t]+::=|\\/\\/");

			if(splitted.length < 2)
				continue;

			lines.add(new Pair<String, String>(splitted[0], splitted[1]));
		}
		inputFileReader.close();


		initialVariable = lines.get(0).first;

		// 先にすべての記号を辞書に入れておく必要がある．
		for(var pair : lines)
			generativeRule.put(pair.first, new ArrayList<ArrayList<IParser>>());

		// パーサのルールを計算
		for(var pair : lines)
			// putで入れてはいけない(既にその変数の規則を参照しているオブジェクトがあるため，リストの参照を超えたらダメ)
			generativeRule.get(pair.first).addAll(ParseParsers(pair.second));
	}

	// 文字列で渡された規則をパーサの構造に変換
	private static ArrayList<ArrayList<IParser>> ParseParsers(String str) {
		var wordsPattern = Pattern.compile("\\[[^\\]]+\\]|\\{[^\\}]+\\}|\\$?\\w+|\\|");
		Matcher matcher = wordsPattern.matcher(str);

		var rule = new ArrayList<ArrayList<IParser>>();
		var generation = new ArrayList<IParser>();
		while(matcher.find()) {
			String word = matcher.group();

			if(word.equals("|")) {
				rule.add(generation);
				generation = new ArrayList<IParser>();

			} else if(word.charAt(0) == '[') {
				generation.add(new ZeroOrOneParser(ParseParsers(word.substring(1, word.length() - 1))));

			} else if(word.charAt(0) == '{') {
				generation.add(new ZeroOrMoreParser(ParseParsers(word.substring(1, word.length() - 1))));

			} else if(word.charAt(0) == '$') {
				// 非終端記号
				if(!generativeRule.keySet().contains(word.substring(1)))
					throw new RuntimeException("Undefined variable: " + word);
				generation.add(new NonterminalParser(word.substring(1)));

			} else {
				// 終端記号
				if(!TokenSetting.getTokenNameSet().contains(word))
					throw new RuntimeException("Undefined constant: " + word);
				generation.add(new TerminalParser(TokenSetting.getIDFromTokenName(word)));

			}
		}
		rule.add(generation);

		return rule;
	}

}
