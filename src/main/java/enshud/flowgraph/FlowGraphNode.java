package enshud.flowgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import enshud.interlanguage.iloperand.AbstractILOperand;
import enshud.s4.ilgenerator.ILCodeBlock;
import enshud.symboltable.SymbolTableStack;

public class FlowGraphNode extends ILCodeBlock {

	public FlowGraphNode child;
	public FlowGraphNode parent;

	public HashSet<FlowGraphNode> conditionalParents;
	public HashSet<FlowGraphNode> conditionalChildren;

	public ArrayList<HashMap<AbstractILOperand, Integer>> allocativePlan;

	public FlowGraphNode(SymbolTableStack symbolTableStack) {
		super(symbolTableStack);

		this.child = null;
		this.parent = null;
		this.conditionalParents = new HashSet<FlowGraphNode>();
		this.conditionalChildren = new HashSet<FlowGraphNode>();
	}

	@Override
	public int hashCode() {
		return System.identityHashCode(this);
	}

	public void setChild(FlowGraphNode child) {
		if (this.child != null)
			throw new RuntimeException();
		this.child = child;
	}

	public void setParent(FlowGraphNode parent) {
		if (this.parent != null)
			throw new RuntimeException();
		this.parent = parent;
	}

	// フローグラフから自分を削除
	public void removeMyself() {
		if (parent != null)
			parent.child = child;

		if (child != null)
			child.parent = parent;

		for(var conditionalParent : conditionalParents) {
			conditionalParent.conditionalChildren.remove(this);

			if (child != null)
				conditionalParent.conditionalChildren.add(child);
			conditionalParent.conditionalChildren.addAll(conditionalChildren);
		}

		for(var conditionalChild : conditionalChildren) {
			conditionalChild.conditionalParents.remove(this);

			if (parent != null)
				conditionalChild.conditionalParents.add(parent);
			conditionalChild.conditionalParents.addAll(conditionalParents);
		}

	}

	@Override
	public String toString() {
		String res = String.format("[%d]\n", hashCode());
		res += String.format("parent: %s\n", parent == null ? null : parent.hashCode());
		res += String.format("child: %s\n", child == null ? null : child.hashCode());
		res += "conditionalParents: [";
		for (var conditionalParent : conditionalParents)
			res += String.format("%d, ", conditionalParent.hashCode());
		res += "]\n";
		res += "conditionalChildren: [";
		for (var conditionalChild : conditionalChildren)
			res += String.format("%d, ", conditionalChild.hashCode());
		res += "]\n";
		res += "---\n";
		for (var statement : this)
			res += statement + "\n";
		res += "---\n";
		return res;
	}

}
