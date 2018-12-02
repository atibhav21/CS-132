import cs132.vapor.ast.*;
import java.util.*;

public class VaporMTranslator extends VInstr.Visitor<java.lang.RuntimeException> {
	private int indent_counter = 0;
	private LinkedList<String> vaporm_code;
	private HashMap<String, String> var_name_to_location;
	private HashSet<String> unused_registers;
	private int local_counter = 0;

	public VaporMTranslator(VaporProgram program) {
		vaporm_code = new LinkedList<String>();
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

		for(VFunction function : program.functions) {
			var_name_to_location = new HashMap<String, String>();
			unused_registers = new HashSet<String>();
			initializeRegisters();

			local_counter = 0;
			// Add function header.
			String stack_info = "[in " + function.params.length + ", out 100, local " + function.vars.length + "]"; 
			vaporm_code.add(indent() + "func " + function.ident + " " + stack_info);

			indent_counter += 1;
			// Add the functions parameters to the var_name_to_location map.
			int index = 0;
			for(VVarRef.Local param : function.params) {
				var_name_to_location.put(param.toString(), "in[" + String.valueOf(index) + "]");
				index += 1;
			}

			for(VInstr instruction : function.body) {
				instruction.accept(this);
			}

			indent_counter -= 1;
			// Add empty line after each function.
			vaporm_code.add("");
		}
	}

	public void initializeRegisters() {
		for(int i = 0; i < 8; i++) {
			unused_registers.add("s" + String.valueOf(i));	
		}
		for(int i = 0; i < 9; i++) {
			unused_registers.add("t" + String.valueOf(i));
		}
		for(int i = 0; i < 4; i++) {
			unused_registers.add("a" + String.valueOf(i));
		}
		unused_registers.add("v0");
		
	}

	public String indent() {
		String indentedString = "";
		for(int i = 0; i < indent_counter; i++ ) {
			indentedString += "  ";
		}
		return indentedString;
	}

	public LinkedList<String> getVaporMCode() {
		return vaporm_code;
	}

	public String getAvailableRegister() {
		String available_register = unused_registers.iterator().next();
		unused_registers.remove(available_register);
		return "$" + available_register;
	}

	public void freeRegister(String reg) {
		unused_registers.add(reg);
	}

	public String getLocalIndex(String var_name) {
		if(! var_name_to_location.containsKey(var_name)) {
			return null;
		}
		return var_name_to_location.get(var_name);
	}

	public String getOperandInfo(VOperand operand) {
		if(operand instanceof VVarRef.Local) {
			// Local variable, so get location using the name.
			return getLocalIndex(operand.toString());
		}
		// Literal, so return value.
		return operand.toString();
	}

	public String getNextLocal() {
		String next_local = "local[" + String.valueOf(local_counter) + "]";
		local_counter ++;
		return next_local;
	}

	@Override
	public void visit(VAssign a) {
		String lhs_location = getLocalIndex(a.dest.toString());

		String rhs = getOperandInfo(a.source);
		if (lhs_location == null) {
			// lhs variable doesn't exist so create a local var for lhs.
			String next_local = getNextLocal();
			var_name_to_location.put(a.dest.toString(), next_local);
			String rhs_register = getAvailableRegister();
			vaporm_code.add(indent() + rhs_register + " = " + rhs);
			vaporm_code.add(indent() + next_local + " = " + rhs_register);
		}
		else {
			// lhs local variable exists, so fetch from stack and put in a reg.
			String lhs_register = getAvailableRegister();
			String rhs_register = getAvailableRegister();
			vaporm_code.add(indent() + lhs_register + " = " + lhs_location);
			vaporm_code.add(indent() + rhs_register + " = " + rhs);
			vaporm_code.add(indent() + lhs_register + " = " + rhs_register);
			vaporm_code.add(indent() + lhs_location + " = " + lhs_register);
			freeRegister(lhs_register);
			freeRegister(rhs_register);
		}
		
		
		
	}

	@Override
	public void visit(VBranch b) {
		// Value branched on should already be in the map, since it has been assigned
		// in a previous step.
		String value_location = getOperandInfo(b.value);
		String reg = getAvailableRegister();
		vaporm_code.add(indent() + reg + " = " + value_location);
		if(b.positive) {
			// if branch.
			vaporm_code.add(indent() + "if " + reg + " goto :" + b.target.ident);
		}
		else {
			// if0 branch.
			vaporm_code.add(indent() + "if0 " + reg + " goto :" + b.target.ident);
		}
	}

	@Override
	public void visit(VBuiltIn c) {
		if(c.dest == null) {
			// No assignment, just a built in call.

		}
		else {
			HashSet<String> registers_used = new HashSet<String>();
			String parameters_string = "";
			for(VOperand operand : c.args) {
				// Move each parameter into a register.
				String param_location = getOperandInfo(operand);
				String register = getAvailableRegister();
				vaporm_code.add(indent() + register + " = " + param_location);
				registers_used.add(register);
				parameters_string += register + " ";
			}
			// Strip the last space, will always exist since every call will have atleast 1 parameter.
			parameters_string = parameters_string.substring(0, parameters_string.length() - 1);

			String dest_register = getAvailableRegister();
			vaporm_code.add(indent() + dest_register + " = " + c.op.name + "(" + parameters_string + ")" );

			// Store the result in the destination location.
			String store_location = getLocalIndex(c.dest.toString());
			if(store_location == null) {
				// Variable used doesn't exist yet, so create it.
				store_location = getNextLocal();
				var_name_to_location.put(c.dest.toString(), store_location);
			}

			vaporm_code.add(indent() + store_location + " = " + dest_register);

			freeRegister(dest_register);
			for(Iterator<String> it = registers_used.iterator(); it.hasNext();) {
				freeRegister(it.next());
				it.remove();
			}
		}
	}

	@Override
	public void visit(VCall c) {
		String reg_to_transfer = getAvailableRegister();
		for(VOperand argument : c.args) {
			String argument_info = getOperandInfo(argument);
		}
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

	public void visit(VReturn r) {
		
	} 
} 