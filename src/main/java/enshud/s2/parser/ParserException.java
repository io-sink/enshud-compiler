package enshud.s2.parser;

import enshud.s1.lexer.TokenStruct;

public class ParserException extends Exception {
	public String message;
	public TokenStruct token;

	public ParserException(String message, TokenStruct token) {
		this.message = message;
		this.token = token;
	}
}
