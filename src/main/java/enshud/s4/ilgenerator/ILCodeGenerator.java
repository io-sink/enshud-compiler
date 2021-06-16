package enshud.s4.ilgenerator;

import java.util.HashMap;
import java.util.Map;

import enshud.s3.checker.TypeExpressionMap;
import enshud.s4.ilgenerator.visitors.AssignStatementILCodeGenerator;
import enshud.s4.ilgenerator.visitors.ConstantResolver;
import enshud.s4.ilgenerator.visitors.ExpressionILCodeGenerator;
import enshud.s4.ilgenerator.visitors.ExpressionsILCodeGenerator;
import enshud.s4.ilgenerator.visitors.FactorILCodeGenerator;
import enshud.s4.ilgenerator.visitors.ILCodeBlockInitializer;
import enshud.s4.ilgenerator.visitors.IOStatementILCodeGenerator;
import enshud.s4.ilgenerator.visitors.IndexedVariablePropagator;
import enshud.s4.ilgenerator.visitors.ProcedureCallStatementILCodeGenerator;
import enshud.s4.ilgenerator.visitors.SimpleExpressionILCodeGenerator;
import enshud.s4.ilgenerator.visitors.StatementILCodeGenerator;
import enshud.s4.ilgenerator.visitors.TermILCodeGenerator;
import enshud.s4.ilgenerator.visitors.VariableNamePropagator;
import enshud.s4.ilgenerator.visitors.VariableNameResolver;
import enshud.s4.ilgenerator.visitors.VariablesILCodeGenerator;
import enshud.symboltable.SymbolTable;
import enshud.syntaxtree.AbstractSyntaxNode;
import enshud.syntaxtree.AbstractVisitor;
import enshud.syntaxtree.TerminalNode;

public class ILCodeGenerator {

	private TypeExpressionMap typeExpressions;
	private Map<AbstractSyntaxNode, SymbolTable> symbolTables;

	// 一時変数，ラベルの生成器
	public TempVariablePool tempVariablePool;
	public LabelGenerator labelGenerator;


	// 手続き名 -> CompoundStatementノードの対応関係
	public HashMap<String, AbstractSyntaxNode> procedureNodeMap;
	// 複合文ノード -> 3番地コードの対応関係
	public HashMap<AbstractSyntaxNode, ILCodeBlock> nodeILCodeBlockMap;


	// 変数・定数・項 -> 一時変数名の対応関係
	// TODO 変数，定数を含めた表現にクラス化したい
	public HashMap<AbstractSyntaxNode, String> variableMap;

	public ILCodeGenerator(
			Map<AbstractSyntaxNode, SymbolTable> symbolTables,
			TypeExpressionMap typeExpressions
			) {

		this.typeExpressions = typeExpressions;
		this.symbolTables = symbolTables;

		this.nodeILCodeBlockMap = new HashMap<AbstractSyntaxNode, ILCodeBlock>();
		this.variableMap = new HashMap<AbstractSyntaxNode, String>();
		this.tempVariablePool = new TempVariablePool();
		this.labelGenerator = new LabelGenerator();
	}

	// TODO(最重要) 配列の添え字にある一時変数はいつ解放するか？...OK
	// TODO 式の計算順序はいいのか？帰りがけ順だと一時変数が多くなる
		// 手続きの引数の3番地コードparamは，「式の並び」のinorderで処理したらどうか．
	// TODO 式の最も上の一時変数が1つ無駄
	// TODO DAGへの対応(構文木の親が複数ある場合，一時変数の解放のタイミングを考える)
	// TODO 単項演算，二項演算の処理を共通化してコードクローンを除去
	// TODO if文，while文の3番地コードのステップ数を減らす

	// 手続き名 -> 3番地コードの基本ブロックのリスト対応関係を返す
	public void generate(AbstractSyntaxNode syntaxNode) {

		// visitor集合の初期化
		var visitors = new HashMap<String, AbstractVisitor>();

		// 3番地コードの新しいコードブロックを作る
		visitors.put("CompoundStatements", 		new ILCodeBlockInitializer(nodeILCodeBlockMap, tempVariablePool));

		// 構文木から定数を取得して伝搬させる
		visitors.put("Constant", 				new ConstantResolver(variableMap));
		// 構文木から変数名を取得して伝搬させる
		visitors.put("VariableName", 			new VariableNameResolver(variableMap));
		// 対応する変数を構文木の上へ伝搬させる
		visitors.put("SimpleVariable", 			new VariableNamePropagator(variableMap));
		visitors.put("Variable", 				new VariableNamePropagator(variableMap));
		visitors.put("LeftHandSide", 			new VariableNamePropagator(variableMap));
		visitors.put("Index", 					new VariableNamePropagator(variableMap));
		// 配列の変数名を伝搬
		visitors.put("IndexedVariable", 		new IndexedVariablePropagator(variableMap, nodeILCodeBlockMap, tempVariablePool));
		// 3番地コードを生成しながら変数を伝搬させる
		visitors.put("Factor", 					new FactorILCodeGenerator(variableMap, nodeILCodeBlockMap, tempVariablePool));
		visitors.put("Term", 					new TermILCodeGenerator(variableMap, nodeILCodeBlockMap, tempVariablePool));
		visitors.put("SimpleExpression",		new SimpleExpressionILCodeGenerator(variableMap, nodeILCodeBlockMap, tempVariablePool));
		visitors.put("Expression",				new ExpressionILCodeGenerator(variableMap, nodeILCodeBlockMap, tempVariablePool));
		// 3番地コードを生成
		visitors.put("AssignStatement", 		new AssignStatementILCodeGenerator(variableMap, nodeILCodeBlockMap, tempVariablePool));
		visitors.put("Statement", 				new StatementILCodeGenerator(variableMap, nodeILCodeBlockMap, tempVariablePool, labelGenerator));
		visitors.put("ProcedureCallStatement", 	new ProcedureCallStatementILCodeGenerator(variableMap, nodeILCodeBlockMap, tempVariablePool));
		visitors.put("IOStatement", 			new IOStatementILCodeGenerator(variableMap, nodeILCodeBlockMap, tempVariablePool));
		visitors.put("Expressions", 			new ExpressionsILCodeGenerator(variableMap, nodeILCodeBlockMap, tempVariablePool));
		visitors.put("Variables", 				new VariablesILCodeGenerator(variableMap, nodeILCodeBlockMap, tempVariablePool));

		try {
			syntaxNode.dfs(visitors, typeExpressions, symbolTables);

		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		this.procedureNodeMap = new HashMap<String, AbstractSyntaxNode>();
		findProcedures(syntaxNode);

	}


	// 手続きから複合文への対応関係を作る
	private void findProcedures(AbstractSyntaxNode node) {
		if(node.variableName.equals("Program"))
			procedureNodeMap.put(".main", node.get(4));
		else if(node.variableName.equals("ProcedureDeclaration")) {
			String procedureName = ((TerminalNode)node.get(0).get(1).get(0)).token.content;
			procedureNodeMap.put(procedureName, node.get(2));
		}
		for (var child: node)
			findProcedures(child);
	}

}
