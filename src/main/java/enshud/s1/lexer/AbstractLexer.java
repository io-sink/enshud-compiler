package enshud.s1.lexer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public abstract class AbstractLexer {
	// トークンID->トークン名の変換
	public static final String[] TOKEN_TYPE_NAME = {
			"SAND", "SARRAY", "SBEGIN", "SBOOLEAN", "SCHAR", "SDIVD", "SDO", "SELSE", "SEND", "SFALSE", "SIF",
			"SINTEGER", "SMOD", "SNOT", "SOF", "SOR", "SPROCEDURE", "SPROGRAM", "SREADLN", "STHEN", "STRUE",
			"SVAR", "SWHILE", "SWRITELN", "SEQUAL", "SNOTEQUAL", "SLESS", "SLESSEQUAL", "SGREATEQUAL", "SGREAT",
			"SPLUS", "SMINUS", "SSTAR", "SLPAREN", "SRPAREN", "SLBRACKET", "SRBRACKET", "SSEMICOLON", "SCOLON",
			"SRANGE", "SASSIGN", "SCOMMA", "SDOT", "SIDENTIFIER", "SCONSTANT", "SSTRING",
	};

	/**
	 * TODO
	 *
	 * 開発対象となるLexer実行メソッド．
	 * 以下の仕様を満たすこと．
	 *
	 * 仕様:
	 * 第一引数で指定されたpasファイルを読み込み，トークン列に分割する．
	 * トークン列は第二引数で指定されたtsファイルに書き出すこと．
	 * 正常に処理が終了した場合は標準出力に"OK"を，
	 * 入力ファイルが見つからない場合は標準エラーに"File not found"と出力して終了すること．
	 *
	 * @param inputFileName 入力pasファイル名
	 * @param outputFileName 出力tsファイル名
	 */
	public void run(final String inputFileName, final String outputFileName) {

		// ファイルから読み込んで各行をリストに格納．
		ArrayList<String> lines = null;
		try {
			lines = readLines(inputFileName);
		} catch (IOException e) {
			System.err.println("File not found");
			return;
		}

		// 各行をトークンに分解
		ArrayList<TokenStruct> res;
		try {
			res = analyze(lines);

		} catch (LexerSyntaxException e) {
			System.err.println(e.Message);
			System.err.println(String.format("line: %d, pos: %d", e.lineNumber, e.position));
			return;
		}

		// 結果をファイルに出力
		writeTSFile(outputFileName, res);

		System.out.println("OK");
	}

	public ArrayList<String> readLines(String fileName) throws IOException {
		ArrayList<String> lines = new ArrayList<String>();

		BufferedReader inputFileReader;

		inputFileReader = new BufferedReader(new FileReader(fileName));

		String line;
		while ((line = inputFileReader.readLine()) != null)
			lines.add(line);

		inputFileReader.close();
		return lines;
	}

	protected ArrayList<TokenStruct> analyze(ArrayList<String> lines) throws LexerSyntaxException {
		throw new RuntimeException("analyze method must be overridden");
	}

	public void writeTSFile(String outputFileName, ArrayList<TokenStruct> tokens) {
		try {
			PrintWriter writer = new PrintWriter(new File(outputFileName));
			for (TokenStruct token : tokens)
				writer.println(String.format("%s\t%s\t%d\t%d", token.content, TOKEN_TYPE_NAME[token.type],
						token.type, token.lineNumber));

			writer.close();

		} catch (FileNotFoundException e) {
			System.err.println("cannot write");
		}
	}

}
