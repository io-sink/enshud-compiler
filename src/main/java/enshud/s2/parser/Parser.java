package enshud.s2.parser;

import java.io.IOException;
import java.util.Queue;

import enshud.minipascal.SyntaxSetting;
import enshud.minipascal.TokenSetting;
import enshud.s1.lexer.TokenStruct;
import enshud.s2.parser.parsers.IParser;
import enshud.s2.parser.parsers.NonterminalParser;
import enshud.syntaxtree.AbstractSyntaxNode;

public class Parser {

	/**
	 * サンプルmainメソッド．
	 * 単体テストの対象ではないので自由に改変しても良い．
	 */
	public static void main(final String[] args) {
		// normalの確認
		//new Parser().run("data/ts/normal01.ts");
		//new Parser().run("data/ts/normal02.ts");

		// synerrの確認
		//new Parser().run("data/ts/synerr01.ts");
		//new Parser().run("data/ts/synerr02.ts");

	}


	/**
	 * TODO
	 *
	 * 開発対象となるParser実行メソッド．
	 * 以下の仕様を満たすこと．
	 *
	 * 仕様:
	 * 第一引数で指定されたtsファイルを読み込み，構文解析を行う．
	 * 構文が正しい場合は標準出力に"OK"を，正しくない場合は"Syntax error: line"という文字列とともに，
	 * 最初のエラーを見つけた行の番号を標準エラーに出力すること （例: "Syntax error: line 1"）．
	 * 入力ファイル内に複数のエラーが含まれる場合は，最初に見つけたエラーのみを出力すること．
	 * 入力ファイルが見つからない場合は標準エラーに"File not found"と出力して終了すること．
	 *
	 * @param inputFileName 入力tsファイル名
	 */
	public AbstractSyntaxNode parse(final String inputFileName) {

		// ファイルを読み込む
		Queue<TokenStruct> tokens = null;

		try {
			tokens = TokenStruct.readFromFile(inputFileName);
		} catch (IOException e) {
			System.err.println("File not found");
			return null;
		}

		// 文法設定を読み込む
		try {
			TokenSetting.initialize("conf/tokens.txt");
			SyntaxSetting.initialize("conf/smallpascal_rev.txt");
		} catch (IOException e) {
			System.err.println("Configuration file not found");
			return null;
		}

		AbstractSyntaxNode syntaxParent;
		try {
			syntaxParent = (new NonterminalParser(SyntaxSetting.initialVariable)).parse(tokens);

			if(tokens.peek().type != IParser.EPSILON)
				throw new ParserException("Unexpected token", tokens.peek());

		} catch (ParserException e) {
			System.err.println(String.format("Syntax error: line %d", e.token.lineNumber));
			// System.err.println(String.format("%s: \"%s\"", e.message, e.token.content));
			return null;
		}

		syntaxParent.flat();
		syntaxParent.renumber();
		return syntaxParent;
	}

	public void run(final String inputFileName) {
		parse(inputFileName);
		System.out.println("OK");
	}
}
