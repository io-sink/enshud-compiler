package enshud.interlanguage.ilstatement;

public class ILLabelDefinition extends AbstractILStatement {

	public String labelName;

	public ILLabelDefinition(String labelName) {
		this.labelName = labelName;
	}

	@Override
	public String toString() {
		return String.format("%s:", labelName);
	}

}
