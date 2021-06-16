package enshud.asm.instruction;

public class AsmComment implements IAsmInstruction {

	public String comment;
	public AsmComment(String comment) {
		this.comment = comment;
	}

	@Override
	public String toString() {
		return "; " + comment;
	}

}
