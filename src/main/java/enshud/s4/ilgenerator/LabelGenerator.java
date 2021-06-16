package enshud.s4.ilgenerator;

public class LabelGenerator {

	int n = 0;

	// 新しいラベルを作成
	public String get() {
		return "L" + ++n;
	}

}
