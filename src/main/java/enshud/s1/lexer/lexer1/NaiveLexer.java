package enshud.s1.lexer.lexer1;

import java.util.ArrayList;

import enshud.s1.lexer.AbstractLexer;
import enshud.s1.lexer.LexerSyntaxException;
import enshud.s1.lexer.TokenStruct;
import enshud.s1.lexer.lexer1.tokenfinder.AbstractTokenFinder;
import enshud.s1.lexer.lexer1.tokenfinder.AnnotationEndFinder;
import enshud.s1.lexer.lexer1.tokenfinder.AnnotationFinder;
import enshud.s1.lexer.lexer1.tokenfinder.ConstantFinder;
import enshud.s1.lexer.lexer1.tokenfinder.IdentifierFinder;
import enshud.s1.lexer.lexer1.tokenfinder.ReservedFinder;
import enshud.s1.lexer.lexer1.tokenfinder.SeparatorFinder;
import enshud.s1.lexer.lexer1.tokenfinder.StringFinder;
import enshud.s1.lexer.lexer1.tokenfinder.SymbolFinder;

public class NaiveLexer extends AbstractLexer {

	protected ArrayList<TokenStruct> analyze(ArrayList<String> lines) throws LexerSyntaxException {
		ArrayList<TokenStruct> res = new ArrayList<TokenStruct>();

		boolean isInAnnotation = false;
		for(int i = 0; i < lines.size(); ++i) {
			int pos = 0;
			tokenloop: while(pos < lines.get(i).length()) {
				String currentLine = lines.get(i).substring(pos);

				// 順に呼び出すFinderのリスト
				ArrayList<AbstractTokenFinder> tokenFinders = new ArrayList<AbstractTokenFinder>();
				if(isInAnnotation) {
					tokenFinders.add(new AnnotationEndFinder());

				} else {
					tokenFinders.add(new SeparatorFinder());
					tokenFinders.add(new AnnotationFinder());
					tokenFinders.add(new StringFinder());
					tokenFinders.add(new ReservedFinder());
					tokenFinders.add(new SymbolFinder());
					tokenFinders.add(new IdentifierFinder());
					tokenFinders.add(new ConstantFinder());
				}

				for (AbstractTokenFinder finder : tokenFinders) {
					TokenStruct token;
					try {
						// Finderを呼び出して結果を得る
						token = finder.findNext(currentLine);
					} catch (LexerSyntaxException e) {
						e.lineNumber = i + 1;
						e.position = pos;
						throw e;
					}

					if (token != null) {
						token.lineNumber = i + 1;
						token.position = pos;

						// Finderにヒット→無視するべきものでなければリストに追加
						if(token.type >= 0)
							res.add(token);

						if(token.type == LexerSetting.TOKEN_ANNOTATION_START ||
								token.type == LexerSetting.TOKEN_ANNOTATION_END)
							isInAnnotation = !isInAnnotation;

						pos += token.content.length();
						continue tokenloop;
					}
				}

				if (i < lines.size() - 1)
					throw new LexerSyntaxException("Unexpected token", i + 1, pos);
				else
					// EOF
					break;
			}
		}

		if(isInAnnotation)
			throw new LexerSyntaxException("Expected \"}\" at the end of the annotation", lines.size(), lines.get(lines.size() - 1).length());

		return res;
	}
}
