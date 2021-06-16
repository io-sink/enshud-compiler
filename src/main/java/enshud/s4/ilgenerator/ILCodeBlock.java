package enshud.s4.ilgenerator;

import java.util.LinkedList;

import enshud.interlanguage.ilstatement.AbstractILStatement;
import enshud.symboltable.SymbolTableStack;

public class ILCodeBlock extends LinkedList<AbstractILStatement> {

	// 3番地コードの環境
	public SymbolTableStack symbolTableStack;

	public ILCodeBlock(SymbolTableStack symbolTableStack) {
		this.symbolTableStack = symbolTableStack;
	}

	@Override
	public String toString() {
		String res = "";
		for (var statement : this)
			res += statement.toString() + "\n";
		return res;
	}

}
