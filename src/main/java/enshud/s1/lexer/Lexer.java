package enshud.s1.lexer;

import enshud.s1.lexer.lexer2.RegexLexer;

public class Lexer extends RegexLexer {

	public static void main(final String[] args) {
		// normalの確認
		new Lexer().run("mydata/compiler/test.pas", "mydata/compiler/test.ts");
	}

}
