package enshud.minipascal;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TokenSetting {

	private static Map<Integer, String> IDToName;
	private static Map<Integer, String> IDToToken;
	private static Map<String, Integer> TokenToID;
	private static Map<String, Integer> TokenNameToID;


	public static void initialize(String tokenFileName) throws IOException {
		IDToName = new HashMap<Integer, String>();
		IDToToken = new HashMap<Integer, String>();
		TokenToID = new HashMap<String, Integer>();
		TokenNameToID = new HashMap<String, Integer>();

		var inputFileReader = new BufferedReader(new FileReader(tokenFileName));

		String line;
		while ((line = inputFileReader.readLine()) != null) {
			String[] splitted = line.split("\\t");
			if(splitted.length < 2) continue;

			TokenSetting.add(
					Integer.parseInt(splitted[0]),
					splitted[1],
					splitted.length == 2 ? "" : splitted[2]);
		}
		inputFileReader.close();
	}

	public static void add(int tokenID, String tokenName, String token) {
		IDToName.put(tokenID, tokenName);
		IDToToken.put(tokenID, token);
		TokenNameToID.put(tokenName, tokenID);

		if(token != null)
			TokenToID.put(token, tokenID);
	}

	public static String getName(int tokenID) {
		return IDToName.get(tokenID);
	}

	public static String getToken(int tokenID) {
		return IDToToken.get(tokenID);
	}

	public static int getIDFromToken(String token) {
		return TokenToID.get(token);
	}

	public static int getIDFromTokenName(String tokenName) {
		return TokenNameToID.get(tokenName);
	}

	public static Set<String> getTokenNameSet() {
		return TokenNameToID.keySet();
	}

}
