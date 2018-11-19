import syntaxtree.*;
import visitor.*;
import java.util.*;


public class VaporVisitor extends GJNoArguDepthFirst<String> {
	GlobalSymbolTable gst;
	LinkedList<String> vapor_code;
	int indent_counter;
	int variable_counter;
	int label_counter;
	String class_name;
	String function_name;
  String latest_class_type;
  boolean arrayAllocated;

	VaporVisitor(GlobalSymbolTable gst) {
		this.gst = gst;
		vapor_code = new LinkedList<String>();
		indent_counter = 0;
		variable_counter = 0;
		label_counter = 0;
    latest_class_type = null;
    arrayAllocated = false;
	}

  public LinkedList<String> getVaporCode() {
    return vapor_code;
  }

   private String indent() {
   	String indentation = "";
   	for(int i = 0; i < 2 * indent_counter; i++) {
   		indentation += " ";
   	}
   	return indentation;
   }

   private String getNextVariableName() {
   	String variable_name = "t." + String.valueOf(variable_counter);
   	variable_counter += 1;
   	return variable_name;
   }

   private void resetVariableCounter() {
   	variable_counter = 0;
   }

   private String getNextLabel() {
   	String label = "line_" + String.valueOf(label_counter);
   	label_counter += 1;
   	return label;
   }

   private void generateNullPointerCheck(String array_variable_name) {
   	  String next_label = getNextLabel();
      String array_index = checkSyntax(array_variable_name);
      vapor_code.add(indent() + "if " + array_index + " goto :" + next_label);
      indent_counter++;
      vapor_code.add(indent() + "Error(\"null pointer\")");
      indent_counter--;
      vapor_code.add(indent() + next_label + ":");
   }

   private void generateOutOfBoundsCheck(String array_variable_name, String index_accessed) {
   	String next_label = getNextLabel();
   	String length_variable = getNextVariableName();
    String array_index = checkSyntax(array_variable_name);
   	vapor_code.add(indent() + length_variable + " = [" + array_index + "]");
   	String comparison_result = getNextVariableName();
   	vapor_code.add(indent() + comparison_result + " = Lt(" + index_accessed + " " + length_variable + ")");
   	vapor_code.add(indent() + "if " + comparison_result + " goto :" + next_label);
   	indent_counter++;
   	vapor_code.add(indent() + "Error(\"array index out of bounds\")");
   	indent_counter--;
   	vapor_code.add(indent() + next_label + ":");
   }
	
   private String checkSyntax(String variable) {
    String new_var = variable;
    if(variable.indexOf(']') >= 0) {
      new_var = getNextVariableName();
      vapor_code.add(indent() + new_var + " = " + variable);
    }
    return new_var;
   }

	/**
    * f0 -> MainClass()
    * f1 -> ( TypeDeclaration() )*
    * f2 -> <EOF>
    */
   public String visit(Goal n) {
      n.f0.accept(this);
      n.f1.accept(this);
      if (arrayAllocated) {
        vapor_code.add(indent() + "func AllocArray(size)");
        indent_counter += 1;
        vapor_code.add(indent() + "bytes = MulS(size 4)");
        vapor_code.add(indent() + "bytes = Add(bytes 4)");
        vapor_code.add(indent() + "v = HeapAllocZ(bytes)");
        vapor_code.add(indent() + "[v] = size");
        vapor_code.add(indent() + "ret v");
      }
      indent_counter -= 1;
      return null;
   }
   /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> "public"
    * f4 -> "static"
    * f5 -> "void"
    * f6 -> "main"
    * f7 -> "("
    * f8 -> "String"
    * f9 -> "["
    * f10 -> "]"
    * f11 -> Identifier()
    * f12 -> ")"
    * f13 -> "{"
    * f14 -> ( VarDeclaration() )*
    * f15 -> ( Statement() )*
    * f16 -> "}"
    * f17 -> "}"
    */
   public String visit(MainClass n) {
   	  vapor_code.add("func Main()");
   	  indent_counter += 1;
   	  class_name = n.f1.f0.toString();
   	  function_name = n.f6.toString();
      n.f15.accept(this);
      vapor_code.add(indent() + "ret");
      function_name = null;
      class_name = null;
      indent_counter -= 1;
      return null;
   }

   /**
    * f0 -> ClassDeclaration()
    *       | ClassExtendsDeclaration()
    */
   public String visit(TypeDeclaration n) {
      n.f0.accept(this);
      return null;
   }

