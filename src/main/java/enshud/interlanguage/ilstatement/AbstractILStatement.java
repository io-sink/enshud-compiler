package enshud.interlanguage.ilstatement;

import java.util.HashSet;

import enshud.interlanguage.iloperand.AbstractILOperand;
import enshud.interlanguage.iloperand.AbstractILVariableOperand;
import enshud.interlanguage.iloperand.ILConstantOperand;
import enshud.interlanguage.iloperand.ILSimpleVariableOperand;
import enshud.symboltable.SymbolTableStack;

public abstract class AbstractILStatement {

	public SymbolTableStack symbolTableStack;

	// 参照される変数の集合
	public HashSet<ILSimpleVariableOperand> getRefSet() {
		return new HashSet<ILSimpleVariableOperand>();
	}

	// 新たに定義される変数の集合
	public HashSet<ILSimpleVariableOperand> getDefSet() {
		return new HashSet<ILSimpleVariableOperand>();
	}

	// データフロー解析の結果，文が冗長である場合に削除してよいか(配列の値を定義する文はfalseにする)
	public boolean isRemovable() {
		return false;
	}

	// データフロー解析の結果，オペランドを定数に置き換えてよいか(配列が関わる文はfalseにする)
	public boolean isOperandReplaceableByConstant() {
		return false;
	}

	// オペランドの参照を置き換える
	public void replaceOperandReference(AbstractILOperand oldOperand, AbstractILOperand newOperand) {
		throw new RuntimeException();
	}

	// オペランドの定義を置き換える
	public void replaceOperandDefinition(AbstractILVariableOperand oldOperand, AbstractILVariableOperand newOperand) {
		throw new RuntimeException();
	}

	// 結果が演算かどうか
	public boolean isConstant() {
		return false;
	}

	// isConstant()がtrueの場合，その値を返す
	public ILConstantOperand getConstantValue() {
		throw new RuntimeException();
	}


}
