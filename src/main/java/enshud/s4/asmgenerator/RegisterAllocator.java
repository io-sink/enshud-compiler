package enshud.s4.asmgenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

import enshud.flowgraph.FlowGraphNode;
import enshud.interlanguage.iloperand.AbstractILOperand;
import enshud.interlanguage.iloperand.ILConstantOperand;
import enshud.interlanguage.iloperand.ILIndexedVariableOperand;
import enshud.interlanguage.iloperand.ILSimpleVariableOperand;
import enshud.interlanguage.ilstatement.ILArgPushStatement;
import enshud.interlanguage.ilstatement.ILAssign1Statement;
import enshud.interlanguage.ilstatement.ILAssign2Statement;
import enshud.interlanguage.ilstatement.ILConditionalJump;
import enshud.interlanguage.ilstatement.ILCopyStatement;
import enshud.s4.compiler.Compiler;

// レジスタ割り付け計画を作る
public class RegisterAllocator {

	// 汎用レジスタとしてGR1～GR5のみを使う(GR0は面倒くさい, GR6，GR7は使用不可！)
	// GR0も使うように改良したい
	public static final int REGISTER_COUNT = 5;

	// 各行のレジスタ番号の対応関係を返す
	// 左辺が配列の要素の場合はすぐにメモリ上に書き込まれるが，仮に使われるレジスタの情報も含む
	public ArrayList<HashMap<AbstractILOperand, Integer>> allocativePlan;

	// 各変数の定義と参照が行われている行番号をまとめておき，二分探索できるようにする
	private HashMap<String, TreeSet<Integer>> variableDefinition;
	private HashMap<String, TreeSet<Integer>> variableReference;

	// 割り付け中に使うデータ構造，配列の要素は入れない
	private BiHashMap<String, Integer> currentAllocation;

	// TODO 定数はGR0に割り付けるようにする
	public ArrayList<HashMap<AbstractILOperand, Integer>> allocate(FlowGraphNode basicBlock) {

		// 各変数の定義と参照を計算
		calculateVariableDefinitionAndReference(basicBlock);

		// 割付の初期化
		allocativePlan = new ArrayList<HashMap<AbstractILOperand, Integer>>();
		currentAllocation = new BiHashMap<String, Integer>();

		// 順次割付していく
		int currentLine = -1;
		for(var statement : basicBlock) {
			++currentLine;

			allocativePlan.add(new HashMap<AbstractILOperand, Integer>());

			if(statement instanceof ILCopyStatement) {
				// 複写文
				// TODO 実は複写文の左辺はメモリでもよい(ただしオペランドが配列の要素の場合は考慮が必要)
				AbstractILOperand leftHandSide, rightHandSide;

				leftHandSide = ((ILCopyStatement)statement).leftHandSide;
				rightHandSide = ((ILCopyStatement)statement).rightHandSide;

				int leftHandSideRegister = -1;
				if(leftHandSide instanceof ILIndexedVariableOperand || rightHandSide instanceof ILIndexedVariableOperand) {
					// 左右のどちらかが添え字付なら必ず割り付ける

					// 左辺を割り付ける(必ず割り付ける)
					leftHandSideRegister =
							mandatoryAllocationPlan(
									leftHandSide,
									currentLine,
									new Integer[] {
											currentAllocation.get(rightHandSide.toString())
											}
									);

				} else if(Compiler.allocatable) {

					// 左辺を割り付ける(割り付けなくてもよい)
					leftHandSideRegister =
								optionalAllocationPlan(
										leftHandSide,
										currentLine,
										new Integer[] {
												currentAllocation.get(rightHandSide.toString())
												}
										);
				}

				if(Compiler.allocatable) {
					// 右辺を割り付ける(割り付けなくてもよい)
					int rightHandSideRegister =
								optionalAllocationPlan(
										rightHandSide,
										currentLine,
										new Integer[] {
												leftHandSideRegister
												}
										);
				}


			} else if((statement instanceof ILAssign1Statement || statement instanceof ILAssign2Statement) && Compiler.allocatable) {
				// 単項または二項演算
				AbstractILOperand leftHandSide, leftOperand, rightOperand;

				if(statement instanceof ILAssign1Statement) {
					// 単項演算は左オペランドが定数の二項演算と見ることができる
					leftHandSide = ((ILAssign1Statement)statement).leftHandSide;
					leftOperand = new ILConstantOperand("undefined", null);
					rightOperand = ((ILAssign1Statement)statement).operand;

				} else {
					leftHandSide = ((ILAssign2Statement)statement).leftHandSide;
					leftOperand = ((ILAssign2Statement)statement).leftOperand;
					rightOperand = ((ILAssign2Statement)statement).rightOperand;
				}

				// 任意割り付け
				int leftHandSideRegister =
							optionalAllocationPlan(
									leftHandSide,
									currentLine,
									null	// 必要に応じて退避が行われるので左辺は任意のレジスタに割り付けて良い
									);

				int leftOperandRegister =
						optionalAllocationPlan(
								leftOperand,
								currentLine,
								new Integer[] {
										leftHandSideRegister,
										currentAllocation.get(rightOperand.toString())
										}
								);


				int rightOperandRegister =
						optionalAllocationPlan(
								rightOperand,
								currentLine,
								new Integer[] {
										leftHandSideRegister,
										leftOperandRegister
										}
								);


			} else if(statement instanceof ILArgPushStatement) {
				AbstractILOperand operand = ((ILArgPushStatement)statement).operand;

				int selectedRegister =
						mandatoryAllocationPlan(
								operand,
								currentLine,
								null
								);


			} else if(statement instanceof ILConditionalJump && Compiler.allocatable) {
				// 条件付きジャンプ
				// 条件付きジャンプは，CMPに定数0と論理値を入力する
				// したがって，レジスタへの代入が必須のオペランド1つ
				AbstractILOperand condition = ((ILConditionalJump)statement).condition;

				// 条件を割り付ける
				int conditionRegister =
						optionalAllocationPlan(
								condition,
								currentLine,
								null
								);

			}

		}

		return allocativePlan;
	}


