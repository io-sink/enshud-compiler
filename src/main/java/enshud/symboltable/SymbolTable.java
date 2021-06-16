package enshud.symboltable;

import java.util.HashMap;

import enshud.typeexpression.AbstractType;

public class SymbolTable extends HashMap<String, SymbolTable.SymbolTableEntry> {

	public enum TableType {
		PROGRAM, PROCEDURE, LOCAL
	}

	public enum EntryType {
		PROGRAM, PROCEDURE, VARIABLE, TEMPVARIABLE, TEMPPARAMETER, CONSTSTR
	}

	public static class SymbolTableEntry {
		public String name;
		public EntryType entryType;
		public AbstractType typeExpression;
		public boolean isOnStack;
		public int size;
		public int location;

		public SymbolTableEntry(String name, EntryType entryType, AbstractType typeExpression, boolean isOnStack) {
			this.name = name;
			this.entryType = entryType;
			this.typeExpression = typeExpression;
			this.isOnStack = isOnStack;
		}

		@Override
		public String toString() {
			return String.format("name: %s, type: %s, typeExpr: %s, isOnStack: %s, size: %d, location: %d",
					name, entryType, typeExpression, isOnStack, size, location);
		}

	}

	public TableType tableType;
	public SymbolTable(TableType tableType) {
		this.tableType = tableType;
	}

	@Override
	public String toString() {
		String res = String.format("[tableType: %s]\n", tableType);
		for (String key : this.keySet())
			res += String.format("%s => {%s}\n", key, this.get(key));
		return res;
	}

}
