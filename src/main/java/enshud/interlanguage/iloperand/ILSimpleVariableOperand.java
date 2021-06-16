package enshud.interlanguage.iloperand;

public class ILSimpleVariableOperand extends AbstractILVariableOperand {

	public String variableName;

	public ILSimpleVariableOperand(String variableName) {
		this.variableName = variableName;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof ILSimpleVariableOperand))
			return false;

		return this.variableName.equals(((ILSimpleVariableOperand)other).variableName);
	}

	@Override
	public int hashCode() {
		return this.variableName.hashCode();
	}

	@Override
	public String toString() {
		return variableName;
	}
}
