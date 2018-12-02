import cs132.vapor.ast.*;
import java.util.*;

public class VaporMTranslateVisitor extends VInstr.Visitor<java.lang.RuntimeException>{
	LinkedList<String> vaporm_code = new LinkedList<String>();
	HashSet<String> unused_registers = new HashSet<String>();
	HashSet<String> registers_in_use = new HashSet<String>();
	HashMap<String, String> temp_name_to_location;
	int local_counter = 0;
	int indent_counter = 0;

	public VaporMTranslateVisitor(VaporProgram program) {
		for(VDataSegment segment : program.dataSegments) {
			if(segment.mutable) {
				vaporm_code.add(indent() + "var " + segment.ident);
			}
			else {
				vaporm_code.add(indent() + "const " + segment.ident);
			}
			indent_counter += 1;
			for(VOperand function : segment.values) {
				vaporm_code.add(indent() + function.toString());
			}
			indent_counter -= 1;
			// Add an empty line after printing VMT.
			vaporm_code.add("");
		}

		initializeRegisters();
		for(VFunction function : program.functions) {
			// Initialize new map for each function since there are no 
			// shared temp variables between functions.
			temp_name_to_location = new HashMap<String, String>();
			// Reset local counter.
			local_counter = 0;

			// Add function header.
			String stack_info = " [in " + function.params.length + ", out 100, local " + function.vars.length + "]"; 
			vaporm_code.add(indent() + "func " + function.ident + stack_info);

			indent_counter += 1;
			// Add the functions parameters to the temp_name_to_location map.
			int index = 0;
			for(VVarRef.Local param : function.params) {
				temp_name_to_location.put(param.toString(), "in[" + String.valueOf(index) + "]");
				index += 1;
			}

			for(VInstr instruction : function.body) {
				instruction.accept(this);
			}

			indent_counter -= 1;
			// Add empty line after each function.
			vaporm_code.add("");

			// Debug: Remove.
			System.err.println(function.ident);
			for(String temp_name : temp_name_to_location.keySet()) {
				System.err.println(temp_name + " @ " + temp_name_to_location.get(temp_name));
			}
		}
		System.err.println("End of Function");

	}

	/*
	 * Get the VaporMCode as a LinkedList.
	 */
	public LinkedList<String> getVaporMCode() {
		return vaporm_code;
	}

	/*
	 * Return correct indented string.
	 */
	public String indent() {
		String indentedString = "";
		for(int i = 0; i < indent_counter; i++ ) {
			indentedString += " ";
		}
		return indentedString;
	}

	/*
	 * Initalize the set of registers that exist.
	 */
	public void initializeRegisters() {
		for(int i = 0; i < 8; i++) {
			unused_registers.add("$s" + String.valueOf(i));	
		}
		for(int i = 0; i < 9; i++) {
			unused_registers.add("$t" + String.valueOf(i));
		}
		for(int i = 0; i < 4; i++) {
			unused_registers.add("$a" + String.valueOf(i));
		}
	}

	/*
	 * Gets a random available register.
	 */
	public String getAvailableRegister() {
		String available_register = unused_registers.iterator().next();
		unused_registers.remove(available_register);
		registers_in_use.add(available_register);
		return available_register;
	}

	/*
	 *
	 */
	public void freeRegisters() {
		unused_registers.addAll(registers_in_use);
		registers_in_use.clear();
	}

	/*
	 * Gets the location of t.? variable that's stored on the stack if it's already assigned.
	 * Otherwise, assigns it a new location on stack and returns it.
	 */
	public String getTempVarLocationOrCreate(String temp_var) {
		if(! temp_name_to_location.containsKey(temp_var)) {
			String new_location = "local[" + local_counter + "]";
			local_counter += 1;
			temp_name_to_location.put(temp_var, new_location);
		}
		return temp_name_to_location.get(temp_var);
	}

	/*
	 * Gets the location of a local variable, otherwise returns null.
	 */
	public String getTempVarLocation(String temp_var) {
		if(temp_name_to_location.containsKey(temp_var)) {
			return temp_name_to_location.get(temp_var);
		}
		else {
			return null;
		}
	}

	/*
	 * Assigns an operand to a register if it's a local variable, and returns the register.
	 * Returns the Literal as is otherwise.
	 */
	public String assignToRegisterOrGetLiteral(VOperand operand) {
		if(operand instanceof VVarRef.Local) {
			// Local Variable, guaranteed to exist in temp_name_to_location map.
			String location = getTempVarLocation(operand.toString());
			/*if(location == null) {
				System.err.println("Operand hasn't been initialized yet.");
				System.exit(1);
			}*/
			String register = getAvailableRegister();
			vaporm_code.add(indent() + register + " = " + location);
			return register;
		} else if(operand instanceof VLitStr || operand instanceof VLitInt) {
			// Literal String so return as is.
			return operand.toString();
		} 
		else {
			// VOperand.Static, so put value in register, and return register.
			String register = getAvailableRegister();
			vaporm_code.add(indent() + register + " = " + operand.toString());
			return register;
		}
	}

	/*
	 * Assignment Statements in Vapor, of the form:
	 * expr1 = expr2
	 */ 
	@Override
	public void visit(VAssign a) {
		// lhs_location = local[x] where x is some index.
		String lhs_location = getTempVarLocationOrCreate(a.dest.toString());
		String rhs_register_or_val = assignToRegisterOrGetLiteral(a.source);
		vaporm_code.add(indent() + lhs_location + " = " + rhs_register_or_val);
		// Free the register if rhs_register_or_val is a register.
		freeRegisters();
	}

	@Override
	public void visit(VBranch b) {
		String reg_to_branch_on = assignToRegisterOrGetLiteral(b.value);
		if(b.positive) {
			// if branch.
			vaporm_code.add(indent() + "if " + reg_to_branch_on + " goto :" + b.target.ident);
		}
		else {
			// if0 branch.
			vaporm_code.add(indent() + "if0 " + reg_to_branch_on + " goto :" + b.target.ident);
		}
		freeRegisters();
	}

	@Override
	public void visit(VBuiltIn c) {
		String parameter_string = "";
		for(VOperand operand : c.args) {
			String reg_or_val = assignToRegisterOrGetLiteral(operand);
			parameter_string += reg_or_val + " ";
		}
		parameter_string = parameter_string.substring(0, parameter_string.length() - 1);
		if(c.dest == null) {
			// just make the function call.
			vaporm_code.add(indent() + c.op.name + "(" + parameter_string + ")");
		}
		else {
			String dest_register = getAvailableRegister();
			String location = getTempVarLocationOrCreate(c.dest.toString());
			vaporm_code.add(indent() + dest_register + " = " + c.op.name + "(" + parameter_string + ")");
			vaporm_code.add(indent() + location + " = " + dest_register);
		}
		freeRegisters();
	}

	@Override
	public void visit(VCall c) {
		// Put arguments in out array.
	}

	@Override
	public void visit(VGoto g) {
		vaporm_code.add(indent() + "goto " + g.target.toString());
	}

	@Override
	public void visit(VMemRead r) {
		
	}

	@Override
	public void visit(VMemWrite w) {
		
	}

	/*
	 * Store the return value in $v0.
	 */
	@Override
	public void visit(VReturn r) {
		
	} 
}