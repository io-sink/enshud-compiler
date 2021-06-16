package enshud.s1.lexer;

public class LexerSyntaxException extends Exception {
	public String Message;
	public int lineNumber;
	public int position;

	public LexerSyntaxException(String Message) {
		this.Message = Message;
	}

	public LexerSyntaxException(String Message, int lineNumber, int position) {
		this.Message = Message;
		this.lineNumber = lineNumber;
		this.position = position;
	}
}
