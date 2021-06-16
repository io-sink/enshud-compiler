package enshud.interlanguage.iloperand;

public class ILIndexedVariableOperand extends AbstractILVariableOperand {

	public String variableName;
	public ILSimpleVariableOperand index;
	//public String indexName;

	public ILIndexedVariableOperand(String variableName, String indexName) {
		this.variableName = variableName;
		//this.indexName = indexName;
		this.index = new ILSimpleVariableOperand(indexName);
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof ILIndexedVariableOperand))
			return false;

		return this.variableName.equals(((ILIndexedVariableOperand)other).variableName) &&
				this.index.equals(((ILIndexedVariableOperand)other).index);
	}

	@Override
	public int hashCode() {
		return this.variableName.hashCode() * 32 + this.index.hashCode();
	}

	@Override
	public String toString() {
		return String.format("%s[%s]", variableName, index);
	}
}
