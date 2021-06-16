package enshud.s4.ilgenerator.visitors;

import java.util.HashMap;
import java.util.Map;

import enshud.interlanguage.iloperand.ILConstantOperand;
import enshud.interlanguage.iloperand.ILSimpleVariableOperand;
import enshud.interlanguage.ilstatement.ILAssign1Statement;
import enshud.interlanguage.ilstatement.ILCopyStatement;
import enshud.s3.checker.TypeExpressionMap;
import enshud.s4.ilgenerator.ILCodeBlock;
import enshud.s4.ilgenerator.TempVariablePool;
import enshud.symboltable.SymbolTable;
import enshud.symboltable.SymbolTableStack;
import enshud.syntaxtree.AbstractSyntaxNode;
import enshud.syntaxtree.AbstractVisitor;
import enshud.syntaxtree.SyntaxNodeStack;
import enshud.typeexpression.SimpleType;

public class FactorILCodeGenerator extends AbstractVisitor {

	private HashMap<AbstractSyntaxNode, String> variableMap;
	private HashMap<AbstractSyntaxNode, ILCodeBlock> ilCodeBlocks;
	private TempVariablePool tempVariablePool;

	public FactorILCodeGenerator(
			HashMap<AbstractSyntaxNode, String> variableMap,
			HashMap<AbstractSyntaxNode, ILCodeBlock> ilCodeBlocks,
			TempVariablePool tempVariablePool) {

		this.variableMap = variableMap;
		this.ilCodeBlocks = ilCodeBlocks;
		this.tempVariablePool = tempVariablePool;
	}


	// 3番地コードを生成しながら変数を伝搬させる
	public void postorder(
			TypeExpressionMap typeExpressions,
			Map<AbstractSyntaxNode, SymbolTable> symbolTables,

			SyntaxNodeStack syntaxNodeStack,
			SymbolTableStack symbolTableStack) {

		// 直近のILコードブロックを取得
		var ilCodeBlock = ilCodeBlocks.get(syntaxNodeStack.getLastVariable("CompoundStatements"));

		String firstVariable = syntaxNodeStack.getLast().get(0).variableName;

		String variableName;
		if(firstVariable.equals("Variable")) {
			variableName = variableMap.get(syntaxNodeStack.getLast().get(0));

		} else if(firstVariable.equals("Constant")) {

			String constantValue = variableMap.get(syntaxNodeStack.getLast().get(0));

			String newTempVariableName;
			if(constantValue.charAt(0) == '\'' && constantValue.length() > 3) {
				// 文字列の場合はそのまま伝搬させる
				newTempVariableName = constantValue;

			} else {
				newTempVariableName = tempVariablePool.get();
				ilCodeBlock.add(new ILCopyStatement(
						new ILSimpleVariableOperand(newTempVariableName),
						new ILConstantOperand(constantValue, typeExpressions.get(syntaxNodeStack.getLast().get(0))),
						typeExpressions.get(syntaxNodeStack.getLast().get(0))
						));
			}

			variableName = newTempVariableName;

		} else if(firstVariable.equals("SLPAREN")) {
			variableName = variableMap.get(syntaxNodeStack.getLast().get(1));

		} else /* SNOT $Expression */ {
			String operandName = variableMap.get(syntaxNodeStack.getLast().get(1));

			// オペランドが一時変数なら解放
			tempVariablePool.free(operandName);
			// 新しい一時変数を取得
			variableName = tempVariablePool.get();

			ilCodeBlock.add(new ILAssign1Statement(
					new ILSimpleVariableOperand(variableName),
					ILAssign1Statement.OperationType.NOT,
					new ILSimpleVariableOperand(operandName),
					SimpleType.BOOLEAN
					));

		}

		variableMap.put(syntaxNodeStack.getLast(), variableName);
	}
}
