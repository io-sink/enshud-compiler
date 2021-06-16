package enshud.s1.lexer.lexer1.tokenfinder;

import enshud.s1.lexer.LexerSyntaxException;
import enshud.s1.lexer.TokenStruct;
import enshud.s1.lexer.lexer1.LexerSetting;

public class SeparatorFinder extends AbstractTokenFinder {

	@Override
	public TokenStruct findNext(String currentLine) throws LexerSyntaxException {
		if (currentLine == null ||
			currentLine.length() == 0)
			return null;

		char nextChar = currentLine.charAt(0);
		if(LexerSetting.SEPARATORS.indexOf(nextChar) == -1)
			return null;

		/*
		 * 区切り文字の後に区切り文字を挟まず他のトークンがあってもOK
		 * →トークンが独立であることは確認しなくてよい
		 */

		TokenStruct res = new TokenStruct(	0,
											0,
											currentLine.substring(0, 1),
											LexerSetting.TOKEN_IGNORE);
		return res;
	}


}
