package enshud.s4.asmgenerator;

import java.util.HashMap;

import enshud.flowgraph.FlowGraphProgram;
import enshud.interlanguage.iloperand.ILConstantOperand;
import enshud.interlanguage.ilstatement.ILWriteStatement;

public class ConstantAllocator {

	int constNumber = 0;
	public ConstantAllocator() {
		constNumber = 0;
	}

	public HashMap<String, String> allocate(FlowGraphProgram flowGraphs) {

		var constantAllocation = new HashMap<String, String>();

		for(var block : flowGraphs)
			for(var statement : block) {

				if(!(statement instanceof ILWriteStatement))
					continue;

				var operand = ((ILWriteStatement)statement).operand;
				if(!(operand instanceof ILConstantOperand))
					continue;

				String strOperand = ((ILConstantOperand)operand).value;
				if(!(strOperand.charAt(0) == '\'' && strOperand.length() > 3))
					continue;

				constantAllocation.put(strOperand, ".LConst" + constNumber++);
			}

		return constantAllocation;
	}
}
