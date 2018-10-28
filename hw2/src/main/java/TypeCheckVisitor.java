import syntaxtree.*;
import visitor.*;
import java.util.*;

public class TypeCheckVisitor extends GJNoArguDepthFirst<String> {
	SymbolTable symbol_table;
	String class_name;
	String function_name;
	final String boolean_type = "Boolean";
	final String integer_type = "Integer";
	final String array_type = "Array";
	LinkedHashMap<String, String> function_parameters;
	int parameter_index;

	public TypeCheckVisitor(SymbolTable symbol_table) {
		this.symbol_table = symbol_table;
		class_name = null;
		function_name = null;
		function_parameters = null;
	}

	private void PrintErrorAndExit() {
		System.out.println("Type error");
		System.exit(1);
	}

	/**
    * f0 -> MainClass()
    * f1 -> ( TypeDeclaration() )*
    * f2 -> <EOF>
    */
   public String visit(Goal n) {
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      return null;
   }

   private String getValueByIndex(int index) {
   	String key = new ArrayList<String>(function_parameters.keySet()).get(index);
   	return function_parameters.get(key);
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
      n.f0.accept(this);
      n.f2.accept(this);
      n.f3.accept(this);
      n.f4.accept(this);
      n.f5.accept(this);
      n.f6.accept(this);
      n.f7.accept(this);
      n.f8.accept(this);
      n.f9.accept(this);
      n.f10.accept(this);
      n.f12.accept(this);
      n.f13.accept(this);
      n.f14.accept(this);
      n.f15.accept(this);
      n.f16.accept(this);
      n.f17.accept(this);
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
      n.f0.accept(this);
      class_name = n.f1.f0.toString();
      n.f2.accept(this);
      n.f3.accept(this);
      n.f4.accept(this);
      n.f5.accept(this);
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
      n.f0.accept(this);
      class_name = n.f1.f0.toString();
      n.f2.accept(this);
      n.f4.accept(this);
      n.f5.accept(this);
      n.f6.accept(this);
      n.f7.accept(this);
      class_name = null;
      return null;
   }

   /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
   public String visit(VarDeclaration n) {
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
      n.f0.accept(this);
      String return_type = n.f1.accept(this);
      function_name = n.f2.f0.toString();
      n.f3.accept(this);
      n.f4.accept(this);
      n.f5.accept(this);
      n.f6.accept(this);
      n.f7.accept(this);
      n.f8.accept(this);
      n.f9.accept(this);
      String return_expression_type = n.f10.accept(this);
      function_name = null;
      if(return_type != return_expression_type) {
        System.err.println("1");
      	PrintErrorAndExit();
      }
      return null;
   }

   /**
    * f0 -> FormalParameter()
    * f1 -> ( FormalParameterRest() )*
    */
   public String visit(FormalParameterList n) {
      return null;
   }

   /**
    * f0 -> Type()
    * f1 -> Identifier()
    */
   public String visit(FormalParameter n) {
      return null;
   }

   /**
    * f0 -> ","
    * f1 -> FormalParameter()
    */
   public String visit(FormalParameterRest n) {
      return null;
   }

   /**
    * f0 -> ArrayType()
    *       | BooleanType()
    *       | IntegerType()
    *       | Identifier()
    */
   public String visit(Type n) {
      return n.f0.accept(this);
   }

   /**
    * f0 -> "int"
    * f1 -> "["
    * f2 -> "]"
    */
   public String visit(ArrayType n) {
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      return array_type;
   }

   /**
    * f0 -> "boolean"
    */
   public String visit(BooleanType n) {
      n.f0.accept(this);
      return boolean_type;
   }

   /**
    * f0 -> "int"
    */
   public String visit(IntegerType n) {
      n.f0.accept(this);
      return integer_type;
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
      return n.f0.accept(this);
   }

   /**
    * f0 -> "{"
    * f1 -> ( Statement() )*
    * f2 -> "}"
    */
   public String visit(Block n) {
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      return null;
   }

