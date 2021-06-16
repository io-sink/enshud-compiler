package enshud.syntaxtree;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class SyntaxNodeStack implements Iterable<AbstractSyntaxNode> {
	private LinkedList<AbstractSyntaxNode> stack;
	private HashMap<String, LinkedList<AbstractSyntaxNode>> variableStacks;

	public SyntaxNodeStack() {
		stack = new LinkedList<AbstractSyntaxNode>();
		variableStacks = new HashMap<String, LinkedList<AbstractSyntaxNode>>();
	}

	public void push(AbstractSyntaxNode node) {
		stack.addLast(node);

		if(!variableStacks.containsKey(node.variableName))
			variableStacks.put(node.variableName, new LinkedList<AbstractSyntaxNode>());
		variableStacks.get(node.variableName).addLast(node);
	}

	public void addLast(AbstractSyntaxNode node) {
		push(node);
	}


	public AbstractSyntaxNode pop() {
		var last = stack.getLast();
		stack.removeLast();
		variableStacks.get(last.variableName).removeLast();
		return last;
	}

	public void removeLast() {
		pop();
	}

	public AbstractSyntaxNode getLastVariable(String variableName) {
		return variableStacks.get(variableName).getLast();
	}

	public AbstractSyntaxNode getLast() {
		return stack.getLast();
	}

	public AbstractSyntaxNode get(int index) {
		if(index >= 0)
			return stack.get(index);
		else
			return stack.get(stack.size() + index);
	}

	@Override
	public Iterator<AbstractSyntaxNode> iterator() {
		return stack.iterator();
	}

	public Iterator<AbstractSyntaxNode> descendingIterator() {
		return stack.descendingIterator();
	}

}
