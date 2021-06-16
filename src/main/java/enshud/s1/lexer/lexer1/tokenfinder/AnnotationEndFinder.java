package enshud.s1.lexer.lexer1.tokenfinder;

import enshud.s1.lexer.LexerSyntaxException;
import enshud.s1.lexer.TokenStruct;
import enshud.s1.lexer.lexer1.LexerSetting;

public class AnnotationEndFinder extends AbstractTokenFinder {

	@Override
	public TokenStruct findNext(String currentLine) throws LexerSyntaxException {
		if (currentLine == null ||
			currentLine.length() == 0)
			return null;

		int endPos = currentLine.indexOf(LexerSetting.ANNOTATION_ED, 1);

		/*
		 * 注釈の後に区切り文字を挟まず他のトークンがあってもOK
		 * →トークンが独立であることは確認しなくてよい
		 */

		TokenStruct res;

		// 括弧が閉じられていない
		if(endPos == -1) {
			res = new TokenStruct(	0,
									0,
									currentLine,
									LexerSetting.TOKEN_IGNORE);
		} else {
			res = new TokenStruct(	0,
									0,
									currentLine.substring(0, endPos + 1),
									LexerSetting.TOKEN_ANNOTATION_END);
		}

		return res;
	}

}
