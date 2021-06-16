package enshud.asm.operand;

import java.util.HashMap;

public enum RegisterOperand implements IAsmOperand {
	GR1, GR2, GR3, GR4, GR5, GR6, GR7, 	// 汎用レジスタ
	GR8, 	// スタックポインタ
	GR0;	// 当面は使わない

	public static RegisterOperand[] registers = {
			GR1, GR2, GR3, GR4, GR5, GR6, GR7, GR8, GR0
			};

	public static HashMap<RegisterOperand, Integer> registersInv = new HashMap<RegisterOperand, Integer>(){
		{
			put(GR1, 0);
			put(GR2, 1);
			put(GR3, 2);
			put(GR4, 3);
			put(GR5, 4);
			put(GR6, 5);
			put(GR7, 6);
			put(GR8, 7);
			put(GR0, 8);
		}
	};
}
