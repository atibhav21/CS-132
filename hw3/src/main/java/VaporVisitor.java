import syntaxtree.*;
import visitor.*;


public class VaporVisitor extends GJNoArguDepthFirst<String> {
	GlobalSymbolTable gst;
	List<String> vapor_code;
	int indent_counter;
	int variable_counter;
	int label_counter;
	String class_name;
	String function_name;

	VaporVisitor(GlobalSymbolTable gst) {
		this.gst = gst;
		vapor_code = new List<String>();
		indent_counter = 0;
		variable_counter = 0;
		label_counter = 0;
	}

	/**
    * f0 -> MainClass()
    * f1 -> ( TypeDeclaration() )*
    * f2 -> <EOF>
    */
   public String visit(Goal n) {
      n.f0.accept(this);
      n.f1.accept(this);
      return null;
   }

   private String indent() {
   	String indentation = "";
   	for(int i = 0; i < 2 * indent_counter; i++) {
   		indentation += " ";
   	}
   	return indentation;
   }

   private String getNextVariableName() {
   	String variable_name = "t." + variable_counter.toString();
   	variable_counter += 1;
   	return variable_name;
   }

   private void resetVariableCounter() {
   	variable_counter = 0;
   }

   private String getNextLabel() {
   	String label = "line_" + label_counter.toString();
   	label_counter += 1;
   	return label
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
      // TODO: Check if I need to do something other than this for Inheritance.
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
   public R visit(MethodDeclaration n) {
      R _ret=null;
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      n.f3.accept(this);
      n.f4.accept(this);
      n.f5.accept(this);
      n.f6.accept(this);
      n.f7.accept(this);
      n.f8.accept(this);
      n.f9.accept(this);
      n.f10.accept(this);
      n.f11.accept(this);
      n.f12.accept(this);
      return _ret;
   }

   // TODO: Check if need to override Formal Parameter visitors.

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
      // TODO: Check if need to indent for block.
      n.f1.accept(this);
      return null;
   }

   /**
    * f0 -> Identifier()
    * f1 -> "="
    * f2 -> Expression()
    * f3 -> ";"
    */
   public R visit(AssignmentStatement n) {
      R _ret=null;
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      n.f3.accept(this);
      return _ret;
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
   public R visit(ArrayAssignmentStatement n) {
      R _ret=null;
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      n.f3.accept(this);
      n.f4.accept(this);
      n.f5.accept(this);
      n.f6.accept(this);
      return _ret;
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
   public R visit(IfStatement n) {
      String var_for_condition = n.f2.accept(this);
      
      n.f3.accept(this);
      n.f4.accept(this);
      n.f5.accept(this);
      n.f6.accept(this);
      return _ret;
   }

   /**
    * f0 -> "while"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    */
   public R visit(WhileStatement n) {
      R _ret=null;
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      n.f3.accept(this);
      n.f4.accept(this);
      return _ret;
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
   public R visit(AndExpression n) {
   	 // TODO: Do using Mult
      String var1 = n.f0.accept(this);
      String var2 = n.f2.accept(this);
      String assignment_variable = getNextVariableName();
      vapor_code.add(indent() + assignment_variable " = Sub(" + var1 + " " + var2 ")");
      return assignment_variable;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "<"
    * f2 -> PrimaryExpression()
    */
   public String visit(CompareExpression n) {
      String first_var = n.f0.accept(this);
      String second_var = n.f2.accept(this);
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
      String first_operand = n.f0.accept(this);
      String second_operand = n.f2.accept(this);
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
      String first_operand = n.f0.accept(this);
      String second_operand = n.f2.accept(this);
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
      String first_operand = n.f0.accept(this);
      String second_operand = n.f2.accept(this);
      String var_with_mult_result = getNextVariableName();
      vapor_code.add(indent() + var_with_subtract_result + " = " + "MulS(" + first_operand + " " + second_operand + ")");
      return var_with_subtract_result;	
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "["
    * f2 -> PrimaryExpression()
    * f3 -> "]"
    */
   public R visit(ArrayLookup n) {
      R _ret=null;
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      n.f3.accept(this);
      return _ret;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> "length"
    */
   public R visit(ArrayLength n) {
      R _ret=null;
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      return _ret;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( ExpressionList() )?
    * f5 -> ")"
    */
   public R visit(MessageSend n) {
      R _ret=null;
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      n.f3.accept(this);
      n.f4.accept(this);
      n.f5.accept(this);
      return _ret;
   }

   /**
    * f0 -> Expression()
    * f1 -> ( ExpressionRest() )*
    */
   public R visit(ExpressionList n) {
      R _ret=null;
      n.f0.accept(this);
      n.f1.accept(this);
      return _ret;
   }

   /**
    * f0 -> ","
    * f1 -> Expression()
    */
   public R visit(ExpressionRest n) {
      R _ret=null;
      n.f0.accept(this);
      n.f1.accept(this);
      return _ret;
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
   public R visit(PrimaryExpression n) {
      R _ret=null;
      n.f0.accept(this);
      return _ret;
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
   public R visit(Identifier n) {
      R _ret=null;
      n.f0.accept(this);
      return _ret;
   }

   /**
    * f0 -> "this"
    */
   public R visit(ThisExpression n) {
      R _ret=null;
      n.f0.accept(this);
      return _ret;
   }

   /**
    * f0 -> "new"
    * f1 -> "int"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"
    */
   public R visit(ArrayAllocationExpression n) {
      R _ret=null;
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      n.f3.accept(this);
      n.f4.accept(this);
      return _ret;
   }

   /**
    * f0 -> "new"
    * f1 -> Identifier()
    * f2 -> "("
    * f3 -> ")"
    */
   public String visit(AllocationExpression n) {
      String variable = getNextVariableName();
      int sizeToAllocate = gst.getClassSymbolTable(n.f1.f0.toString()).getClassSize();
      vapor_code.add(indent() + variable + " = HeapAllocZ(" + sizeToAllocate + ")");
      vapor_code.add(indent() + "[" + variable + "] = :vmt_" n.f1.f0.toString());
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
      return vavriable;
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
