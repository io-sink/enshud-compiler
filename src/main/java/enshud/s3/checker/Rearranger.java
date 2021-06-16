package enshud.s3.checker;

import java.util.ArrayList;

import enshud.syntaxtree.AbstractSyntaxNode;
import enshud.syntaxtree.NonterminalNode;


public class Rearranger {

	// Pascal風言語をLL(1)文法に変換する際に弄った基本文周りの文法を元に戻す
	public void rearrange(AbstractSyntaxNode tree) {
		if(tree.variableName.equals("AssignOrProcedureCallStatement")) {
			var original = new ArrayList<AbstractSyntaxNode>(tree);

			if(original.get(1).get(0).variableName.equals("AssignSuccessor")) {
				// 代入文
				tree.variableName = "AssignStatement";
				tree.clear();

				AbstractSyntaxNode LeftHandSide, Variable, VariableAttribute, VariableName;

				tree.add(LeftHandSide = new NonterminalNode("LeftHandSide"));
				LeftHandSide.add(Variable = new NonterminalNode("Variable"));
				Variable.add(VariableAttribute = new NonterminalNode("SimpleVariable"));
				VariableAttribute.add(VariableName = original.get(0));
				VariableName.variableName = "VariableName";

				if(original.get(1).get(0).size() == 5) {
					// 左辺が添え字付き変数
					VariableAttribute.variableName = "IndexedVariable";
					VariableAttribute.add(original.get(1).get(0).get(0));
					VariableAttribute.add(original.get(1).get(0).get(1));
					VariableAttribute.add(original.get(1).get(0).get(2));
					tree.add(original.get(1).get(0).get(3));
					tree.add(original.get(1).get(0).get(4));
				} else {
					tree.add(original.get(1).get(0).get(0));
					tree.add(original.get(1).get(0).get(1));
				}

			} else {
				// 手続き呼び出し文
				tree.variableName = "ProcedureCallStatement";
				tree.clear();

				tree.add(original.get(0));
				tree.get(0).variableName = "ProcedureName";
				tree.addAll(original.get(1).get(0));
			}
		}

		if(tree.variableName.equals("Variable") && tree.size() == 4) {
			AbstractSyntaxNode indexedVariable = new NonterminalNode("IndexedVariable");
			for(var child : tree)
				indexedVariable.add(child);

			tree.clear();
			tree.add(indexedVariable);
		}

		if(tree.variableName.equals("Variable") && tree.get(0).variableName.equals("VariableName")) {
			AbstractSyntaxNode simpleVariable = new NonterminalNode("SimpleVariable");
			simpleVariable.add(tree.get(0));

			tree.clear();
			tree.add(simpleVariable);
		}


		for(var node : tree)
			rearrange(node);
	}

}
