package enshud.s3.checker;

import java.util.HashMap;

import enshud.syntaxtree.AbstractSyntaxNode;
import enshud.typeexpression.AbstractType;

public class TypeExpressionMap extends HashMap<AbstractSyntaxNode, AbstractType> {

	// キーが存在しない場合は実行時例外を発生させる
	@Override
	public AbstractType get(Object node) {
		var res = super.get(node);
		if(res == null)
			throw new RuntimeException("type not defined");

		return res;
	}
}
