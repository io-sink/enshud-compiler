package enshud.s4.compiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import enshud.casl.CaslSimulator;
import enshud.flowgraph.FlowGraphProgram;
import enshud.s3.checker.Checker;
import enshud.s4.asmgenerator.AsmCodeGenerator;
import enshud.s4.flowglaph.FlowGraphGenerator;
import enshud.s4.ilgenerator.ILCodeGenerator;
import enshud.s4.optimizer.ControlFlowOptimizer;
import enshud.s4.optimizer.CopyStatementOptimizer;
import enshud.s4.optimizer.DataFlowOptimizer;
import enshud.s4.optimizer.JumpOptimizer;
import enshud.s4.optimizer.LabelReplacer;

public class Compiler {

	public static boolean allocatable = true;
	public static boolean optimizable = true;

	/**
	 * サンプルmainメソッド．
	 * 単体テストの対象ではないので自由に改変しても良い．
	 */
	public static void main(final String[] args) {
		// Compilerを実行してcasを生成する
		//new Compiler().run("data/ts/normal20.ts", "tmp/out.cas");
		//new Compiler().run("mydata/compiler/test.ts", "tmp/out.cas");
		//new Compiler().run("mydata/compiler/test.ts", "mydata/compiler/out_unallocated_optimized.cas");


		for(int i = 1; i <= 20; ++i)
			new Compiler().run(
					String.format("data/ts/normal%02d.ts", i),
					String.format("mydata/compiler/cas/allocated_optimized/out%02d.cas", i));



		for(int i = 1; i <= 20; ++i)
			CaslSimulator.run(
					String.format("mydata/compiler/cas/allocated_optimized/out%02d.cas", i),
					String.format("mydata/compiler/cas/allocated_optimized/out%02d.ans", i)
					);



		// 上記casを，CASLアセンブラ & COMETシミュレータで実行する
		//CaslSimulator.run("tmp/out.cas", "tmp/out.ans");
		//CaslSimulator.run("mydata/compiler/out_unallocated_optimized.cas", "mydata/compiler/out_unallocated_optimized.ans");
	}

	/**
	 * TODO
	 *
	 * 開発対象となるCompiler実行メソッド．
	 * 以下の仕様を満たすこと．
	 *
	 * 仕様:
	 * 第一引数で指定されたtsファイルを読み込み，CASL IIプログラムにコンパイルする．
	 * コンパイル結果のCASL IIプログラムは第二引数で指定されたcasファイルに書き出すこと．
	 * 構文的もしくは意味的なエラーを発見した場合は標準エラーにエラーメッセージを出力すること．
	 * （エラーメッセージの内容はChecker.run()の出力に準じるものとする．）
	 * 入力ファイルが見つからない場合は標準エラーに"File not found"と出力して終了すること．
	 *
	 * @param inputFileName 入力tsファイル名
	 * @param outputFileName 出力casファイル名
	 */

	public void run(final String inputFileName, final String outputFileName) {
		// レジスタ割り付けと最適化を行うかどうか設定
		Compiler.allocatable = true;
		Compiler.optimizable = true;

		var checker = new Checker();
		if(!checker.check(inputFileName))
			return;

		// 構文木の複合文の部分から3番地コードを生成
		var ilCodeGenerator = new ILCodeGenerator(
				checker.symbolTables,
				checker.typeExpressions);

		ilCodeGenerator.generate(checker.syntaxTree);

		// 3番地コードからフローグラフを生成，コンパイルに必要な全ての情報を構文木から取り出す
		var flowGraphGenerator = new FlowGraphGenerator(
				checker.symbolTables,
				checker.typeExpressions,
				ilCodeGenerator.procedureNodeMap,
				ilCodeGenerator.nodeILCodeBlockMap);
		flowGraphGenerator.generate();


		FlowGraphProgram optimizedILCode;
		optimizedILCode = flowGraphGenerator.flowGraphs;

		System.out.println();
		System.out.println("before optimization");
		System.out.println(optimizedILCode);

		if(Compiler.optimizable) {

			boolean optimized = true;
			for(int i = 0; i < 10 && optimized; ++i) {
				optimized = false;
				System.out.println(String.format("@@@ optimize set %d", i));

				// データフロー解析
				optimizedILCode.calculateDataFlow();

				System.out.println("@@@ data flow optimization");

				// データフロー最適化
				optimized |= (new DataFlowOptimizer()).optimize(optimizedILCode);
				System.out.println("@@@ copy statement optimization");
				optimized |= (new CopyStatementOptimizer()).optimize(optimizedILCode);

				System.out.println("@@@ control flow optimization");

				// 冗長な条件付きジャンプを除去
				optimized |= (new ControlFlowOptimizer()).optimize(optimizedILCode, flowGraphGenerator.labelMap);


				System.out.println();
				System.out.println("after optimization");
				System.out.println(optimizedILCode);
			}

			// ジャンプへのジャンプを除去
			(new JumpOptimizer()).optimize(optimizedILCode, flowGraphGenerator.labelMap);

		}


		// 各手続きの最後のブロックを計算
		optimizedILCode.calculateProcedureTail();


		// アセンブリコードを生成
		//AsmCodeGenerator.debugPrint = true;
		var asmCodeGenerator = new AsmCodeGenerator(
				optimizedILCode,
				ilCodeGenerator.labelGenerator);
		var asmProgram = asmCodeGenerator.generate();


		// 有効なラベルに置き換え
		(new LabelReplacer()).optimize(asmProgram);


		/*
		 * TODO
		 * 構文木をDAGにできないかな
		 */


		String output = asmProgram.toString();

		try{
			String line;
			var buffReader = new BufferedReader(new FileReader("data/cas/lib.cas"));

			while((line = buffReader.readLine()) != null)
				output += "\n" + line;
			buffReader.close();

		} catch(IOException e){
			System.out.println(e.getMessage());
		}

		try{
			File file = new File(outputFileName);
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write(output);
			fileWriter.close();

		} catch(IOException e){
			System.out.println(e.getMessage());
		}


	}
}