   /**
    * f0 -> Identifier()
    * f1 -> "="
    * f2 -> Expression()
    * f3 -> ";"
    */
   public String visit(AssignmentStatement n) {
      String t1 = n.f0.accept(this);
      n.f2.accept(this);
      String t2 = n.f2.accept(this);
      if(t1 != t2) {
      	PrintErrorAndExit();
      }
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
      if (n.f0.accept(this) != array_type) {
      	PrintErrorAndExit();
      }
      n.f1.accept(this);
      if(n.f2.accept(this) != integer_type) {
      	PrintErrorAndExit();
      }
      n.f3.accept(this);
      n.f4.accept(this);
      if(n.f5.accept(this) != integer_type) {
      	PrintErrorAndExit();
      }
      n.f6.accept(this);
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
      n.f0.accept(this);
      n.f1.accept(this);
      if (n.f2.accept(this) != boolean_type) {
      	PrintErrorAndExit();
      }
      n.f3.accept(this);
      n.f4.accept(this);
      n.f5.accept(this);
      n.f6.accept(this);
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
      n.f0.accept(this);
      n.f1.accept(this);
      if(n.f2.accept(this) != boolean_type) {
      	PrintErrorAndExit();
      }
      n.f3.accept(this);
      n.f4.accept(this);
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
      n.f0.accept(this);
      n.f1.accept(this);
      if(n.f2.accept(this) != integer_type) {
      	PrintErrorAndExit();
      }
      n.f3.accept(this);
      n.f4.accept(this);
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
      if(n.f0.accept(this) != boolean_type) {
      	PrintErrorAndExit();
      }
      n.f1.accept(this);
      if(n.f2.accept(this) != boolean_type) {
      	PrintErrorAndExit();
      }
      return boolean_type;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "<"
    * f2 -> PrimaryExpression()
    */
   public String visit(CompareExpression n) {
      if(n.f0.accept(this) != integer_type) {
      	PrintErrorAndExit();
      }
      n.f1.accept(this);
      if(n.f2.accept(this) != integer_type) {
      	PrintErrorAndExit();
      }
      return boolean_type;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "+"
    * f2 -> PrimaryExpression()
    */
   public String visit(PlusExpression n) {
      if(n.f0.accept(this) != integer_type) {
      	PrintErrorAndExit();
      }
      n.f1.accept(this);
      if (n.f2.accept(this) != integer_type) {
      	PrintErrorAndExit();
      }
      return integer_type;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "-"
    * f2 -> PrimaryExpression()
    */
   public String visit(MinusExpression n) {
      if (n.f0.accept(this) != integer_type) {
      	PrintErrorAndExit();
      }
      n.f1.accept(this);
      if (n.f2.accept(this) != integer_type) {
      	PrintErrorAndExit();
      }
      return integer_type;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "*"
    * f2 -> PrimaryExpression()
    */
   public String visit(TimesExpression n) {
      if (n.f0.accept(this) != integer_type) {
      	PrintErrorAndExit();
      }
      n.f1.accept(this);
      if(n.f2.accept(this) != integer_type) {
      	PrintErrorAndExit();
      }
      return integer_type;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "["
    * f2 -> PrimaryExpression()
    * f3 -> "]"
    */
   public String visit(ArrayLookup n) {
      if(n.f0.accept(this) != array_type) {
      	PrintErrorAndExit();
      }
      n.f1.accept(this);
      if (n.f2.accept(this) != integer_type) {
      	PrintErrorAndExit();
      }
      n.f3.accept(this);
      return integer_type;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> "length"
    */
   public String visit(ArrayLength n) {
      if (n.f0.accept(this) != array_type) {
      	PrintErrorAndExit();
      }
      n.f1.accept(this);
      n.f2.accept(this);
      return integer_type;
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
      String object_class_type = n.f0.accept(this);
      if (object_class_type == null) {
      	PrintErrorAndExit();
      }
      ClassSymbolTable cst = symbol_table.getClassSymbolTable(object_class_type);
      if (cst == null) {
      	PrintErrorAndExit();
      }
      FunctionSymbolTable fst = cst.getFunctionSymbolTable(n.f2.f0.toString());
      if (fst == null) {
      	PrintErrorAndExit();
      }
      // TODO: Maybe set function_name
      LinkedHashMap<String, String> prior_parameters = function_parameters;
      function_parameters = fst.formal_parameters;
      int prior_index = parameter_index;
      parameter_index = 0;
      if (n.f4 == null && function_parameters.size() != 0) {
      	PrintErrorAndExit();
      } 
      n.f4.accept(this);
      if (parameter_index != function_parameters.size()) {
        PrintErrorAndExit();
      }
      function_parameters = prior_parameters;
      parameter_index = prior_index;
      return fst.return_type;
   }

   /**
    * f0 -> Expression()
    * f1 -> ( ExpressionRest() )*
    */
   public String visit(ExpressionList n) {
      String actual_parameter_type = n.f0.accept(this);
      String formal_parameter_type = getValueByIndex(0);
      if (formal_parameter_type != actual_parameter_type) {
      	PrintErrorAndExit();
      }
      parameter_index += 1;
      n.f1.accept(this);
      return null;
   }

   /**
    * f0 -> ","
    * f1 -> Expression()
    */
   public String visit(ExpressionRest n) {
   	  String formal_parameter_type = getValueByIndex(parameter_index);
   	  String actual_parameter_type = n.f1.accept(this);
   	  if (formal_parameter_type != actual_parameter_type) {
   	  	PrintErrorAndExit();
   	  }
   	  parameter_index += 1;
      return null;
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
      n.f0.accept(this);
      return integer_type;
   }

   /**
    * f0 -> "true"
    */
   public String visit(TrueLiteral n) {
      n.f0.accept(this);
      return boolean_type;
   }

   /**
    * f0 -> "false"
    */
   public String visit(FalseLiteral n) {
      n.f0.accept(this);
      return boolean_type;
   }

   /**
    * f0 -> <IDENTIFIER>
    */
   public String visit(Identifier n) {
   	  if (class_name == null || function_name == null) {
   	  	PrintErrorAndExit();
   	  }
   	  // TODO: Might have null ptr exception
   	  ClassSymbolTable cst = symbol_table.getClassSymbolTable(class_name);
   	  FunctionSymbolTable fst = symbol_table.getClassSymbolTable(class_name).getFunctionSymbolTable(function_name);
      String ret_val = null;
   	  if(fst.getLocalVariableType(n.f0.toString()) != null) {
   	  	// Check Local Variables first
   	  	ret_val = fst.getLocalVariableType(n.f0.toString());
   	  }
   	  else {
   	  	if (fst.getFormalParameterType(n.f0.toString()) != null) {
   	  		// Check formal parameter list
   	  		ret_val = fst.getFormalParameterType(n.f0.toString());
   	  	}
   	  	else {
   	  		// Check class Fields
   	  		if (cst.getFieldType(n.f0.toString()) != null) {
   	  			ret_val = cst.getFieldType(n.f0.toString());
   	  		}
   	  		else {
   	  			PrintErrorAndExit();
   	  		}
   	  	}
   	  }
   	  return ret_val;
   }

   /**
    * f0 -> "this"
    */
   public String visit(ThisExpression n) {
      return class_name;
   }

   /**
    * f0 -> "new"
    * f1 -> "int"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"
    */
   public String visit(ArrayAllocationExpression n) {
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      if(n.f3.accept(this) != integer_type) {
      	PrintErrorAndExit();
      }
      n.f4.accept(this);
      return array_type;
   }

   /**
    * f0 -> "new"
    * f1 -> Identifier()
    * f2 -> "("
    * f3 -> ")"
    */
   public String visit(AllocationExpression n) {
   	  if (symbol_table.getClassSymbolTable(n.f1.f0.toString()) != null) {
   	  	return n.f1.f0.toString();
   	  }
      else {
      	PrintErrorAndExit();
      }
      return null;
   }

   /**
    * f0 -> "!"
    * f1 -> Expression()
    */
   public String visit(NotExpression n) {
      if (n.f1.accept(this) != boolean_type) {
      	PrintErrorAndExit();
      }
      return boolean_type;
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