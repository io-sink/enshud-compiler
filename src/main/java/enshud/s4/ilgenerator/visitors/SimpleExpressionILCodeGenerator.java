package enshud.s4.ilgenerator.visitors;

import java.util.HashMap;
import java.util.Map;

import enshud.interlanguage.iloperand.ILSimpleVariableOperand;
import enshud.interlanguage.ilstatement.ILAssign1Statement;
import enshud.interlanguage.ilstatement.ILAssign2Statement;
import enshud.s3.checker.CheckerException;
import enshud.s3.checker.TypeExpressionMap;
import enshud.s4.ilgenerator.ILCodeBlock;
import enshud.s4.ilgenerator.TempVariablePool;
import enshud.symboltable.SymbolTable;
import enshud.symboltable.SymbolTableStack;
import enshud.syntaxtree.AbstractSyntaxNode;
import enshud.syntaxtree.AbstractVisitor;
import enshud.syntaxtree.SyntaxNodeStack;
import enshud.typeexpression.SimpleType;

public class SimpleExpressionILCodeGenerator extends AbstractVisitor {

	private HashMap<AbstractSyntaxNode, String> variableMap;
	private HashMap<AbstractSyntaxNode, ILCodeBlock> ilCodeBlocks;
	private TempVariablePool tempVariablePool;

	public SimpleExpressionILCodeGenerator(
			HashMap<AbstractSyntaxNode, String> variableMap,
			HashMap<AbstractSyntaxNode, ILCodeBlock> ilCodeBlocks,
			TempVariablePool tempVariablePool) {

		this.variableMap = variableMap;
		this.ilCodeBlocks = ilCodeBlocks;
		this.tempVariablePool = tempVariablePool;
	}

	@Override
	public void postorder(
			TypeExpressionMap typeExpressions,
			Map<AbstractSyntaxNode, SymbolTable> symbolTables,

			SyntaxNodeStack syntaxNodeStack,
			SymbolTableStack symbolTableStack
			) throws CheckerException {

		// 左結合で3番地コードを生成
		// 直近のILコードブロックを取得
		var ilCodeBlock = ilCodeBlocks.get(syntaxNodeStack.getLastVariable("CompoundStatements"));

		String accumVariableName;
		String newTempVariableName;
		String newILStatement;

		int firstOperatorIndex = syntaxNodeStack.getLast().size() % 2;
		if(firstOperatorIndex == 1) {
			// 先頭に符号なし
			accumVariableName = variableMap.get(syntaxNodeStack.getLast().get(0));

		} else {
			// 先頭に符号あり
			accumVariableName = variableMap.get(syntaxNodeStack.getLast().get(1));

			if(syntaxNodeStack.getLast().get(0).get(0).variableName.equals("SMINUS")) {
				// オペランドが一時変数なら解放
				tempVariablePool.free(accumVariableName);
				// 新しい一時変数を取得
				newTempVariableName = tempVariablePool.get();

				ilCodeBlock.add(new ILAssign1Statement(
						new ILSimpleVariableOperand(newTempVariableName),
						ILAssign1Statement.OperationType.MINUS,
						new ILSimpleVariableOperand(accumVariableName),
						SimpleType.INTEGER
						));

				accumVariableName = newTempVariableName;
			}

			firstOperatorIndex += 2;
		}

		// 左結合で順に型をチェック
		for(int i = firstOperatorIndex; i < syntaxNodeStack.getLast().size(); i += 2) {
			String rightOperandName = variableMap.get(syntaxNodeStack.getLast().get(i + 1));

			switch (syntaxNodeStack.getLast().get(i).get(0).variableName) {
			case "SPLUS":

				// オペランドが一時変数なら解放
				tempVariablePool.free(rightOperandName);
				tempVariablePool.free(accumVariableName);
				// 新しい一時変数を取得
				newTempVariableName = tempVariablePool.get();

				ilCodeBlock.add(new ILAssign2Statement(
						new ILSimpleVariableOperand(newTempVariableName),
						ILAssign2Statement.OperationType.PLUS,
						new ILSimpleVariableOperand(accumVariableName),
						new ILSimpleVariableOperand(rightOperandName),
						SimpleType.INTEGER
						));

				accumVariableName = newTempVariableName;
				break;

			case "SMINUS":

				// オペランドが一時変数なら解放
				tempVariablePool.free(rightOperandName);
				tempVariablePool.free(accumVariableName);
				// 新しい一時変数を取得
				newTempVariableName = tempVariablePool.get();

				ilCodeBlock.add(new ILAssign2Statement(
						new ILSimpleVariableOperand(newTempVariableName),
						ILAssign2Statement.OperationType.MINUS,
						new ILSimpleVariableOperand(accumVariableName),
						new ILSimpleVariableOperand(rightOperandName),
						SimpleType.INTEGER
						));

				accumVariableName = newTempVariableName;
				break;

			case "SOR":

				// オペランドが一時変数なら解放
				tempVariablePool.free(rightOperandName);
				tempVariablePool.free(accumVariableName);
				// 新しい一時変数を取得
				newTempVariableName = tempVariablePool.get();

				ilCodeBlock.add(new ILAssign2Statement(
						new ILSimpleVariableOperand(newTempVariableName),
						ILAssign2Statement.OperationType.OR,
						new ILSimpleVariableOperand(accumVariableName),
						new ILSimpleVariableOperand(rightOperandName),
						SimpleType.BOOLEAN
						));

				accumVariableName = newTempVariableName;
				break;

			}

		}

		variableMap.put(syntaxNodeStack.getLast(), accumVariableName);
	}

}



