package enshud.interlanguage.ilstatement;

public class ILUnconditionalJump extends AbstractILStatement {

	public String labelName;

	public ILUnconditionalJump(String labelName) {
		this.labelName = labelName;
	}

	@Override
	public String toString() {
		return String.format("goto %s", labelName);
	}

}
