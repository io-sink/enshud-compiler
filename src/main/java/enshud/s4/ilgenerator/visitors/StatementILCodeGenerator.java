package enshud.s4.ilgenerator.visitors;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import enshud.interlanguage.iloperand.ILSimpleVariableOperand;
import enshud.interlanguage.ilstatement.ILAssign1Statement;
import enshud.interlanguage.ilstatement.ILCodeBlockCall;
import enshud.interlanguage.ilstatement.ILConditionalJump;
import enshud.interlanguage.ilstatement.ILLabelDefinition;
import enshud.interlanguage.ilstatement.ILUnconditionalJump;
import enshud.s3.checker.CheckerException;
import enshud.s3.checker.TypeExpressionMap;
import enshud.s4.ilgenerator.ILCodeBlock;
import enshud.s4.ilgenerator.LabelGenerator;
import enshud.s4.ilgenerator.TempVariablePool;
import enshud.symboltable.SymbolTable;
import enshud.symboltable.SymbolTableStack;
import enshud.syntaxtree.AbstractSyntaxNode;
import enshud.syntaxtree.AbstractVisitor;
import enshud.syntaxtree.SyntaxNodeStack;
import enshud.typeexpression.SimpleType;

public class StatementILCodeGenerator extends AbstractVisitor {

	private enum StatementType {
		If, IfElse, While, Other
	}

	private HashMap<AbstractSyntaxNode, String> variableMap;
	private HashMap<AbstractSyntaxNode, ILCodeBlock> ilCodeBlocks;
	private TempVariablePool tempVariablePool;
	private LabelGenerator labelGenerator;

	// preorder～inorder～postorderの間でラベルを記憶しておくためのスタック
	private Stack<String> labelStack;

	public StatementILCodeGenerator(
			HashMap<AbstractSyntaxNode, String> variableMap,
			HashMap<AbstractSyntaxNode, ILCodeBlock> ilCodeBlocks,
			TempVariablePool tempVariablePool,
			LabelGenerator labelGenerator) {

		this.variableMap = variableMap;
		this.ilCodeBlocks = ilCodeBlocks;
		this.tempVariablePool = tempVariablePool;
		this.labelGenerator = labelGenerator;

		this.labelStack = new Stack<String>();
	}

	@Override
	public void preorder(
			TypeExpressionMap typeExpressions,
			Map<AbstractSyntaxNode, SymbolTable> symbolTables,

			SyntaxNodeStack syntaxNodeStack,
			SymbolTableStack symbolTableStack
			) throws CheckerException {

		StatementType statementType = getStatementType(syntaxNodeStack.getLast());
		if(statementType != StatementType.While)
			return;

		// 直近のILコードブロックを取得
		var ilCodeBlock = ilCodeBlocks.get(syntaxNodeStack.getLastVariable("CompoundStatements"));

		// Whileのcontinueラベル
		String continueLabel = labelGenerator.get();
		labelStack.push(continueLabel);
		ilCodeBlock.add(new ILLabelDefinition(continueLabel));


	}

