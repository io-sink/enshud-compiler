package enshud.s1.lexer.lexer1.tokenfinder;

import enshud.Pair;
import enshud.s1.lexer.LexerSyntaxException;
import enshud.s1.lexer.TokenStruct;
import enshud.s1.lexer.lexer1.LexerSetting;

public class SymbolFinder extends AbstractTokenFinder {

	@Override
	public TokenStruct findNext(String currentLine) throws LexerSyntaxException {
		if (currentLine == null)
			return null;

		for (Pair<String, Integer> pair : LexerSetting.symbols) {
			if (currentLine.length() < pair.first.length())
				continue;

			/*
			 * 記号の後に区切り文字を挟まず他のトークンがあってもOK
			 * →トークンが独立であることは確認しなくてよい
			 */

			if (currentLine.substring(0, pair.first.length()).equals(pair.first)) {
				TokenStruct res = new TokenStruct(
						0,
						0,
						currentLine.substring(0, pair.first.length()),
						pair.second);
				return res;
			}
		}

		return null;
	}

}
