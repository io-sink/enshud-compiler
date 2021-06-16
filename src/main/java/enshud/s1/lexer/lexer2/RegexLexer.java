package enshud.s1.lexer.lexer2;

import java.util.ArrayList;

import enshud.s1.lexer.AbstractLexer;
import enshud.s1.lexer.LexerSyntaxException;
import enshud.s1.lexer.TokenStruct;

public class RegexLexer extends AbstractLexer {

	@Override
	protected ArrayList<TokenStruct> analyze(ArrayList<String> lines) throws LexerSyntaxException {
		ArrayList<TokenStruct> res = new ArrayList<TokenStruct>();
		TokenFinder tokenFinder = new TokenFinder();
		boolean isInAnnotation = false;

		for(int i = 0; i < lines.size(); ++i) {
			int pos = 0;
			while(pos < lines.get(i).length()) {
				TokenStruct token = new TokenStruct();
				isInAnnotation = tokenFinder.annotate(lines.get(i).substring(pos), isInAnnotation, token);

				if(token.type == TokenStruct.TOKEN_NOTFOUND) {
					if(lines.get(i).charAt(pos) == LexerSetting.QUOTATION_MARK)
						throw new LexerSyntaxException("Expected \"\'\" at the end of the string", i + 1, pos);
					else
						throw new LexerSyntaxException("Unexpected token", i + 1, pos);
				}

				// 無視しないトークン
				if(token.type >= 0) {
					token.lineNumber = i + 1;
					token.position = pos;
					res.add(token);
				}
				pos += token.content.length();
			}
		}

		if(isInAnnotation)
			throw new LexerSyntaxException("Expected \"}\" at the end of the annotation", lines.size(), lines.get(lines.size() - 1).length());

		return res;
	}
}
