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
import enshud.typeexpression.ProcedureType;

public class ProcedureDeclarationChecker extends AbstractVisitor {

	@Override
	public void preorder(
			TypeExpressionMap typeExpressions,
			Map<AbstractSyntaxNode, SymbolTable> symbolTables,

			SyntaxNodeStack syntaxNodeStack,
			SymbolTableStack symbolTableStack
			) throws CheckerException {

		String ProcedureName =
				((TerminalNode) syntaxNodeStack.getLast()
				.get(0).get(1).get(0)).token.content;

		// すでに記号表に同名のエントリが存在する場合
		if(symbolTableStack.findVariableFromLastTable(ProcedureName) != null)
			throw new CheckerException(
					String.format("'%s' is already declared", ProcedureName),
					syntaxNodeStack.getLast().get(0).get(1));

		// プロシージャ名を追加してから，新しいスコープとして記号表を作成
		var ProcedureType = new ProcedureType(syntaxNodeStack.getLast().get(0));

		symbolTableStack.getLast().put(
				ProcedureName,
				new SymbolTable.SymbolTableEntry(ProcedureName, SymbolTable.EntryType.PROCEDURE, ProcedureType, false));

		symbolTables.put(syntaxNodeStack.getLast(), new SymbolTable(SymbolTable.TableType.PROCEDURE));
	}

}
