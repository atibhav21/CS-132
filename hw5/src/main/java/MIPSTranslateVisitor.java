import cs132.vapor.ast.*;
import java.util.*;

public class MIPSTranslateVisitor extends
			 VInstr.Visitor<java.lang.RuntimeException> {
	LinkedList<String> mips_code = new LinkedList<String>();
	int indent_counter = 0;
	boolean shouldPrintHeapAlloc = false;
	boolean shouldPrintError = false;
	boolean shouldPrintIntS = false;

	public MIPSTranslateVisitor(VaporProgram program) {
		mips_code.add(".data");
		mips_code.add("");

		for (VDataSegment segment : program.dataSegments) {
			mips_code.add(indent() + segment.ident + ":");

			indent_counter += 1;
			for(VOperand function : segment.values) {
				// Substring to strip the colon before the VMT Name.
				mips_code.add(indent() + function.toString().substring(1));
			}
			indent_counter -= 1;
			// Add an empty line after printing VMT.
			mips_code.add("");
			
		}

		mips_code.add(indent() + ".text");
		mips_code.add("");

		indent_counter += 1;

		mips_code.add(indent() + "jal Main");
		mips_code.add(indent() + "li $v0 10");
		mips_code.add(indent() + "syscall");

		indent_counter -= 1;
		mips_code.add("");

		for (VFunction function : program.functions) {
			mips_code.add(indent() + function.ident + ":");
			
			// Add 2 since min size of each frame is 8.
			int sizeOfFrame = 4 * (function.stack.local + function.stack.out 
														+ 2);

			indent_counter += 1;
			// Store the frame pointer.

			mips_code.add(indent() + "sw $fp -8($sp)");
			mips_code.add(indent() + "move $fp $sp");
			mips_code.add(indent() + "subu $sp $sp " +
											String.valueOf(sizeOfFrame));
			mips_code.add(indent() + "sw $ra -4($fp)");

			// Iterate over all the instructions, and call visitors.
			LinkedList<VCodeLabel> code_labels = new LinkedList<VCodeLabel>
											(Arrays.asList(function.labels));
			for(VInstr instruction : function.body) {
				for(Iterator<VCodeLabel> it = code_labels.iterator(); 
										it.hasNext();) {
					VCodeLabel label = it.next();
					if(label.sourcePos.line < instruction.sourcePos.line) {
						mips_code.add(label.ident + ":");
						it.remove();
					}
				}
				instruction.accept(this);
			}

			mips_code.add(indent() + "lw $ra -4($fp)");
			mips_code.add(indent() + "lw $fp -8($fp)");
			mips_code.add(indent() + "addu $sp $sp " +
											String.valueOf(sizeOfFrame));
  			mips_code.add(indent() + "jr $ra");

			indent_counter -= 1;
			mips_code.add("");
		}

		if(shouldPrintHeapAlloc) {
			printHeapAlloc();
		}

		if(shouldPrintError) {
			printError();
		}

		if(shouldPrintIntS) {
			printIntS();
		}

		mips_code.add(".data");
		mips_code.add(".align 0");
		mips_code.add("_newline: .asciiz \"\\n\"");
		mips_code.add("_str0: .asciiz \"null pointer\\n\"");
	}

	public void printHeapAlloc() {
		mips_code.add("_heapAlloc:");
		indent_counter += 1;
		mips_code.add(indent() + "li $v0 9");
		mips_code.add(indent() + "syscall");
		mips_code.add(indent() + "jr $ra");
		mips_code.add("");
		indent_counter -= 1;
	}

	public void printError() {
		mips_code.add("_error:");
		indent_counter += 1;
		mips_code.add(indent() + "li $v0 4");
		mips_code.add(indent() + "syscall");
		mips_code.add(indent() + "li $v0 10");
		mips_code.add(indent() + "syscall");
		mips_code.add("");
		indent_counter -= 1;
	}

	public void printIntS() {
		mips_code.add("_print:");
		indent_counter += 1;
		mips_code.add(indent() + "li $v0 1");
  		mips_code.add(indent() + "syscall");
  		mips_code.add(indent() + "la $a0 _newline");
  		mips_code.add(indent() + "li $v0 4");
  		mips_code.add(indent() + "syscall");
  		mips_code.add(indent() + "jr $ra");
  		mips_code.add("");
		indent_counter -= 1;
	}

	public String indent() {
		String indentation = "";
		for(int i = 0; i < indent_counter; i++) {
			indentation += " ";
		}
		return indentation;
	}

	public LinkedList<String> getMIPSCode() {
		return mips_code;
	}

	public void visit(VAssign a) {
		if (a.source instanceof VVarRef) {
			// RHS is a register so use move instruction.
			mips_code.add(indent() + "move " + a.dest.toString() + " "
										+ a.source.toString());
		}
		else {
			// RHS is an immediate value or a label.
			if(a.source instanceof VLabelRef) {
				mips_code.add(indent() + "la " + a.dest.toString() + " "
										+ a.source.toString().substring(1));
			}
			else {
				mips_code.add(indent() + "li " + a.dest.toString() + " "
										+ a.source.toString());
			}
			
		}
	}

	public void visit(VBranch b) {
		if(b.positive) {
			// if statement.
			mips_code.add(indent() + "bnez " + b.value.toString() + " "
									+ b.target.toString().substring(1));
		}
		else {
			// if0 statement.
			mips_code.add(indent() + "beqz " + b.value.toString() + " "
								 	+ b.target.toString().substring(1));
		}
	}

	public void visit(VBuiltIn c) {
		VOperand operand1 = c.args[0];
		VOperand operand2 = (c.args.length > 1) ? c.args[1] : null;

		if(c.op == VBuiltIn.Op.HeapAllocZ) {
			shouldPrintHeapAlloc = true;
			if(operand1 instanceof VLitInt) {
				mips_code.add(indent() + "li $a0 " + operand1.toString());
			}
			else {
				mips_code.add(indent() + "move $a0 " + operand1.toString());
			}
			mips_code.add(indent() + "jal _heapAlloc");
			mips_code.add(indent() + "move " + c.dest.toString() + " $v0");
		}
		else if(c.op == VBuiltIn.Op.Error) {
			shouldPrintError = true;
			// TODO: Check if this needs to be changed.
			mips_code.add(indent() + "la $a0 _str0");
			mips_code.add(indent() + "j _error");
		}
		else if (c.op == VBuiltIn.Op.PrintIntS) {
			shouldPrintIntS = true;
			if(operand1 instanceof VLitInt) {
				mips_code.add(indent() + "li $a0 " + operand1.toString());
			}
			else {
				mips_code.add(indent() + "move $a0 " + operand1.toString());
			}
			mips_code.add(indent() + "jal _print");
		}
		else if (c.op == VBuiltIn.Op.Sub) {
			String target = c.dest.toString();
			if(operand1 instanceof VLitInt) {
				mips_code.add(indent() + "li $t9 " + operand1.toString());
				mips_code.add(indent() + "subu " + target + " $t9 "
					+ operand2.toString());
			}
			else {
				mips_code.add(indent() + "subu " + target + " "
					+ operand1.toString() + " " + operand2.toString());
			}
		}
		else if (c.op == VBuiltIn.Op.MulS) {
			String target = c.dest.toString();
			if(operand1 instanceof VLitInt) {
				mips_code.add(indent() + "li $t9 " + operand1.toString());
				mips_code.add(indent() + "mul " + target + " $t9 "
					+ operand2.toString());
			}
			else {
				mips_code.add(indent() + "mul " + target + " "
					+ operand1.toString() + " " + operand2.toString());
			}
		}
		else if (c.op == VBuiltIn.Op.Add) {
			String target = c.dest.toString();
			if(operand1 instanceof VLitInt) {
				if(operand2 instanceof VLitInt) {
					mips_code.add(indent() + "addiu " + target + " " 
						+ operand1.toString() + " " + operand2.toString());
				}
				else {
					mips_code.add(indent() + "addiu " + target + " " 
						+ operand2.toString() + " " + operand1.toString());
				}
			}
			else {
				// Operand 1 is a VVarRef
				if (operand2 instanceof VLitInt) {
					mips_code.add(indent() + "addiu " + target + " "
						+ operand1.toString() + " " + operand2.toString());
				}
				else {
					mips_code.add(indent() + "addu " + target + " " 
						+ operand1.toString() + " " + operand2.toString());
				}
			}
			
		}
		else if (c.op == VBuiltIn.Op.Eq) { 
			String target = c.dest.toString();
			if(operand2 instanceof VLitInt) {
				mips_code.add(indent() + "li $v0 " + operand2.toString());
				mips_code.add(indent() + "seq " + target + " " + 
									operand1.toString() + " $v0");
			}
			else {
				mips_code.add(indent() + "seq " + target + " " +
					operand1.toString() + " " + operand2.toString());
			}
		}
		else if (c.op == VBuiltIn.Op.Lt) {
			String target = c.dest.toString();
			if(operand1 instanceof VLitInt) {
				if(operand2 instanceof VLitInt) {
					mips_code.add(indent() + "li $v0 " + operand1.toString());
					mips_code.add(indent() + "sltiu $v0 " + operand2.toString());
				}
				else {
					mips_code.add(indent() + "sgtu " + target + " " +
						operand2.toString() + " " + operand1.toString());
				}
			}
			else {
				if(operand2 instanceof VLitInt) {
					mips_code.add(indent() + "sltiu " + target + " " + 
						operand1.toString() + " " + operand2.toString());
				}
				else {
					mips_code.add(indent() + "sltu " + target + " " +
						operand1.toString() + " " + operand2.toString());
				}
			}
		}
		else if (c.op == VBuiltIn.Op.LtS) {
			String target = c.dest.toString();
			if(operand1 instanceof VLitInt) {
				if(operand2 instanceof VLitInt) {
					mips_code.add(indent() + "li $v0 " + operand1.toString());
					mips_code.add(indent() + "slti $v0 " + operand2.toString());
				}
				else {
					mips_code.add(indent() + "sgt " + target + " " +
						operand2.toString() + " " + operand1.toString());
				}
			}
			else {
				if(operand2 instanceof VLitInt) {
					mips_code.add(indent() + "slti " + target + " " + 
						operand1.toString() + " " + operand2.toString());
				}
				else {
					mips_code.add(indent() + "slt " + target + " " +
						operand1.toString() + " " + operand2.toString());
				}
			}
		}

	}

	public void visit(VCall c)  {
		if(c.addr instanceof VAddr.Label) {
			mips_code.add(indent() + "jal " + c.addr.toString().substring(1));
		}
		else {
			mips_code.add(indent() + "jalr " + c.addr.toString());
		}
		
	}
           
	public void	visit(VGoto g) {
		mips_code.add(indent() + "j " + g.target.toString().substring(1));
	}
           
	public void	visit(VMemRead r) {
		if(r.source instanceof VMemRef.Global) {
			// Global reference so just use the offset directly from register.
			VMemRef.Global global_ref = (VMemRef.Global) r.source;
			String mem_address = String.valueOf(global_ref.byteOffset) + "(" 
											+ global_ref.base.toString() + ")";
			mips_code.add(indent() + "lw " + r.dest.toString() + " " 
											+ mem_address);
		}
		else {
			VMemRef.Stack stack_ref = (VMemRef.Stack) r.source;
			if (stack_ref.region == VMemRef.Stack.Region.valueOf("In")) {
				// Argument, so reference using $fp.
				mips_code.add("lw " + r.dest.toString() + " " +
						String.valueOf(stack_ref.index * 4) + "($fp)");
			}
			else if (stack_ref.region == VMemRef.Stack.Region.valueOf("Local")) {
				// Local stack variable, so reference using $sp.
				mips_code.add("lw " + r.dest.toString() + " " +
						String.valueOf(stack_ref.index * 4) + "($sp)");
			}
			
		}
		
	}
           
	public void	visit(VMemWrite w) {
		/*
			$t0 + 0 = :vmt_MyVisitor
			$t0 + 12 = $t1
			$t0 + 16 = 0
			Local[1] = $s1
		*/
		String store_address = "";
		String source_register = "";
		if(w.dest instanceof VMemRef.Global) {
			VMemRef.Global global_ref = (VMemRef.Global) w.dest;
			store_address = String.valueOf(global_ref.byteOffset) + "("
									+ global_ref.base.toString() + ")";
			//System.err.println(global_ref.base.toString() + " + " + global_ref.byteOffset + " = " + w.source.toString());
			
		}
		else {
			VMemRef.Stack stack_ref = (VMemRef.Stack) w.dest;
			//System.err.println(stack_ref.region.toString() + String.valueOf(stack_ref.index) + " = " + w.source.toString());
			if (stack_ref.region == VMemRef.Stack.Region.valueOf("Out")) {
				store_address = String.valueOf(stack_ref.index * 4) + "($sp)";
			}
			else if (stack_ref.region == VMemRef.Stack.Region.valueOf("Local")) {
				store_address = String.valueOf(stack_ref.index * 4) + "($sp)";
			}
		}

		if (w.source instanceof VLabelRef) {
			// RHS is a global static label.
			mips_code.add(indent() + "la $v0 " +
								 w.source.toString().substring(1));
			source_register = "$v0";
			// add a la instruction?
		}
		else {
			// Could be a register or an immediate value.
			if(w.source instanceof VVarRef) {
				source_register = w.source.toString();
			}
			else {
				mips_code.add(indent() + "li $v0 " + w.source.toString());
				source_register = "$v0";
			}
		}

		mips_code.add(indent() + "sw " + source_register + " " + store_address);
	}
           
	public void	visit(VReturn r) {
		return;
	}
}