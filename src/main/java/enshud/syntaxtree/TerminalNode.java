package enshud.syntaxtree;

import enshud.minipascal.TokenSetting;
import enshud.s1.lexer.TokenStruct;

public class TerminalNode extends AbstractSyntaxNode {
	public TokenStruct token;

	public TerminalNode(TokenStruct token) {
		this.token = token;
		this.isTerminal = true;
		this.variableName = TokenSetting.getName(token.type);
		this.startLineNumber = token.lineNumber;
	}

}
