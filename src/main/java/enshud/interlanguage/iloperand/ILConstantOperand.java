package enshud.interlanguage.iloperand;

import enshud.typeexpression.AbstractType;
import enshud.typeexpression.SimpleType;

public class ILConstantOperand extends AbstractILOperand {

	public String value;
	public AbstractType typeExpression;

	public ILConstantOperand(String value, AbstractType type) {
		this.value = value;
		this.typeExpression = type;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof ILConstantOperand))
			return false;

		return this.value.equals(((ILConstantOperand)other).value);
	}

	@Override
	public int hashCode() {
		return this.value.hashCode();
	}

	@Override
	public String toString() {
		return value;
	}


	public int getIntValue() {

		int constantValue;
		if(this.value.charAt(0) == '\'')
			constantValue = (int)this.value.charAt(1);
		else if(this.value.equals("false"))
			constantValue = 0x0000;	// falseは0x0000
		else if(this.value.equals("true"))
			constantValue = 0xFFFF;	// trueは0xFFFF
		else
			constantValue = Integer.parseInt(this.value);

		return constantValue;
	}

	public boolean getBooleanValue() {
		if(!this.typeExpression.equals(SimpleType.BOOLEAN))
			throw new RuntimeException();

		if(this.value.equals("false"))
			return false;
		else if(this.value.equals("true"))
			return true;
		else if(Integer.parseInt(this.value) == 0x0000)
			return false;
		else if(Integer.parseInt(this.value) == 0xFFFF)
			return true;

		return false;
	}

}
