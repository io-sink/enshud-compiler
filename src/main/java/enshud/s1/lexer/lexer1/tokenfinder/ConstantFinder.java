package enshud.s1.lexer.lexer1.tokenfinder;

import enshud.s1.lexer.LexerSyntaxException;
import enshud.s1.lexer.TokenStruct;
import enshud.s1.lexer.lexer1.LexerSetting;

public class ConstantFinder extends AbstractTokenFinder {

	@Override
	public TokenStruct findNext(String currentLine) throws LexerSyntaxException {
		if (currentLine == null ||
				currentLine.length() == 0)
			return null;

		int tokenLength;
		for (tokenLength = 0; tokenLength < currentLine.length()
				&& Character.isDigit(currentLine.charAt(tokenLength)); ++tokenLength);

		if(tokenLength == 0)
			return null;

		// 符号なし整数と仮定したとき，直後に名前，綴り記号，符号なし整数がないか確認
		if (!isTokenEnd(currentLine, tokenLength)) {
			// 先頭が一致したが独立したトークンでない場合
			return null;
		}

		TokenStruct res = new TokenStruct(
				0,
				0,
				currentLine.substring(0, tokenLength),
				LexerSetting.TOKEN_SCONSTANT);
		return res;
	}

}