	@Override
	public void inorder(
			int nodeIndex,
			TypeExpressionMap typeExpressions,
			Map<AbstractSyntaxNode, SymbolTable> symbolTables,

			SyntaxNodeStack syntaxNodeStack,
			SymbolTableStack symbolTableStack
			) throws CheckerException {

		if(nodeIndex == 3) {

			StatementType statementType = getStatementType(syntaxNodeStack.getLast());
			if(statementType == StatementType.Other)
				return;

			String conditionVariableName = variableMap.get(syntaxNodeStack.getLast().get(1));

			// 直近のILコードブロックを取得
			var ilCodeBlock = ilCodeBlocks.get(syntaxNodeStack.getLastVariable("CompoundStatements"));

			if(statementType == StatementType.If || statementType == StatementType.IfElse) {

				// 一時変数なら解放
				tempVariablePool.free(conditionVariableName);
				// 新しい一時変数を取得
				String notConditionName = tempVariablePool.get();
				ilCodeBlock.add(new ILAssign1Statement(
						new ILSimpleVariableOperand(notConditionName),
						ILAssign1Statement.OperationType.NOT,
						new ILSimpleVariableOperand(conditionVariableName),
						SimpleType.BOOLEAN
						));

				// 条件不成立時のジャンプ
				String newLabel = labelGenerator.get();
				labelStack.push(newLabel);
				// 一時変数なら解放
				tempVariablePool.free(notConditionName);
				ilCodeBlock.add(new ILConditionalJump(
						new ILSimpleVariableOperand(notConditionName),
						newLabel
						));

			} else if(statementType == StatementType.While) {

				// 一時変数なら解放
				tempVariablePool.free(conditionVariableName);
				// 新しい一時変数を取得
				String notConditionName = tempVariablePool.get();
				ilCodeBlock.add(new ILAssign1Statement(
						new ILSimpleVariableOperand(notConditionName),
						ILAssign1Statement.OperationType.NOT,
						new ILSimpleVariableOperand(conditionVariableName),
						SimpleType.BOOLEAN
						));

				// 条件不成立時のジャンプ
				String breakLabel = labelGenerator.get();
				labelStack.push(breakLabel);
				// 一時変数なら解放
				tempVariablePool.free(notConditionName);
				ilCodeBlock.add(new ILConditionalJump(
						new ILSimpleVariableOperand(notConditionName),
						breakLabel
						));

			}

			// if/while句のブロックを呼び出し
			ilCodeBlock.add(new ILCodeBlockCall(syntaxNodeStack.getLast().get(3)));

		} else if(nodeIndex == 5) {
			// IfElse


			// 直近のILコードブロックを取得
			var ilCodeBlock = ilCodeBlocks.get(syntaxNodeStack.getLastVariable("CompoundStatements"));

			String Label = labelStack.pop();
			String newLabel = labelGenerator.get();
			labelStack.push(newLabel);

			// ifの終わりへのジャンプ
			ilCodeBlock.add(new ILUnconditionalJump(newLabel));

			// else句のラベル
			ilCodeBlock.add(new ILLabelDefinition(Label));

			// else句のブロックを呼び出し
			ilCodeBlock.add(new ILCodeBlockCall(syntaxNodeStack.getLast().get(5)));
		}
	}

	@Override
	public void postorder(
			TypeExpressionMap typeExpressions,
			Map<AbstractSyntaxNode, SymbolTable> symbolTables,

			SyntaxNodeStack syntaxNodeStack,
			SymbolTableStack symbolTableStack
			) throws CheckerException {

		StatementType statementType = getStatementType(syntaxNodeStack.getLast());
		if(statementType == StatementType.Other)
			return;

		// 直近のILコードブロックを取得
		var ilCodeBlock = ilCodeBlocks.get(syntaxNodeStack.getLastVariable("CompoundStatements"));;

		if(statementType == StatementType.If || statementType == StatementType.IfElse) {

			// ifの終わりのラベル
			String Label = labelStack.pop();
			ilCodeBlock.add(new ILLabelDefinition(Label));

		} else if(statementType == StatementType.While) {

			String breakLabel = labelStack.pop();
			String continueLabel = labelStack.pop();

			// continueラベルへのジャンプ
			ilCodeBlock.add(new ILUnconditionalJump(continueLabel));

			// breakラベル
			ilCodeBlock.add(new ILLabelDefinition(breakLabel));
		}
	}

	private StatementType getStatementType(AbstractSyntaxNode currentNode) {

		String firstNodeName = currentNode.get(0).variableName;
		if(firstNodeName.equals("SWHILE"))
			return StatementType.While;
		else if(firstNodeName.equals("SIF"))
			if(currentNode.size() == 4)
				return StatementType.If;
			else
				return StatementType.IfElse;
		else
			return StatementType.Other;
	}
}
