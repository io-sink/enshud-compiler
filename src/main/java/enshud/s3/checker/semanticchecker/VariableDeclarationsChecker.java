package enshud.s3.checker.semanticchecker;

import java.util.ArrayList;
import java.util.Map;

import enshud.s3.checker.CheckerException;
import enshud.s3.checker.TypeExpressionMap;
import enshud.symboltable.SymbolTable;
import enshud.symboltable.SymbolTableStack;
import enshud.syntaxtree.AbstractSyntaxNode;
import enshud.syntaxtree.AbstractVisitor;
import enshud.syntaxtree.NonterminalNode;
import enshud.syntaxtree.SyntaxNodeStack;
import enshud.syntaxtree.TerminalNode;
import enshud.typeexpression.AbstractType;

public class VariableDeclarationsChecker extends AbstractVisitor {

	@Override
	public void preorder(
			TypeExpressionMap typeExpressions,
			Map<AbstractSyntaxNode, SymbolTable> symbolTables,

			SyntaxNodeStack syntaxNodeStack,
			SymbolTableStack symbolTableStack
			) throws CheckerException {

		var syntaxNode = syntaxNodeStack.getLast();
		boolean subProcedure =
				syntaxNode.variableName.equals("TempParameters") ||
				syntaxNodeStack.get(-3).variableName.equals("ProcedureDeclaration");

		var variableNameNodes = new ArrayList<AbstractSyntaxNode>();
		for(int i = 0; i < syntaxNode.size(); ++i) {
			if(i % 4 == 0) {
				// VariableNames
				variableNameNodes = new ArrayList<AbstractSyntaxNode>();
				AbstractSyntaxNode variableNamesNode = syntaxNode.get(i);

				for(AbstractSyntaxNode node : variableNamesNode)
					if(node instanceof NonterminalNode)
						variableNameNodes.add(node);

			} else if (i % 4 == 2) {
				// Type
				var TypeExpression = AbstractType.initialize(syntaxNode.get(i));

				for(var variableNameNode : variableNameNodes) {
					String variableName = ((TerminalNode)variableNameNode.get(0)).token.content;

					// すでに記号表に同名のエントリが存在する場合
					if(symbolTableStack.findVariableFromLastTable(variableName) != null)
						throw new CheckerException(
								String.format("'%s' is already declared", variableName),
								variableNameNode);


					if(syntaxNode.variableName.equals("TempParameters")) {

						symbolTableStack.getLast().put(
								variableName,
								new SymbolTable.SymbolTableEntry(variableName, SymbolTable.EntryType.TEMPPARAMETER, TypeExpression, subProcedure));
					} else {

						symbolTableStack.getLast().put(
								variableName,
								new SymbolTable.SymbolTableEntry(variableName, SymbolTable.EntryType.VARIABLE, TypeExpression, subProcedure));
					}

				}
			}

		}
	}
}
