package enshud.s4.asmgenerator;

import enshud.symboltable.SymbolTable;
import enshud.symboltable.SymbolTableStack;
import enshud.typeexpression.ArrayType;
import enshud.typeexpression.ProcedureType;

public class ProcedureVariableAllocator {

	private SymbolTableStack symbolTableStack;
	private String procedureName;

	public ProcedureVariableAllocator(SymbolTableStack symbolTableStack, String procedureName) {
		this.symbolTableStack = symbolTableStack;
		this.procedureName = procedureName;
	}


	// 変数のアドレスを決定し，メモリ領域の大きさを返す
	public int allocate() {

		var tailSymbolTable = symbolTableStack.get(symbolTableStack.size() - 1);
		int address = 0;

		// 局所変数と一時変数にアドレスを割り当てる
		for(String symbolName : tailSymbolTable.keySet())
			if(tailSymbolTable.get(symbolName).entryType == SymbolTable.EntryType.VARIABLE ||
					tailSymbolTable.get(symbolName).entryType == SymbolTable.EntryType.TEMPVARIABLE) {

				if(tailSymbolTable.get(symbolName).typeExpression instanceof ArrayType) {
					var typeExpression = (ArrayType)tailSymbolTable.get(symbolName).typeExpression;
					tailSymbolTable.get(symbolName).size = typeExpression.endIndex - typeExpression.startIndex + 1;
				} else
					tailSymbolTable.get(symbolName).size = 1;

				tailSymbolTable.get(symbolName).location = address;
				address += tailSymbolTable.get(symbolName).size;
			}

		int sizeSum = address;
		address++;

		// 仮パラメータにアドレスを割り当てる
		if(procedureName != null && !procedureName.equals(".main")) {
			var procedureSimpleTableEntry = symbolTableStack.findProcedureFromAnyTable(procedureName, null);
			var arguments = ((ProcedureType)procedureSimpleTableEntry.typeExpression).arguments;

			// 仮パラメータをpushで積むので，逆順に割り当てる
			for(int i = arguments.size() - 1; i >= 0; --i) {
				var argument = arguments.get(i);

				tailSymbolTable.get(argument.name).location = address;
				tailSymbolTable.get(argument.name).size = 1;
				address += tailSymbolTable.get(argument.name).size;
			}

		}

		return sizeSum;
	}

	public int tempParameterSize() {

		var procedureSimpleTableEntry = symbolTableStack.findProcedureFromAnyTable(procedureName, null);
		return ((ProcedureType)procedureSimpleTableEntry.typeExpression).arguments.size();
	}
}
