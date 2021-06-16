package enshud.s3.checker.semanticchecker;

import java.util.Map;

import enshud.s3.checker.CheckerException;
import enshud.s3.checker.TypeExpressionMap;
import enshud.symboltable.SymbolTable;
import enshud.symboltable.SymbolTableStack;
import enshud.syntaxtree.AbstractSyntaxNode;
import enshud.syntaxtree.AbstractVisitor;
import enshud.syntaxtree.SyntaxNodeStack;
import enshud.syntaxtree.TerminalNode;

public class ProgramChecker extends AbstractVisitor {

	@Override
	public void preorder(
			TypeExpressionMap typeExpressions,
			Map<AbstractSyntaxNode, SymbolTable> symbolTables,

			SyntaxNodeStack syntaxNodeStack,
			SymbolTableStack symbolTableStack
			) throws CheckerException {

		String ProgramName =
				((TerminalNode)syntaxNodeStack.getLast()
				.get(1).get(0)).token.content;

		// 新しいスコープとして記号表を作成し，プログラム名を追加
		symbolTables.put(syntaxNodeStack.getLast(), new SymbolTable(SymbolTable.TableType.PROGRAM));
		symbolTables.get(syntaxNodeStack.getLast()).put(
				ProgramName,
				new SymbolTable.SymbolTableEntry(ProgramName, SymbolTable.EntryType.PROGRAM, null, false));

	}

}
