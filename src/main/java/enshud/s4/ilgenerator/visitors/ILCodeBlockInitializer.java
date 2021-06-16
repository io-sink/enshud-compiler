package enshud.s4.ilgenerator.visitors;

import java.util.HashMap;
import java.util.Map;

import enshud.s3.checker.TypeExpressionMap;
import enshud.s4.ilgenerator.ILCodeBlock;
import enshud.s4.ilgenerator.TempVariablePool;
import enshud.symboltable.SymbolTable;
import enshud.symboltable.SymbolTableStack;
import enshud.syntaxtree.AbstractSyntaxNode;
import enshud.syntaxtree.AbstractVisitor;
import enshud.syntaxtree.SyntaxNodeStack;
import enshud.typeexpression.SimpleType;

public class ILCodeBlockInitializer extends AbstractVisitor {

	private HashMap<AbstractSyntaxNode, ILCodeBlock> ilCodeBlocks;
	private TempVariablePool tempVariablePool;

	public ILCodeBlockInitializer(
			HashMap<AbstractSyntaxNode, ILCodeBlock> ilCodeBlocks,
			TempVariablePool tempVariablePool
			) {

		this.ilCodeBlocks = ilCodeBlocks;
		this.tempVariablePool = tempVariablePool;
	}

	// 3番地コードの新しいコードブロックを作る
	public void preorder(
			TypeExpressionMap typeExpressions,
			Map<AbstractSyntaxNode, SymbolTable> symbolTables,

			SyntaxNodeStack syntaxNodeStack,
			SymbolTableStack symbolTableStack) {

		var currentSyntaxNode = syntaxNodeStack.getLast();
		var newCodeBlock = new ILCodeBlock(symbolTableStack.clone());

		ilCodeBlocks.put(currentSyntaxNode, newCodeBlock);
	}

	public void postorder(
			TypeExpressionMap typeExpressions,
			Map<AbstractSyntaxNode, SymbolTable> symbolTables,

			SyntaxNodeStack syntaxNodeStack,
			SymbolTableStack symbolTableStack) {

		tempVariablePool.reset();

		var parentSyntaxNode = syntaxNodeStack.get(-2);
		if (parentSyntaxNode.variableName.equals("Program") ||
				parentSyntaxNode.variableName.equals("ProcedureDeclaration")) {

			boolean subProcedure = parentSyntaxNode.variableName.equals("ProcedureDeclaration");

			// 使用したすべての仮変数を記号表に追加
			for (String tempVariableName : tempVariablePool.usedVariables) {
				symbolTableStack.getLast().put(
						tempVariableName,
						new SymbolTable.SymbolTableEntry(tempVariableName, SymbolTable.EntryType.TEMPVARIABLE, SimpleType.INTEGER, subProcedure));
			}
			tempVariablePool.usedVariables.clear();
		}
	}
}
