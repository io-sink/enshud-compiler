package enshud.s1.lexer.lexer2;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import enshud.Pair;
import enshud.s1.lexer.TokenStruct;

public class TokenFinder {
	public boolean annotate(String strToken, boolean isInAnnotation, TokenStruct outResult) {

		outResult.type = TokenStruct.TOKEN_NOTFOUND;
		Matcher matcher = null;

		ArrayList<Pair<String, Integer>> patternMap = new ArrayList<Pair<String, Integer>>();
		if(isInAnnotation) {
			patternMap.add(LexerSetting.annotationContent);
			patternMap.add(LexerSetting.annotationEnd);
		} else {
			patternMap.addAll(LexerSetting.tokens);
			patternMap.add(LexerSetting.annotationStart);
			patternMap.add(LexerSetting.annotation);
		}

		for(Pair<String, Integer> pair : patternMap) {
			Pattern pattern = Pattern.compile(pair.first);
			matcher = pattern.matcher(strToken);

			// 文字列の先頭にマッチ
			if(matcher.find() && matcher.start() == 0) {
				outResult.type = pair.second;
				break;
			}
		}

		if(outResult.type == TokenStruct.TOKEN_NOTFOUND) {
			outResult = null;
			return isInAnnotation;
		}

		outResult.content = matcher.group();

		if(outResult.type == LexerSetting.ANNOTATION_START)
			return true;
		else if(outResult.type == LexerSetting.ANNOTATION_END)
			return false;
		return isInAnnotation;
	}
}
