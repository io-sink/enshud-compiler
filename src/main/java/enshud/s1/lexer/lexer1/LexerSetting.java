package enshud.s1.lexer.lexer1;

import java.util.ArrayList;

import enshud.Pair;

// 字句解析器の仕様をまとめたクラス
public class LexerSetting {

	// 各Finderが返すトークンID
	public static final int TOKEN_IGNORE = -1;
	public static final int TOKEN_SIDENTIFIER = 43;
	public static final int TOKEN_SCONSTANT = 44;
	public static final int TOKEN_SSTRING = 45;

	public static final int TOKEN_ANNOTATION_START = -2;
	public static final int TOKEN_ANNOTATION_END = -3;



	// 予約語->トークンIDの変換
	public static final ArrayList<Pair<String, Integer>> reservedWords = new ArrayList<Pair<String, Integer>>() {
		{
			add(new Pair<String, Integer>("and", 0));
			add(new Pair<String, Integer>("array", 1));
			add(new Pair<String, Integer>("begin", 2));
			add(new Pair<String, Integer>("boolean", 3));
			add(new Pair<String, Integer>("char", 4));
			add(new Pair<String, Integer>("div", 5));
			add(new Pair<String, Integer>("do", 6));
			add(new Pair<String, Integer>("else", 7));
			add(new Pair<String, Integer>("end", 8));
			add(new Pair<String, Integer>("false", 9));
			add(new Pair<String, Integer>("if", 10));
			add(new Pair<String, Integer>("integer", 11));
			add(new Pair<String, Integer>("mod", 12));
			add(new Pair<String, Integer>("not", 13));
			add(new Pair<String, Integer>("of", 14));
			add(new Pair<String, Integer>("or", 15));
			add(new Pair<String, Integer>("procedure", 16));
			add(new Pair<String, Integer>("program", 17));
			add(new Pair<String, Integer>("readln", 18));
			add(new Pair<String, Integer>("then", 19));
			add(new Pair<String, Integer>("true", 20));
			add(new Pair<String, Integer>("var", 21));
			add(new Pair<String, Integer>("while", 22));
			add(new Pair<String, Integer>("writeln", 23));
		}
	};

	// 記号->トークンIDの変換 (最長マッチのためにトークンの長い順にする)
	public static final ArrayList<Pair<String, Integer>> symbols = new ArrayList<Pair<String, Integer>>() {
		{
			add(new Pair<String, Integer>("<>", 25));
			add(new Pair<String, Integer>("<=", 27));
			add(new Pair<String, Integer>(">=", 28));
			add(new Pair<String, Integer>("..", 39));
			add(new Pair<String, Integer>(":=", 40));
			add(new Pair<String, Integer>("/", 5));
			add(new Pair<String, Integer>("=", 24));
			add(new Pair<String, Integer>("<", 26));
			add(new Pair<String, Integer>(">", 29));
			add(new Pair<String, Integer>("+", 30));
			add(new Pair<String, Integer>("-", 31));
			add(new Pair<String, Integer>("*", 32));
			add(new Pair<String, Integer>("(", 33));
			add(new Pair<String, Integer>(")", 34));
			add(new Pair<String, Integer>("[", 35));
			add(new Pair<String, Integer>("]", 36));
			add(new Pair<String, Integer>(";", 37));
			add(new Pair<String, Integer>(":", 38));
			add(new Pair<String, Integer>(",", 41));
			add(new Pair<String, Integer>(".", 42));
		}
	};

	public static final char ANNOTATION_ST = '{';
	public static final char ANNOTATION_ED = '}';
	public static final String SEPARATORS = " \t";
	public static final char STRING_QUOTATION = '\'';
}
