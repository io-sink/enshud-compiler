package enshud.s4.ilgenerator;

import java.util.HashSet;
import java.util.Stack;

public class TempVariablePool {

	int n = 0;
	Stack<String> stack = new Stack<String>();
	HashSet<String> unfreedVariables = new HashSet<String>();

	public HashSet<String> usedVariables = new HashSet<String>();

	// 新しい一時変数をスタックから取り出す(なければ新しく作成)
	public String get() {
		if(stack.empty())
			stack.push(".t" + ++n);

		String newVariable = stack.pop();
		unfreedVariables.add(newVariable);
		usedVariables.add(newVariable);

		return newVariable;
	}

	// 一時変数なら解放
	public void free(String variableName) {
		if(variableName.length() > 2 && variableName.substring(0, 2).equals(".t") &&
				unfreedVariables.contains(variableName)) {
			stack.push(variableName);
			unfreedVariables.remove(variableName);
		}
	}

	public void reset() {
		if(unfreedVariables.size() > 0)
			throw new RuntimeException("some variables are unfree");

		n = 0;
		stack.clear();
	}


}
