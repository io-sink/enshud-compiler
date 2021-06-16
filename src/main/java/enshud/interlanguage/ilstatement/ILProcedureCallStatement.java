package enshud.interlanguage.ilstatement;

public class ILProcedureCallStatement extends AbstractILStatement {

	public String procedureName;

	public ILProcedureCallStatement(String procedureName) {
		this.procedureName = procedureName;
	}

	@Override
	public String toString() {
		return String.format("call %s", procedureName);
	}

}
