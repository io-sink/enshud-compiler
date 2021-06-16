package enshud.typeexpression;

import java.util.ArrayList;

import enshud.syntaxtree.AbstractSyntaxNode;
import enshud.syntaxtree.TerminalNode;


public class ProcedureType extends AbstractType {
	public class ProcedureArgument {
		public String name;
		public AbstractType type;

		public ProcedureArgument(String name, AbstractType type) {
			this.name = name;
			this.type = type;
		}

		@Override
		public boolean equals(Object other) {
			if(!(other instanceof ProcedureArgument))
				return false;
			return name.equals(((ProcedureArgument)other).name) &&
					type.equals(((ProcedureArgument)other).type);
		}

		@Override
		public int hashCode() {
			return name.hashCode() * 31 + type.hashCode();
		}
	}


	public ArrayList<ProcedureArgument> arguments;

	public ProcedureType(ArrayList<ProcedureArgument> arguments) {
		this.arguments = arguments;
	}

	public ProcedureType(AbstractSyntaxNode node) {
		if(!node.variableName.equals("ProcedureHead"))
			throw new RuntimeException("");

		arguments = new ArrayList<ProcedureArgument>();

		var tempParameter = node.get(2);
		if(tempParameter.size() == 0) return;


		int argumentsCount = 0;

		var tempParameters = tempParameter.get(1);
		for(var child : tempParameters)
			if(child.variableName.equals("TempParameterNames")) {
				argumentsCount = arguments.size();
				for(var tempParameterName : child)
					if(tempParameterName.variableName.equals("TempParameterName")) {
						this.arguments.add(new ProcedureArgument(
								((TerminalNode)tempParameterName.get(0)).token.content,
								null));
					}

			} else if(child.variableName.equals("SimpleType")) {
				for(int i = argumentsCount; i < arguments.size(); ++i)
					arguments.get(i).type = new SimpleType(child);
				argumentsCount = 0;
			}
	}

	@Override
	public boolean equals(AbstractType obj) {
		if(obj instanceof ProcedureType) {
			if(arguments.size() != ((ProcedureType)obj).arguments.size())
				return false;

			for(int i = 0; i < arguments.size(); ++i)
				if(!arguments.get(i).equals(((ProcedureType)obj).arguments.get(i)))
					return false;

			return true;
		} else
			return false;
	}

	@Override
	public String toString() {
		String res = "procedure (";
		for(var argument : arguments)
			res += String.format("%s: %s, ", argument.name, argument.type);
		return res + ")";
	}

}
