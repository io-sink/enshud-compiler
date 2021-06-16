package enshud.s3.checker.semanticchecker;

import java.util.ArrayList;
import java.util.Map;

import enshud.s3.checker.CheckerException;
import enshud.s3.checker.TypeExpressionMap;
import enshud.symboltable.SymbolTable;
import enshud.symboltable.SymbolTableStack;
import enshud.syntaxtree.AbstractSyntaxNode;
import enshud.syntaxtree.AbstractVisitor;
import enshud.syntaxtree.SyntaxNodeStack;
import enshud.syntaxtree.TerminalNode;
import enshud.typeexpression.AbstractType;

public class ProcedureCallStatementChecker extends AbstractVisitor {

	@Override
	public void postorder(
			TypeExpressionMap typeExpressions,
			Map<AbstractSyntaxNode, SymbolTable> symbolTables,

			SyntaxNodeStack syntaxNodeStack,
			SymbolTableStack symbolTableStack
			) throws CheckerException {

		var syntaxNode = syntaxNodeStack.getLast();
		String procedureName = ((TerminalNode)syntaxNode.get(0).get(0)).token.content;

		var argTypes = new ArrayList<AbstractType>();
		if(syntaxNode.size() == 4)
			for(AbstractSyntaxNode expressionNode : syntaxNode.get(2))
				if(expressionNode.variableName.equals("Expression"))
					argTypes.add(typeExpressions.get(expressionNode));

		var procedureEntry = symbolTableStack.findProcedureFromAnyTable(procedureName, argTypes);
		if(procedureEntry == null)
			throw new CheckerException(
					String.format("Procedure '%s' that matches for the argument list is not declared", procedureName),
					syntaxNode);
	}


}