	/*
	 	オペランドをレジスタに無条件に割り付ける
	 	オペランドが配列要素である場合，選択されたレジスタの割り当てが空になる
	 */
	private int mandatoryAllocationPlan(
			AbstractILOperand operand,
			int currentLine,
			Integer[] exceptRegisters) {

		// すでに割り付けられているとき
		if(operand instanceof ILSimpleVariableOperand &&
				currentAllocation.containsKey(((ILSimpleVariableOperand)operand).variableName))
			return currentAllocation.get(((ILSimpleVariableOperand)operand).variableName);

		// 割り付けるレジスタを決定
		int selectedRegister = selectRegister(currentLine, exceptRegisters);

		// 割り付けを実施
		allocativePlan.get(currentLine).put(operand, selectedRegister);

		if(operand instanceof ILSimpleVariableOperand && Compiler.allocatable)
			currentAllocation.put(((ILSimpleVariableOperand)operand).variableName, selectedRegister);
		else
			currentAllocation.removeInv(selectedRegister);

		return selectedRegister;
	}

	/*
	 	オペランドをレジスタに割り付けるべきか判断し，必要に応じて割り付ける
	 	配列要素は割り付けない
	 */
	private int optionalAllocationPlan(
			AbstractILOperand operand,
			int currentLine,
			Integer[] exceptRegisters) {

		// 配列要素は無視
		if(!(operand instanceof ILSimpleVariableOperand))
			return -1;

		// すでに割り付けられているとき
		if(currentAllocation.containsKey(((ILSimpleVariableOperand)operand).variableName))
			return currentAllocation.get(((ILSimpleVariableOperand)operand).variableName);

		// 割り付けるレジスタを決定
		int selectedRegister = selectRegister(currentLine, exceptRegisters);

		int maxCost = this.selectRegister_maxCost;
		int allocCost = allocativeCost(((ILSimpleVariableOperand)operand).variableName, currentLine);

		if(allocCost < maxCost) {
			// 既に割り付けられている他の変数よりコストが低ければ割り付けを実施
			allocativePlan.get(currentLine).put(operand, selectedRegister);

			if(Compiler.allocatable)
				currentAllocation.put(((ILSimpleVariableOperand)operand).variableName, selectedRegister);
		}

		return selectedRegister;
	}



	// ある変数が割り付けられているレジスタを除いて，割り付けに最適なレジスタを探す
	private int selectRegister_maxCost;	// 戻り値を2つ返したいが...
	private int selectRegister(int currentLine, Integer[] exceptRegisters) {

		var exceptRegistersSet = new HashSet<Integer>();
		if(exceptRegisters != null)
			for(Integer register : exceptRegisters)
				if(register != null)
					exceptRegistersSet.add(register);

		int res = -1;
		int maxCost = Integer.MIN_VALUE;
		for(int reg = 0; reg < REGISTER_COUNT; ++reg) {
			if(exceptRegistersSet.contains(reg))
				continue;

			String regVariable = currentAllocation.getInv(reg);
			// 空いているレジスタがあればすぐに返す
			if(regVariable == null) {
				this.selectRegister_maxCost = Integer.MAX_VALUE;
				return reg;
			}

			int cost = allocativeCost(regVariable, currentLine);
			if(cost > maxCost) {
				res = reg;
				maxCost = cost;
			}
		}

		if(res == -1)
			throw new RuntimeException();

		this.selectRegister_maxCost = res;
		return res;
	}

	// 割り付けコストを計算
	private int allocativeCost(String variableName, int currentLine) {
		if(!variableDefinition.containsKey(variableName))
			throw new RuntimeException();

		// 次にその変数の値が参照されるまでの行数，二度と参照されなければコスト∞
		Integer nextDefinition = variableDefinition.get(variableName).ceiling(currentLine + 1);
		Integer nextReference = variableReference.get(variableName).ceiling(currentLine + 1);

		if(nextDefinition == null)
			nextDefinition = Integer.MAX_VALUE;
		if(nextReference == null)
			nextReference = Integer.MAX_VALUE;

		if(nextDefinition < nextReference)
			return Integer.MAX_VALUE;
		else
			return nextReference - currentLine;
	}


	// 各変数が何行目で定義，参照されているか計算する
	private void calculateVariableDefinitionAndReference(FlowGraphNode basicBlock) {

		// 各変数の定義と参照を初期化
		variableDefinition = new HashMap<String, TreeSet<Integer>>();
		variableReference = new HashMap<String, TreeSet<Integer>>();

		for (var it = basicBlock.symbolTableStack.visibleVariableIterator(); it.hasNext();) {
			String variableName = it.next();
			variableDefinition.put(variableName, new TreeSet<Integer>());
			variableReference.put(variableName, new TreeSet<Integer>());
		}

		// 各変数の定義と参照を記録
		int currentLine = -1;
		for(var statement : basicBlock) {
			++currentLine;

			// 定義を追加
			for(var operand : statement.getDefSet())
				if(operand instanceof ILSimpleVariableOperand)
					variableDefinition.get(((ILSimpleVariableOperand) operand).variableName).add(currentLine);

			// 参照を追加
			for(var operand : statement.getRefSet())
				if(operand instanceof ILSimpleVariableOperand)
					variableReference.get(((ILSimpleVariableOperand) operand).variableName).add(currentLine);

		}

	}

}
