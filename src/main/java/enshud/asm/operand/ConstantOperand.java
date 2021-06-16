package enshud.asm.operand;

public class ConstantOperand implements IAsmOperand {

	public int value;
	public String labelValue;

	public ConstantOperand(int value) {
		this.value = value;
	}

	public ConstantOperand(String labelValue) {
		this.labelValue = labelValue;
	}

	@Override
	public String toString() {
		if(labelValue != null)
			return labelValue;

		return Integer.toString(value);
	}
}
