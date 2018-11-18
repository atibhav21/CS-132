import syntaxtree.*;
import java.util.*;

class FunctionSymbolTable {
	public String function_name;
	public HashMap<String, String> local_variables;
	public LinkedHashMap<String, String> formal_parameters;
	public String return_type;

	public FunctionSymbolTable(String function_name, String return_type) {
		this.local_variables = new HashMap<String, String>();
		this.formal_parameters = new LinkedHashMap<String, String>();
		this.return_type = return_type;
		this.function_name = function_name;
	}

	public boolean isLocalVariable(variable_name) {
		return local_variables.containsKey(variable_name) || formal_parameters.containsKey(variable_name);
	}

	public void addLocalVariable(String variable_name, String type) {
		local_variables.put(variable_name, type);
	}

	public String getLocalVariableType(String variable_name) {
		return local_variables.get(variable_name);
	}

	public void addFormalParameter(String parameter_name, String type) {
		formal_parameters.put(parameter_name, type);
	}

	public String getFormalParameterType(String parameter_name) {
		return formal_parameters.get(parameter_name);
	}

	public String getFunctionParameterList() {
		function_header = function_name + "(this";
		for (String parameter : formal_parameters) {
			function_header += " " + parameter;
		}
		function_header += ")";
		return function_header;
	}
}

class ClassSymbolTable {
	/* Offset:
	 * 0 -> VMT pointer
	 * 4 onwards -> Member Variables
	 */
	public HashMap<String, FunctionSymbolTable> function_symbol_table = new HashMap<String, FunctionSymbolTable>();
	public HashMap<String, String> member_variables = new HashMap<String, String>();
	public HashMap<String, Integer> member_function_to_offset = new HashMap<String, Integer>();
	public HashMap<String, Integer> member_variable_to_offset = new HashMap<String, Integer>();
	public String parent_class = null;
	public String class_name;
	public int current_function_offset = 0;
	public int current_variable_offset = 4;

	ClassSymbolTable(String class_name, String parent_class) {
		this.class_name = class_name;
		this.parent_class = parent_class;
	}

	public void addMemberVariable(String variable_name, String type) {
		member_variables.put(variable_name, type);
		member_variable_to_offset.put(variable_name, new Integer(current_variable_offset));
		current_variable_offset += 4;
	}

	public void addMemberFunction(String function_name, String return_type) {
		member_function_to_offset.put(function_name, new Integer(current_function_offset));
		function_symbol_table.put(function_name, new FunctionSymbolTable(function_name, return_type));
		current_function_offset += 4;
	}

	public String getMemberVariableType(String variable_name) {
		return member_variables.get(variable_name);
	}

	public FunctionSymbolTable getFunctionSymbolTable(String function_name) {
		return function_symbol_table.get(function_name);
	}

	public int getClassSize() {
		// Class Size is the number of member variables + 1 (for VMT table pointer) * 4 bytes for each
		return (this.member_variables.size() + 1) * 4;
	}

	public int getMemberVariableOffset(String variable_name) {
		return member_variable_to_offset.get(variable_name);
	}

	public int getMemberFunctionOffset(String function_name) {
		return member_function_to_offset.get(function_name);
	}
}

public class GlobalSymbolTable {
	public HashMap<String, ClassSymbolTable> global_symbol_table = new HashMap<String, ClassSymbolTable>();

	public void addClass(String class_name, String parent_class) {
		// No need to check if class is already defined, since program type checks.
		global_symbol_table.put(class_name, new ClassSymbolTable(class_name, parent_class));
	}

	public ClassSymbolTable getClassSymbolTable(String class_name) {
		// No need to do error checking since program type checks.
		return global_symbol_table.get(class_name);
	}
}