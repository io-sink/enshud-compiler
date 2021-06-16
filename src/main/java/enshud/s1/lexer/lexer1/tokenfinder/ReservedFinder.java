package enshud.s1.lexer.lexer1.tokenfinder;

import enshud.Pair;
import enshud.s1.lexer.LexerSyntaxException;
import enshud.s1.lexer.TokenStruct;
import enshud.s1.lexer.lexer1.LexerSetting;


public class ReservedFinder extends AbstractTokenFinder {

	@Override
	public TokenStruct findNext(String currentLine) throws LexerSyntaxException {
		if (currentLine == null)
			return null;

		for (Pair<String, Integer> pair : LexerSetting.reservedWords) {
			if (currentLine.length() < pair.first.length())
				continue;

			// 独立したトークンでない場合
			if (!isTokenEnd(currentLine, pair.first.length()))
				continue;

			if (currentLine.substring(0, pair.first.length()).equals(pair.first)) {
				TokenStruct res = new TokenStruct(0, 0,
						currentLine.substring(0, pair.first.length()),
						pair.second);
				return res;
			}
		}

		return null;
	}
}
