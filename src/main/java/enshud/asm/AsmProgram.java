package enshud.asm;

import java.util.Collection;
import java.util.LinkedList;

import enshud.asm.instruction.AsmInstruction;
import enshud.asm.instruction.AsmLabel;
import enshud.asm.instruction.IAsmInstruction;

public class AsmProgram extends LinkedList<IAsmInstruction> {

	public String programName;

	public AsmProgram(String programName) {
		this.programName = programName;
	}

	@Override
	public String toString() {

		String res = "";

		String labelName = null;
		for(var instruction : this) {
			if(instruction instanceof AsmLabel) {
				labelName = ((AsmLabel)instruction).labelName;

			} else if(instruction instanceof AsmInstruction) {

				if(labelName == null) {
					res += String.format(" %s\n", instruction.toString());

				} else {
					res += String.format("%s %s\n", labelName, instruction.toString());
					labelName = null;
				}

			} else {
				res += String.format("%s\n", instruction.toString());
			}
		}

		if(labelName != null)
			res += String.format("%s %s\n",
					labelName,
					new AsmInstruction(
							AsmInstruction.Operation.NOP, null)
					);

		return res;
	}


	@Override
	public boolean add(IAsmInstruction instruction) {
		if(instruction instanceof AsmInstruction &&
			!((AsmInstruction)instruction).isMeaningful())
				return false;

		return super.add(instruction);
	}

	@Override
	public void add(int index, IAsmInstruction instruction) {
		if(instruction instanceof AsmInstruction &&
			!((AsmInstruction)instruction).isMeaningful())
				return;

		super.add(index, instruction);
	}

	@Override
	public boolean addAll(Collection<? extends IAsmInstruction> instructions) {
		boolean res = false;
		for(var instruction : instructions) {
			if(instruction instanceof AsmInstruction &&
					!((AsmInstruction)instruction).isMeaningful())
						continue;

			res |= super.add(instruction);
		}
		return res;
	}

	@Override
	public boolean addAll(int index, Collection<? extends IAsmInstruction> instructions) {
		throw new RuntimeException("");
	}

	@Override
	public void addFirst(IAsmInstruction instruction) {
		if(instruction instanceof AsmInstruction &&
			!((AsmInstruction)instruction).isMeaningful())
				return;

		super.addFirst(instruction);
	}

	@Override
	public void addLast(IAsmInstruction instruction) {
		if(instruction instanceof AsmInstruction &&
			!((AsmInstruction)instruction).isMeaningful())
				return;

		super.addLast(instruction);
	}
}
