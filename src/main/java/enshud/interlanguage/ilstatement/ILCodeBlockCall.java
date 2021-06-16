package enshud.interlanguage.ilstatement;

import enshud.syntaxtree.AbstractSyntaxNode;

// 仮想的な3番地コード
// アセンブラのコードには変換されない
public class ILCodeBlockCall extends AbstractILStatement {
	// CompoundStatementのノード
	public AbstractSyntaxNode node;

	public ILCodeBlockCall(AbstractSyntaxNode node) {
		this.node = node;
	}

	@Override
	public String toString() {
		return String.format("codeblock node(%d)", node.hashCode());
	}

}