   /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> ( VarDeclaration() )*
    * f4 -> ( MethodDeclaration() )*
    * f5 -> "}"
    */
   public String visit(ClassDeclaration n) {
   	  class_name = n.f1.f0.toString();
      n.f4.accept(this);
      class_name = null;
      return null;
   }

   /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "extends"
    * f3 -> Identifier()
    * f4 -> "{"
    * f5 -> ( VarDeclaration() )*
    * f6 -> ( MethodDeclaration() )*
    * f7 -> "}"
    */
   public String visit(ClassExtendsDeclaration n) {
      class_name = n.f1.f0.toString();
      n.f6.accept(this);
      class_name = null;
      return null;
   }

   /**
    * f0 -> "public"
    * f1 -> Type()
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( FormalParameterList() )?
    * f5 -> ")"
    * f6 -> "{"
    * f7 -> ( VarDeclaration() )*
    * f8 -> ( Statement() )*
    * f9 -> "return"
    * f10 -> Expression()
    * f11 -> ";"
    * f12 -> "}"
    */
   public String visit(MethodDeclaration n) {
   	  function_name = n.f2.f0.toString();
      vapor_code.add(indent() + "func " + class_name + "." + gst.getClassSymbolTable(class_name).getFunctionSymbolTable(function_name).getFunctionParameterList());
      resetVariableCounter();
      indent_counter++;
      n.f8.accept(this);
      String return_value = n.f10.accept(this);
      String next_variable = getNextVariableName();
      vapor_code.add(indent() + next_variable + " = " + return_value);
      vapor_code.add(indent() + "ret " + next_variable);
      function_name = null;
      indent_counter--;
      return null;
   }

   /**
    * f0 -> Block()
    *       | AssignmentStatement()
    *       | ArrayAssignmentStatement()
    *       | IfStatement()
    *       | WhileStatement()
    *       | PrintStatement()
    */
   public String visit(Statement n) {
      n.f0.accept(this);
      return null;
   }

   /**
    * f0 -> "{"
    * f1 -> ( Statement() )*
    * f2 -> "}"
    */
   public String visit(Block n) {
      n.f1.accept(this);
      return null;
   }

   /**
    * f0 -> Identifier()
    * f1 -> "="
    * f2 -> Expression()
    * f3 -> ";"
    */
   public String visit(AssignmentStatement n) {
      String identifier = n.f0.accept(this);
      String rhs_expression = n.f2.accept(this);
      vapor_code.add(indent() + identifier + " = " + rhs_expression);
      return null;
   }

   /**
    * f0 -> Identifier()
    * f1 -> "["
    * f2 -> Expression()
    * f3 -> "]"
    * f4 -> "="
    * f5 -> Expression()
    * f6 -> ";"
    */
   public String visit(ArrayAssignmentStatement n) {
      String array_address = n.f0.accept(this);
      String variable_name = getNextVariableName();
      vapor_code.add(indent() + variable_name + " = " + array_address);
      generateNullPointerCheck(variable_name);
	    String index_accessed = n.f2.accept(this);
      generateOutOfBoundsCheck(variable_name, index_accessed);
      String array_index_pointer = getNextVariableName();
      vapor_code.add(indent() + array_index_pointer + " = MulS(" + index_accessed + " 4)");
      vapor_code.add(indent() + array_index_pointer + " = Add(" + array_index_pointer + " " + variable_name + ")");
      String rhs = n.f5.accept(this);
      vapor_code.add(indent() + "[" + array_index_pointer + "+4] = " + rhs);
      return null;
   }

   /**
    * f0 -> "if"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    * f5 -> "else"
    * f6 -> Statement()
    */
   public String visit(IfStatement n) {
      String var_for_condition = checkSyntax(n.f2.accept(this));
      String next_label = getNextLabel();
      String end_label = getNextLabel();
      vapor_code.add(indent() + "if0 " + var_for_condition + " goto :" + next_label);
      indent_counter++;
      n.f4.accept(this);
      vapor_code.add(indent() + "goto :" + end_label);
      indent_counter--;
      vapor_code.add(indent() + next_label + ":");
      n.f6.accept(this);
      vapor_code.add(indent() + end_label + ":");
      return null;
   }

   /**
    * f0 -> "while"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    */
   public String visit(WhileStatement n) {
      String while_loop_start_label = getNextLabel();
      String while_loop_end_label = getNextLabel();
      vapor_code.add(indent() + while_loop_start_label + ":");
      String loop_condition_var = n.f2.accept(this);
      vapor_code.add(indent() + "if0 " + loop_condition_var + " goto :" + while_loop_end_label);
      indent_counter++;
 	  n.f4.accept(this);
 	  indent_counter--;
 	  vapor_code.add(indent() + "goto :" + while_loop_start_label);
 	  vapor_code.add(indent() + while_loop_end_label + ":");
      return null;
   }

