package enshud.s1.lexer.lexer1.tokenfinder;
import enshud.s1.lexer.LexerSyntaxException;
import enshud.s1.lexer.TokenStruct;

public abstract class AbstractTokenFinder {
	/*
	 * 直後の文字列をトークンとして認識し，readerをシークして，トークンの情報を返す．
	 * 直後の文字列がトークンと認識できない場合はnullを返す．
	 */
	public TokenStruct findNext(String currentLine) throws LexerSyntaxException {
		throw new RuntimeException("findNext method must be overridden");
	}

	/*
	 * 文字列tokenStrの先頭length文字はトークンとして独立しているか？
	 * 先頭length文字がSymbolまたは文字列または注釈→無条件で独立しているのでこのメソッドは呼び出さない
	 * Symbol以外→次の文字が英数字以外であるか(<=>Symbol，文字列，注釈以外か)で判定可能
	 */
	public boolean isTokenEnd(String tokenStr, int length) {
		if(length >= tokenStr.length())
			return true;

		char nextChar = tokenStr.charAt(length);
		return !Character.isAlphabetic(nextChar) && !Character.isDigit(nextChar);
	}
}
