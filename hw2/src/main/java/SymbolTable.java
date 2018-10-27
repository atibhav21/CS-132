import syntaxtree.*;
import java.util.*;

class FunctionSymbolTable {
	public HashMap<String, Type> local_variables;
	public HashMap<String, Type> formal_parameters;
	public Type return_type;

	public FunctionSymbolTable(Type return_type) {
		this.local_variables = new HashMap<String, Type>();
		this.formal_parameters = new HashMap<String, Type>();
		this.return_type = return_type;
	}

	public boolean addLocalVariable(String variable_name, Type type) {
		if(local_variables.containsKey(variable_name)) {
			return false;
		}
		local_variables.put(variable_name, type);
		return true;
	}

	public Type getLocalVariableType(String variable_name) {
		if (local_variables.containsKey(variable_name)) {
			return local_variables.get(variable_name);
		}
		return null;
	}

	public boolean addFormalParameter(String parameter_name, Type type) {
		if(formal_parameters.containsKey(parameter_name)) {
			return false;
		}
		formal_parameters.put(parameter_name, type);
		return true;
	}

	public Type getFormalParameterType(String parameter_name) {
		if(formal_parameters.containsKey(parameter_name)) {
			return formal_parameters.get(parameter_name);
		}
		return null;
	}

	public Type getFunctionReturnType() {
		return this.return_type;
	}

	public void print() {
		System.out.println("~~~~~~~~~~~~~~ FUNCTION SYMBOL TABLE ~~~~~~~~~~~~~");
		System.out.println("Parameters");
		for (String parameter : this.formal_parameters.keySet()) {
			System.out.println(parameter);
		}
		System.out.println("Local Variables");
		for (String local_var : this.local_variables.keySet()) {
			System.out.println(local_var);
		}
	}
}

class ClassSymbolTable {
	public HashMap<String, Type> field_name_to_type;
	public HashMap<String, FunctionSymbolTable> function_name_to_table;

	public ClassSymbolTable() {
		field_name_to_type = new HashMap<String, Type>();
		function_name_to_table = new HashMap<String, FunctionSymbolTable>();
	}

	public boolean addField(String variable_name, Type type) {
		if(field_name_to_type.containsKey(variable_name)) {
			return false;
		}
		field_name_to_type.put(variable_name, type);
		return true;
	}

	public Type getFieldType(String variable_name) {
		if (field_name_to_type.containsKey(variable_name)) {
			return field_name_to_type.get(variable_name);
		}
		return null;
	}

	public boolean addFunction(String function_name, Type return_type) {
		if (function_name_to_table.containsKey(function_name)) {
			return false;
		}
		function_name_to_table.put(function_name, new FunctionSymbolTable(return_type));
		return true;
	}

	public FunctionSymbolTable getFunctionSymbolTable(String function_name) {
		if (function_name_to_table.containsKey(function_name)) {
			return function_name_to_table.get(function_name);
		}
		return null;
	}

	public void print() {
		System.out.println("~~~~~~~~~~~~~~ CLASS SYMBOL TABLE ~~~~~~~~~~~~~");
		System.out.println("Variables");
		for(String key: field_name_to_type.keySet()) {
			System.out.println(key);
		}
		System.out.println("Functions");
		for(String key: function_name_to_table.keySet()) {
			System.out.println(key);
			// TODO: Print Function Symbol Table
			function_name_to_table.get(key).print();
		}
	}
}

public class SymbolTable {
	public HashMap<String, ClassSymbolTable> class_name_to_symbol_table;
	
	public SymbolTable() {
		class_name_to_symbol_table = new HashMap<String, ClassSymbolTable>();
	}

	public boolean addClass(String class_identifier) {
		if (class_name_to_symbol_table.containsKey(class_identifier)) {
			return false;
		}
		class_name_to_symbol_table.put(class_identifier, new ClassSymbolTable());
		return true;
	}

	public ClassSymbolTable getClassSymbolTable(String class_identifier) {
		if (class_name_to_symbol_table.containsKey(class_identifier)) {
			return class_name_to_symbol_table.get(class_identifier);
		}
		return null;
	}

	public HashMap<String, ClassSymbolTable> getSymbolTable() {
		return class_name_to_symbol_table;
	}

	public void print() {
		System.out.println("~~~~~~~~~~~~~~~ GLOBAL SYMBOL TABLE ~~~~~~~~~~~~~~~~~");
		for(String key: class_name_to_symbol_table.keySet()) {
			System.out.println(key);
			class_name_to_symbol_table.get(key).print();
		}
	}
}