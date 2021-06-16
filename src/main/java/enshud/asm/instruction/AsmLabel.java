package enshud.asm.instruction;

public class AsmLabel implements IAsmInstruction {

	public String labelName;

	public AsmLabel(String labelName) {
		this.labelName = labelName;
	}

	@Override
	public boolean equals(Object other) {
		if(!(other instanceof AsmLabel))
			return false;

		return ((AsmLabel)other).labelName.equals(this.labelName);
	}

	@Override
	public int hashCode() {
		return this.labelName.hashCode();
	}
}
