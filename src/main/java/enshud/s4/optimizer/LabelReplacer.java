package enshud.s4.optimizer;

import java.util.ArrayList;
import java.util.HashMap;

import enshud.asm.AsmProgram;
import enshud.asm.instruction.AsmInstruction;
import enshud.asm.instruction.AsmLabel;
import enshud.asm.operand.ConstantOperand;
import enshud.s4.ilgenerator.LabelGenerator;

public class LabelReplacer {

	public boolean optimize(AsmProgram asmProgram) {

		var labelMap = getLabelMap(asmProgram);

		boolean isPrevLabel = false;
		for(var it = asmProgram.listIterator(asmProgram.size()); it.hasPrevious();) {
			var instruction = it.previous();

			if(instruction instanceof AsmLabel) {

				if(!isPrevLabel) {

					// ラベルを付け替える
					((AsmLabel)instruction).labelName = labelMap.get(((AsmLabel)instruction).labelName);
					isPrevLabel = true;
				} else
					it.remove();

			} else if(instruction instanceof AsmInstruction) {

				// ラベルを付け替える
				for(var operand : ((AsmInstruction)instruction).operands)
					if(operand instanceof ConstantOperand) {
						var labelOperand = (ConstantOperand)operand;
						if(labelMap.containsKey(labelOperand.labelValue))
							labelOperand.labelValue = labelMap.get(labelOperand.labelValue);
					}

				isPrevLabel = false;

			} else {
				isPrevLabel = false;
			}
		}

		return true;
	}

	private HashMap<String, String> getLabelMap(AsmProgram asmProgram) {
		var labelGenerator = new LabelGenerator();
		var labelMap = new HashMap<String, String>();
		var labelQueue = new ArrayList<String>();

		for(var instruction : asmProgram) {
			if(instruction instanceof AsmLabel) {
				String labelName = ((AsmLabel)instruction).labelName;
				labelMap.put(labelName, labelGenerator.get());
				labelQueue.add(labelName);

			} else if(instruction instanceof AsmInstruction) {
				if(labelQueue.size() > 1) {
					for(int i = 0; i < labelQueue.size() - 1; ++i)
						labelMap.put(labelQueue.get(i), labelMap.get(labelQueue.get(labelQueue.size() - 1)));
				}
				labelQueue.clear();
			}
		}

		if(labelQueue.size() > 1)
			for(int i = 0; i < labelQueue.size() - 1; ++i)
				labelMap.put(labelQueue.get(i), labelMap.get(labelQueue.get(labelQueue.size() - 1)));

		return labelMap;
	}

}
