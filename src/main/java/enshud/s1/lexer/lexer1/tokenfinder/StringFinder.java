package enshud.s1.lexer.lexer1.tokenfinder;

import enshud.s1.lexer.LexerSyntaxException;
import enshud.s1.lexer.TokenStruct;
import enshud.s1.lexer.lexer1.LexerSetting;

public class StringFinder extends AbstractTokenFinder {

	@Override
	public TokenStruct findNext(String currentLine) throws LexerSyntaxException {
		if (currentLine == null ||
			currentLine.length() == 0 ||
			currentLine.charAt(0) != LexerSetting.STRING_QUOTATION)
			return null;

		int endPos = currentLine.indexOf(LexerSetting.STRING_QUOTATION, 1);

		// クォーテーションが閉じられていない
		if(endPos == -1)
			throw (new LexerSyntaxException("Expected \"\'\" at the end of the string"));

		/*
		 * 文字列の後に区切り文字を挟まず他のトークンがあってもOK
		 * →トークンが独立であることは確認しなくてよい
		 */


		TokenStruct res = new TokenStruct(	0,
											0,
											currentLine.substring(0, endPos + 1),
											LexerSetting.TOKEN_SSTRING);
		return res;
	}

}
