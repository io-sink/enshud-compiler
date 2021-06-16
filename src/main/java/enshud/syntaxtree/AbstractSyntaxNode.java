package enshud.syntaxtree;

import java.util.ArrayList;
import java.util.Map;

import enshud.s3.checker.TypeExpressionMap;
import enshud.symboltable.SymbolTable;
import enshud.symboltable.SymbolTableStack;

public abstract class AbstractSyntaxNode extends ArrayList<AbstractSyntaxNode> {
	public boolean isTerminal;
	public String variableName;
	public int startLineNumber;


	public void dfs(
			Map<String, AbstractVisitor> visitors,
			TypeExpressionMap typeExpressions,
			Map<AbstractSyntaxNode, SymbolTable> symbolTables,

			SyntaxNodeStack syntaxNodeStack,
			SymbolTableStack symbolTableStack
			) throws Exception {

		syntaxNodeStack.addLast(this);

		if (visitors.containsKey(variableName))
			visitors.get(variableName).preorder(
					typeExpressions,
					symbolTables,
					syntaxNodeStack,
					symbolTableStack);

		// Checkerに渡す記号表のスタックに新たな記号表を追加
		if (symbolTables.containsKey(syntaxNodeStack.getLast()))
			symbolTableStack.addLast(symbolTables.get(syntaxNodeStack.getLast()));

		for (int i = 0; i < this.size(); ++i) {

			if(i > 0 && visitors.containsKey(variableName))
				visitors.get(variableName).inorder(
						i,
						typeExpressions,
						symbolTables,
						syntaxNodeStack,
						symbolTableStack);

			this.get(i).dfs(
						visitors,
						typeExpressions,
						symbolTables,
						syntaxNodeStack,
						symbolTableStack);

		}

		if(visitors.containsKey(variableName))
			visitors.get(variableName).inorder(
					this.size(),
					typeExpressions,
					symbolTables,
					syntaxNodeStack,
					symbolTableStack);


		// Checkerに渡す記号表のスタックから記号票を削除
		if (symbolTables.containsKey(syntaxNodeStack.getLast()))
			symbolTableStack.removeLast();

		if (visitors.containsKey(variableName))
			visitors.get(variableName).postorder(
					typeExpressions,
					symbolTables,
					syntaxNodeStack,
					symbolTableStack);

		syntaxNodeStack.removeLast();
	}


	public void dfs(
			Map<String, AbstractVisitor> visitors,
			TypeExpressionMap typeExpressions,
			Map<AbstractSyntaxNode, SymbolTable> symbolTables
			) throws Exception {

		dfs(visitors, typeExpressions, symbolTables, new SyntaxNodeStack(), new SymbolTableStack());
	}

	@Override
	public int hashCode() {
		return System.identityHashCode(this);
	}

	// 仮に導入した文法記号を消去
	public void flat() {
		var original = new ArrayList<AbstractSyntaxNode>(this);
		this.clear();

		for(var child : original) {
			child.flat();

			if(child.variableName.equals(".ZeroOrOne") || child.variableName.equals(".ZeroOrMore"))
				this.addAll(child);
			else
				this.add(child);
		}
	}

	public void showSyntaxTree(int depth) {
		for (int i = 0; i < depth; ++i)
			System.out.print("  ");
		System.out.println(String.format("%s(%d) : line %d", this.variableName, this.hashCode(),  this.startLineNumber));

		for (var child : this)
			child.showSyntaxTree(depth + 1);
	}

	public void renumber() {
		for (var child : this)
			child.renumber();

		if(size() > 0)
			startLineNumber = get(0).startLineNumber;
	}

}