   /**
    * f0 -> "System.out.println"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> ";"
    */
   public String visit(PrintStatement n) {
      String var_to_print = n.f2.accept(this);
      vapor_code.add(indent() + "PrintIntS(" + var_to_print + ")");
      return null;
   }

   /**
    * f0 -> AndExpression()
    *       | CompareExpression()
    *       | PlusExpression()
    *       | MinusExpression()
    *       | TimesExpression()
    *       | ArrayLookup()
    *       | ArrayLength()
    *       | MessageSend()
    *       | PrimaryExpression()
    */
   public String visit(Expression n) {
      return n.f0.accept(this);
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "&&"
    * f2 -> PrimaryExpression()
    */
   public String visit(AndExpression n) {
      String first_operand = n.f0.accept(this);
      String second_operand = n.f2.accept(this);
      first_operand = checkSyntax(first_operand);
      second_operand = checkSyntax(second_operand);
      String assignment_variable = getNextVariableName();
      vapor_code.add(indent() + assignment_variable + " = MulS(" + first_operand + " " + second_operand + ")");
      return assignment_variable;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "<"
    * f2 -> PrimaryExpression()
    */
   public String visit(CompareExpression n) {
      String first_var = checkSyntax(n.f0.accept(this));
      String second_var = checkSyntax(n.f2.accept(this));
      String var_with_comparison_result = getNextVariableName();
      vapor_code.add(indent() + var_with_comparison_result + " = " + "LtS(" + first_var + " " + second_var + ")");
      return var_with_comparison_result;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "+"
    * f2 -> PrimaryExpression()
    */
   public String visit(PlusExpression n) {
      String first_operand = checkSyntax(n.f0.accept(this));
      String second_operand = checkSyntax(n.f2.accept(this));
      String var_with_add_result = getNextVariableName();
      vapor_code.add(indent() + var_with_add_result + " = " + "Add(" + first_operand + " " + second_operand + ")");
      return var_with_add_result;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "-"
    * f2 -> PrimaryExpression()
    */
   public String visit(MinusExpression n) {
      String first_operand = checkSyntax(n.f0.accept(this));
      String second_operand = checkSyntax(n.f2.accept(this));
      String var_with_subtract_result = getNextVariableName();
      vapor_code.add(indent() + var_with_subtract_result + " = " + "Sub(" + first_operand + " " + second_operand + ")");
      return var_with_subtract_result;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "*"
    * f2 -> PrimaryExpression()
    */
   public String visit(TimesExpression n) {
      String first_operand = checkSyntax(n.f0.accept(this));
      String second_operand = checkSyntax(n.f2.accept(this));
      String var_with_mult_result = getNextVariableName();
      vapor_code.add(indent() + var_with_mult_result + " = " + "MulS(" + first_operand + " " + second_operand + ")");
      return var_with_mult_result;	
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "["
    * f2 -> PrimaryExpression()
    * f3 -> "]"
    */
   public String visit(ArrayLookup n) {
      String array_identifier = checkSyntax(n.f0.accept(this));
      generateNullPointerCheck(array_identifier);
      String index_accessed = n.f2.accept(this);
      generateOutOfBoundsCheck(array_identifier, index_accessed);
      String variable_with_result = getNextVariableName();
      //String array_pointer_temp = getNextVariableName();
      vapor_code.add(indent() + variable_with_result + " = MulS(" + index_accessed + " 4)");
      vapor_code.add(indent() + variable_with_result + " = Add(" + variable_with_result + " " + array_identifier + ")");
      vapor_code.add(indent() + variable_with_result + " = [" + variable_with_result + "+ 4]");
      return variable_with_result;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> "length"
    */
   public String visit(ArrayLength n) {
      String array_identifier = n.f0.accept(this);
      String array_pointer_temp = getNextVariableName();
      String length_temp = getNextVariableName();
      vapor_code.add(indent() + array_pointer_temp + " = " + array_identifier);
      generateNullPointerCheck(array_pointer_temp);
      vapor_code.add(indent() + length_temp + " = [" + array_pointer_temp + "]");
      return length_temp;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( ExpressionList() )?
    * f5 -> ")"
    */
   public String visit(MessageSend n) {
      String temp_variable = n.f0.accept(this);   
      String offset = String.valueOf(gst.getClassSymbolTable(latest_class_type).getMemberFunctionOffset(n.f2.f0.toString()));
      
      String variable_with_call_address = getNextVariableName();
      vapor_code.add(indent() + variable_with_call_address + " = [" + temp_variable + "]");
      vapor_code.add(indent() + variable_with_call_address + " = [" + variable_with_call_address + " + " + offset + "]"); 
      String parameter_list = n.f4.accept(this);
      variable_with_call_address = checkSyntax(variable_with_call_address);
      String return_val = getNextVariableName();
      if(parameter_list == null) {
        vapor_code.add(indent() + return_val + " = call " + variable_with_call_address + "(" + temp_variable + ")");
      }
      else {
        vapor_code.add(indent() + return_val + " = call " + variable_with_call_address + "(" + temp_variable + " " + parameter_list + ")");
      }
      return return_val;
   }

   /**
    * f0 -> Expression()
    * f1 -> ( ExpressionRest() )*
    */
   public String visit(ExpressionList n) {
      String parameter_list = checkSyntax(n.f0.accept(this));

      for(Node parameter : n.f1.nodes) {
        parameter_list += " " + checkSyntax(parameter.accept(this));
      }
      return parameter_list;
   }

   /**
    * f0 -> ","
    * f1 -> Expression()
    */
   public String visit(ExpressionRest n) {
      return n.f1.accept(this);
   }

   /**
    * f0 -> IntegerLiteral()
    *       | TrueLiteral()
    *       | FalseLiteral()
    *       | Identifier()
    *       | ThisExpression()
    *       | ArrayAllocationExpression()
    *       | AllocationExpression()
    *       | NotExpression()
    *       | BracketExpression()
    */
   public String visit(PrimaryExpression n) {
      return n.f0.accept(this);
   }

   /**
    * f0 -> <INTEGER_LITERAL>
    */
   public String visit(IntegerLiteral n) {
      return n.f0.toString();
   }

   /**
    * f0 -> "true"
    */
   public String visit(TrueLiteral n) {
      return "1";
   }

   /**
    * f0 -> "false"
    */
   public String visit(FalseLiteral n) {
       return "0";
   }

   /**
    * f0 -> <IDENTIFIER>
    */
   public String visit(Identifier n) {
      String identifier_name = n.f0.toString();
      FunctionSymbolTable fst = gst.getClassSymbolTable(class_name).getFunctionSymbolTable(function_name);
      if (fst.isLocalVariable(identifier_name)) {
      	latest_class_type = fst.getLocalVariableType(identifier_name);
        return identifier_name;
      }
      else {
      	// Member variable so get the offset from the Symbol Table.
        ClassSymbolTable cst = gst.getClassSymbolTable(class_name);
        latest_class_type = cst.getMemberVariableType(identifier_name);
        String offset = String.valueOf(cst.getMemberVariableOffset(identifier_name));
        return "[this + " + offset.toString() + "]";
      }
   }

   /**
    * f0 -> "this"
    */
   public String visit(ThisExpression n) {
      latest_class_type = class_name;
      return "this";
   }

   /**
    * f0 -> "new"
    * f1 -> "int"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"
    */
   public String visit(ArrayAllocationExpression n) {
      arrayAllocated = true;
      String variable = getNextVariableName();
      String size_variable = n.f3.accept(this);
      vapor_code.add(indent() + variable + " = call :AllocArray(" + size_variable + ")");
      return variable;
   }

   /**
    * f0 -> "new"
    * f1 -> Identifier()
    * f2 -> "("
    * f3 -> ")"
    */
   public String visit(AllocationExpression n) {
      latest_class_type = n.f1.f0.toString();
      String variable = getNextVariableName();
      int sizeToAllocate = gst.getClassSymbolTable(n.f1.f0.toString()).getClassSize();
      vapor_code.add(indent() + variable + " = HeapAllocZ(" + sizeToAllocate + ")");
      vapor_code.add(indent() + "[" + variable + "] = :vmt_" + n.f1.f0.toString());
      generateNullPointerCheck(variable);
      return variable;
   }

   /**
    * f0 -> "!"
    * f1 -> Expression()
    */
   public String visit(NotExpression n) {
      String expression_result = n.f1.accept(this);
      String variable = getNextVariableName();
      vapor_code.add(indent() + variable + " = Sub (1 " + expression_result + ")");
      return variable;
   }

   /**
    * f0 -> "("
    * f1 -> Expression()
    * f2 -> ")"
    */
   public String visit(BracketExpression n) {
      return n.f1.accept(this);
   }

}
