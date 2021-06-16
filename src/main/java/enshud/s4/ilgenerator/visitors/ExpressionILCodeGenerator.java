package enshud.s4.ilgenerator.visitors;

import java.util.HashMap;
import java.util.Map;

import enshud.interlanguage.iloperand.ILSimpleVariableOperand;
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

public class ExpressionILCodeGenerator extends AbstractVisitor {

	private HashMap<AbstractSyntaxNode, String> variableMap;
	private HashMap<AbstractSyntaxNode, ILCodeBlock> ilCodeBlocks;
	private TempVariablePool tempVariablePool;

	public ExpressionILCodeGenerator(
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

		// 直近のILコードブロックを取得
		var ilCodeBlock = ilCodeBlocks.get(syntaxNodeStack.getLastVariable("CompoundStatements"));

		String accumVariableName = variableMap.get(syntaxNodeStack.getLast().get(0));
		String newTempVariableName;

		// 左結合で3番地コードを生成
		for(int i = 1; i < syntaxNodeStack.getLast().size(); i += 2) {
			String rightOperandName = variableMap.get(syntaxNodeStack.getLast().get(i + 1));

			switch (syntaxNodeStack.getLast().get(i).get(0).variableName) {
			case "SEQUAL":

				// オペランドが一時変数なら解放
				tempVariablePool.free(rightOperandName);
				tempVariablePool.free(accumVariableName);
				// 新しい一時変数を取得
				newTempVariableName = tempVariablePool.get();

				ilCodeBlock.add(new ILAssign2Statement(
						new ILSimpleVariableOperand(newTempVariableName),
						ILAssign2Statement.OperationType.EQ,
						new ILSimpleVariableOperand(accumVariableName),
						new ILSimpleVariableOperand(rightOperandName),
						SimpleType.BOOLEAN
						));

				accumVariableName = newTempVariableName;
				break;

			case "SNOTEQUAL":

				// オペランドが一時変数なら解放
				tempVariablePool.free(rightOperandName);
				tempVariablePool.free(accumVariableName);
				// 新しい一時変数を取得
				newTempVariableName = tempVariablePool.get();

				ilCodeBlock.add(new ILAssign2Statement(
						new ILSimpleVariableOperand(newTempVariableName),
						ILAssign2Statement.OperationType.NEQ,
						new ILSimpleVariableOperand(accumVariableName),
						new ILSimpleVariableOperand(rightOperandName),
						SimpleType.BOOLEAN
						));

				accumVariableName = newTempVariableName;
				break;

			case "SLESS":

				// オペランドが一時変数なら解放
				tempVariablePool.free(rightOperandName);
				tempVariablePool.free(accumVariableName);
				// 新しい一時変数を取得
				newTempVariableName = tempVariablePool.get();

				ilCodeBlock.add(new ILAssign2Statement(
						new ILSimpleVariableOperand(newTempVariableName),
						ILAssign2Statement.OperationType.LS,
						new ILSimpleVariableOperand(accumVariableName),
						new ILSimpleVariableOperand(rightOperandName),
						SimpleType.BOOLEAN
						));

				accumVariableName = newTempVariableName;
				break;

			case "SLESSEQUAL":

				// オペランドが一時変数なら解放
				tempVariablePool.free(rightOperandName);
				tempVariablePool.free(accumVariableName);
				// 新しい一時変数を取得
				newTempVariableName = tempVariablePool.get();

				ilCodeBlock.add(new ILAssign2Statement(
						new ILSimpleVariableOperand(newTempVariableName),
						ILAssign2Statement.OperationType.LEQ,
						new ILSimpleVariableOperand(accumVariableName),
						new ILSimpleVariableOperand(rightOperandName),
						SimpleType.BOOLEAN
						));

				accumVariableName = newTempVariableName;
				break;

			case "SGREATEQUAL":

				// オペランドが一時変数なら解放
				tempVariablePool.free(rightOperandName);
				tempVariablePool.free(accumVariableName);
				// 新しい一時変数を取得
				newTempVariableName = tempVariablePool.get();

				ilCodeBlock.add(new ILAssign2Statement(
						new ILSimpleVariableOperand(newTempVariableName),
						ILAssign2Statement.OperationType.GEQ,
						new ILSimpleVariableOperand(accumVariableName),
						new ILSimpleVariableOperand(rightOperandName),
						SimpleType.BOOLEAN
						));

				accumVariableName = newTempVariableName;
				break;

			case "SGREAT":

				// オペランドが一時変数なら解放
				tempVariablePool.free(rightOperandName);
				tempVariablePool.free(accumVariableName);
				// 新しい一時変数を取得
				newTempVariableName = tempVariablePool.get();

				ilCodeBlock.add(new ILAssign2Statement(
						new ILSimpleVariableOperand(newTempVariableName),
						ILAssign2Statement.OperationType.GR,
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
