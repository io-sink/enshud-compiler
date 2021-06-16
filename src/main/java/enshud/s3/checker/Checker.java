package enshud.s3.checker;

import java.util.HashMap;
import java.util.Map;

import enshud.s2.parser.Parser;
import enshud.s3.checker.semanticchecker.AssignStatementChecker;
import enshud.s3.checker.semanticchecker.ConstantChecker;
import enshud.s3.checker.semanticchecker.ExpressionChecker;
import enshud.s3.checker.semanticchecker.FactorChecker;
import enshud.s3.checker.semanticchecker.IndexedVariableChecker;
import enshud.s3.checker.semanticchecker.ProcedureCallStatementChecker;
import enshud.s3.checker.semanticchecker.ProcedureDeclarationChecker;
import enshud.s3.checker.semanticchecker.ProgramChecker;
import enshud.s3.checker.semanticchecker.SimpleExpressionChecker;
import enshud.s3.checker.semanticchecker.StatementChecker;
import enshud.s3.checker.semanticchecker.TermChecker;
import enshud.s3.checker.semanticchecker.TypePropagationChecker;
import enshud.s3.checker.semanticchecker.VariableDeclarationsChecker;
import enshud.s3.checker.semanticchecker.VariableNameChecker;
import enshud.symboltable.SymbolTable;
import enshud.syntaxtree.AbstractSyntaxNode;
import enshud.syntaxtree.AbstractVisitor;

public class Checker {

	/**
	 * サンプルmainメソッド．
	 * 単体テストの対象ではないので自由に改変しても良い．
	 */
	public static void main(final String[] args) {
		// normalの確認
		new Checker().run("mydata/checker/ts/semerr04.ts");
		//new Checker().run("data/ts/normal12.ts");
		//new Checker().run("data/ts/normal08.ts");

		// synerrの確認
		//new Checker().run("data/ts/synerr01.ts");
		//new Checker().run("data/ts/synerr02.ts");

		// semerrの確認
		//new Checker().run("data/ts/semerr01.ts");
		//new Checker().run("data/ts/semerr02.ts");
	}

	/**
	 * TODO
	 *
	 * 開発対象となるChecker実行メソッド．
	 * 以下の仕様を満たすこと．
	 *
	 * 仕様:
	 * 第一引数で指定されたtsファイルを読み込み，意味解析を行う．
	 * 意味的に正しい場合は標準出力に"OK"を，正しくない場合は"Semantic error: line"という文字列とともに，
	 * 最初のエラーを見つけた行の番号を標準エラーに出力すること （例: "Semantic error: line 6"）．
	 * また，構文的なエラーが含まれる場合もエラーメッセージを表示すること（例： "Syntax error: line 1"）．
	 * 入力ファイル内に複数のエラーが含まれる場合は，最初に見つけたエラーのみを出力すること．
	 * 入力ファイルが見つからない場合は標準エラーに"File not found"と出力して終了すること．
	 *
	 * @param inputFileName 入力tsファイル名
	 */

	public TypeExpressionMap typeExpressions;
	public Map<AbstractSyntaxNode, SymbolTable> symbolTables;
	public AbstractSyntaxNode syntaxTree;

	public boolean check(final String inputFileName) {

		syntaxTree = new Parser().parse(inputFileName);
		if (syntaxTree == null)
			return false;

		new Rearranger().rearrange(syntaxTree);
		syntaxTree.renumber();
		// tree.showSyntaxTree(0);

		typeExpressions = new TypeExpressionMap();
		symbolTables = new HashMap<AbstractSyntaxNode, SymbolTable>();

		// Checker集合の定義
		var checkers = new HashMap<String, AbstractVisitor>();

		// グローバルなスコープを表す新たな記号表をスタックに追加
		checkers.put("Program", 				new ProgramChecker());
		// 宣言された手続きを最新の記号表に登録，ローカルなスコープを表す新たな記号表をスタックに追加
		checkers.put("ProcedureDeclaration", 	new ProcedureDeclarationChecker());
		// 宣言された変数を最新の記号表に登録
		checkers.put("VariableDeclarations", 	new VariableDeclarationsChecker());
		// 仮パラメータを最新の記号表に登録
		checkers.put("TempParameters", 			new VariableDeclarationsChecker());

		// 定数の型を判定し，型を伝搬させる
		checkers.put("Constant", 				new ConstantChecker());
		// 変数が宣言済みかどうか確認し，型を伝搬させる
		checkers.put("VariableName", 			new VariableNameChecker());
		// 型を式木の根まで伝搬させる
		checkers.put("Variable", 				new TypePropagationChecker());
		checkers.put("SimpleVariable", 			new TypePropagationChecker());
		checkers.put("Index", 					new TypePropagationChecker());
		checkers.put("LeftHandSide", 			new TypePropagationChecker());
		checkers.put("Factor", 					new FactorChecker());
		checkers.put("Term", 					new TermChecker());
		checkers.put("SimpleExpression", 		new SimpleExpressionChecker());
		checkers.put("Expression", 				new ExpressionChecker());
		// 配列の要素の型を伝搬させる
		checkers.put("IndexedVariable", 		new IndexedVariableChecker());
		// 代入式の両辺で型が一致することをチェック
		checkers.put("AssignStatement", 		new AssignStatementChecker());
		// if, while文の条件式がbooleanであることをチェック
		checkers.put("Statement", 				new StatementChecker());
		// 引数の型と合致する手続きが宣言済みかどうか確認
		checkers.put("ProcedureCallStatement", 	new ProcedureCallStatementChecker());

		try {
			syntaxTree.dfs(checkers, typeExpressions, symbolTables);
			return true;

		} catch (CheckerException e) {
			System.err.println(String.format("Semantic error: line %d", e.node.startLineNumber));
			//System.err.println(e.message);
			//e.printStackTrace();
			return false;
		} catch (Exception e) {
			System.err.println("Unexpected error");
			return false;
		}
	}

	public void run(final String inputFileName) {
		if(check(inputFileName))
			System.out.println("OK");
	}


}
