package enshud.s1.lexer.lexer2;

import java.util.ArrayList;

import enshud.Pair;

public class LexerSetting {

	public static final int TOKEN_IGNORE = -1;
	public static final int ANNOTATION_START = -2;
	public static final int ANNOTATION_END = -3;

	public static final ArrayList<Pair<String, Integer>> tokens = new ArrayList<Pair<String, Integer>>() {
		{
			add(new Pair<String, Integer>("and(?![a-zA-Z0-9])", 0));
			add(new Pair<String, Integer>("array(?![a-zA-Z0-9])", 1));
			add(new Pair<String, Integer>("begin(?![a-zA-Z0-9])", 2));
			add(new Pair<String, Integer>("boolean(?![a-zA-Z0-9])", 3));
			add(new Pair<String, Integer>("char(?![a-zA-Z0-9])", 4));
			add(new Pair<String, Integer>("div(?![a-zA-Z0-9])", 5));
			add(new Pair<String, Integer>("do(?![a-zA-Z0-9])", 6));
			add(new Pair<String, Integer>("else(?![a-zA-Z0-9])", 7));
			add(new Pair<String, Integer>("end(?![a-zA-Z0-9])", 8));
			add(new Pair<String, Integer>("false(?![a-zA-Z0-9])", 9));
			add(new Pair<String, Integer>("if(?![a-zA-Z0-9])", 10));
			add(new Pair<String, Integer>("integer(?![a-zA-Z0-9])", 11));
			add(new Pair<String, Integer>("mod(?![a-zA-Z0-9])", 12));
			add(new Pair<String, Integer>("not(?![a-zA-Z0-9])", 13));
			add(new Pair<String, Integer>("of(?![a-zA-Z0-9])", 14));
			add(new Pair<String, Integer>("or(?![a-zA-Z0-9])", 15));
			add(new Pair<String, Integer>("procedure(?![a-zA-Z0-9])", 16));
			add(new Pair<String, Integer>("program(?![a-zA-Z0-9])", 17));
			add(new Pair<String, Integer>("readln(?![a-zA-Z0-9])", 18));
			add(new Pair<String, Integer>("then(?![a-zA-Z0-9])", 19));
			add(new Pair<String, Integer>("true(?![a-zA-Z0-9])", 20));
			add(new Pair<String, Integer>("var(?![a-zA-Z0-9])", 21));
			add(new Pair<String, Integer>("while(?![a-zA-Z0-9])", 22));
			add(new Pair<String, Integer>("writeln(?![a-zA-Z0-9])", 23));
			add(new Pair<String, Integer>("\\<\\>", 25));
			add(new Pair<String, Integer>("\\<=", 27));
			add(new Pair<String, Integer>("\\>=", 28));
			add(new Pair<String, Integer>("\\.\\.", 39));
			add(new Pair<String, Integer>(":=", 40));
			add(new Pair<String, Integer>("\\<", 26));
			add(new Pair<String, Integer>("\\>", 29));
			add(new Pair<String, Integer>("\\+", 30));
			add(new Pair<String, Integer>("/", 5));
			add(new Pair<String, Integer>("=", 24));
			add(new Pair<String, Integer>("-", 31));
			add(new Pair<String, Integer>("\\*", 32));
			add(new Pair<String, Integer>("\\(", 33));
			add(new Pair<String, Integer>("\\)", 34));
			add(new Pair<String, Integer>("\\[", 35));
			add(new Pair<String, Integer>("\\]", 36));
			add(new Pair<String, Integer>(";", 37));
			add(new Pair<String, Integer>(":", 38));
			add(new Pair<String, Integer>(",", 41));
			add(new Pair<String, Integer>("\\.", 42));
			add(new Pair<String, Integer>("[a-zA-Z][\\da-zA-Z]*", 43));
			add(new Pair<String, Integer>("\\d+", 44));
			add(new Pair<String, Integer>("'[^']+'", 45));
			add(new Pair<String, Integer>("[\t ]+", TOKEN_IGNORE));
		}
	};

	public static final Pair<String, Integer> annotation = new Pair<String, Integer>("\\{[^\\}]*\\}", TOKEN_IGNORE);
	public static final Pair<String, Integer> annotationContent = new Pair<String, Integer>("[^\\}]*$", TOKEN_IGNORE);
	public static final Pair<String, Integer> annotationStart = new Pair<String, Integer>("\\{[^\\}]*$", ANNOTATION_START);
	public static final Pair<String, Integer> annotationEnd = new Pair<String, Integer>("[^\\}]*\\}", ANNOTATION_END);

	public static final char QUOTATION_MARK = '\'';
}
