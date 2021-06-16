package enshud.s1.lexer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class TokenStruct {
	public static final int TOKEN_NOTFOUND = -100;

	public int lineNumber = 0;
	public int position = 0;
	public String content;
	public int type;

	public TokenStruct() {}
	public TokenStruct(int lineNumber,
						int position,
						String content,
						int type) {

		this.lineNumber = lineNumber;
		this.position = position;
		this.content = content;
		this.type = type;
	}


	public static Queue<TokenStruct> readFromFile(String fileName) throws IOException {
		Queue<TokenStruct> tokens = new LinkedList<TokenStruct>();
		BufferedReader inputFileReader = new BufferedReader(new FileReader(fileName));

		int maxLineNumber = 1;
		String line;
		while ((line = inputFileReader.readLine()) != null) {
			String[] columns = line.split("\t");
			if(columns.length < 4)
				continue;

			int lineNumber = Integer.parseInt(columns[columns.length - 1]);
			maxLineNumber = Math.max(maxLineNumber, lineNumber);
			TokenStruct token = new TokenStruct(
					lineNumber,
					0,
					String.join("\t", Arrays.copyOfRange(columns, 0, columns.length - 3)),
					Integer.parseInt(columns[columns.length - 2])
					);
			tokens.add(token);
		}

		inputFileReader.close();

		// 末尾に番兵
		tokens.add(new TokenStruct(maxLineNumber, 0, "", TOKEN_NOTFOUND));

		return tokens;
	}
}
