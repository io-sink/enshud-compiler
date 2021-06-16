package enshud.symboltable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import enshud.symboltable.SymbolTable.EntryType;
import enshud.typeexpression.AbstractType;
import enshud.typeexpression.ProcedureType;
import enshud.typeexpression.ProcedureType.ProcedureArgument;

public class SymbolTableStack extends LinkedList<SymbolTable> {
	public SymbolTableStack() {
		super();
	}

	public SymbolTableStack clone() {
		var newStack = new SymbolTableStack();
		newStack.addAll(this);
		return newStack;
	}

	public class VisibleVariableIterator implements Iterator<String> {

		private Iterator<SymbolTable> symbolTableIterator;
		private Iterator<Entry<String, SymbolTable.SymbolTableEntry>> symbolTableEntryIterator;

		private boolean isTopLevel;
		private HashSet<String> returnedSymbols;
		private SymbolTable currentTable;
		private Entry<String, SymbolTable.SymbolTableEntry> currentEntry;

		public VisibleVariableIterator() {
			isTopLevel = true;
			returnedSymbols = new HashSet<String>();

			symbolTableIterator = SymbolTableStack.this.descendingIterator();
			currentTable = symbolTableIterator.next();
			symbolTableEntryIterator = currentTable.entrySet().iterator();
			currentEntry = null;

			waitNext();
		}

		private void waitNext() {
			for(;;) {

				if(!symbolTableEntryIterator.hasNext()) {
					if(!symbolTableIterator.hasNext()) {
						currentEntry = null;
						return;
					}

					currentTable = symbolTableIterator.next();
					symbolTableEntryIterator = currentTable.entrySet().iterator();

				} else {
					currentEntry = symbolTableEntryIterator.next();

					// 内側で宣言されているのと同じ名前の変数はスルー
					if(returnedSymbols.contains(currentEntry.getKey()))
						continue;

					// プログラム名と手続き名はスルー
					if(currentEntry.getValue().entryType == SymbolTable.EntryType.PROGRAM ||
							currentEntry.getValue().entryType == SymbolTable.EntryType.PROCEDURE)
						continue;

					// 外側の関数の一時変数はスルー
					if((currentEntry.getValue().entryType == SymbolTable.EntryType.TEMPPARAMETER||
							currentEntry.getValue().entryType == SymbolTable.EntryType.TEMPVARIABLE) &&
							!this.isTopLevel)
						continue;

					// 最も内側の関数を出たら一時変数をスルー
					if(currentTable.tableType != SymbolTable.TableType.LOCAL &&
							!symbolTableEntryIterator.hasNext())
						this.isTopLevel = false;

					this.returnedSymbols.add(currentEntry.getKey());
					return;
				}

			}

		}

		@Override
		public boolean hasNext() {
			return currentEntry != null;
		}

		@Override
		public String next() {
			String res = currentEntry.getKey();
			waitNext();

			return res;
		}

	}

	public VisibleVariableIterator visibleVariableIterator() {
		return new VisibleVariableIterator();
	}

	public String getProgramName() {
		for(String symbolName : this.get(0).keySet())
			if(this.get(0).get(symbolName).entryType == EntryType.PROGRAM)
				return symbolName;
		return null;
	}

	public int getScopeDepth(SymbolTable.SymbolTableEntry entry) {
		if(entry.entryType == SymbolTable.EntryType.TEMPVARIABLE)
			return Integer.MAX_VALUE;

		int depth = this.size();
		var it = this.listIterator(this.size());

		while(it.hasPrevious()) {
			--depth;
			var symbolTable = it.previous();

			if(symbolTable.containsKey(entry.name))
				return depth;
		}

		throw new RuntimeException();
	}

	public SymbolTable.SymbolTableEntry findVariableFromLastTable(String variableName) {
		var symbolTable = this.getLast();

		if(symbolTable.containsKey(variableName) &&
					(symbolTable.get(variableName).entryType == SymbolTable.EntryType.VARIABLE ||
					symbolTable.get(variableName).entryType == SymbolTable.EntryType.TEMPPARAMETER ||
					symbolTable.get(variableName).entryType == SymbolTable.EntryType.TEMPVARIABLE))
			return symbolTable.get(variableName);
		else
			return null;
	}

	public SymbolTable.SymbolTableEntry findVariableFromAnyTable(String variableName) {
		boolean isTopLevel = true;
		var it = this.listIterator(this.size());

		while(it.hasPrevious()) {
			var symbolTable = it.previous();
			if(symbolTable.containsKey(variableName) &&
					(symbolTable.get(variableName).entryType == SymbolTable.EntryType.VARIABLE ||
					symbolTable.get(variableName).entryType == SymbolTable.EntryType.TEMPPARAMETER)) {
				return symbolTable.get(variableName);
			}

			if(symbolTable.containsKey(variableName) && isTopLevel &&
					(symbolTable.get(variableName).entryType == SymbolTable.EntryType.TEMPVARIABLE)) {
				return symbolTable.get(variableName);
			}

			if(symbolTable.tableType != SymbolTable.TableType.LOCAL)
				isTopLevel = false;
		}

		return null;
	}


	public SymbolTable.SymbolTableEntry findProcedureFromLastTable(String procedureName, List<AbstractType> argTypes) {
		// argTypesがnullなら引数によらず比較

		var symbolTable = this.getLast();

		if(symbolTable.containsKey(procedureName) &&
					symbolTable.get(procedureName).entryType == SymbolTable.EntryType.PROCEDURE) {

			if(argTypes == null ||
					argTypesEquals(argTypes, ((ProcedureType)symbolTable.get(procedureName).typeExpression).arguments))
				return symbolTable.get(procedureName);
			else
				return null;
		} else
			return null;

	}

	public SymbolTable.SymbolTableEntry findProcedureFromAnyTable(String procedureName, List<AbstractType> argTypes) {
		// argTypesがnullなら引数によらず比較

		var it = this.listIterator(this.size());

		while(it.hasPrevious()) {
			var symbolTable = it.previous();
			if(symbolTable.containsKey(procedureName) &&
						symbolTable.get(procedureName).entryType == SymbolTable.EntryType.PROCEDURE) {

				if(argTypes == null ||
						argTypesEquals(argTypes, ((ProcedureType)symbolTable.get(procedureName).typeExpression).arguments))
					return symbolTable.get(procedureName);
			}
		}


		return null;
	}

	private boolean argTypesEquals(List<AbstractType> argTypes1, List<ProcedureArgument> argTypes2) {
		if(argTypes1.size() != argTypes2.size())
			return false;
		for(int i = 0; i < argTypes1.size(); ++i)
			if(!argTypes1.get(i).equals(argTypes2.get(i).type))
				return false;
		return true;
	}

	@Override
	public String toString() {
		String res = "";
		for (var symbolTable : this) {
			res += "---\n";
			res += symbolTable.toString();
			res += "---\n";
		}
		return res;
	}
}
