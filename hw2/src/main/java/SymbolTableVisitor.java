import syntaxtree.*;
import visitor.*;


public class SymbolTableVisitor extends GJNoArguDepthFirst<String> {
	String class_name;
	String function_name;
	SymbolTable symbol_table;

	public SymbolTableVisitor() {
		class_name = null;
		function_name = null;
		symbol_table = new SymbolTable();
	}

	public SymbolTable getSymbolTable() {
		return symbol_table;
	}

	private void PrintErrorAndExit() {
		System.out.println("Type error");
		System.exit(1);
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
   	  if (!symbol_table.addClass(n.f1.f0.toString())) {
   	  	PrintErrorAndExit();
   	  }
   	  class_name = n.f1.f0.toString();
   	  if (!symbol_table.getClassSymbolTable(class_name).addFunction(n.f6.toString(), null)) {
   	  	PrintErrorAndExit();
   	  }
   	  function_name = n.f6.toString();
      n.f1.accept(this);
      n.f11.accept(this);
      n.f14.accept(this);
      n.f15.accept(this);
      function_name = null;
      class_name = null;
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
   	  if (!symbol_table.addClass(n.f1.f0.toString())) {
   	  	PrintErrorAndExit();
   	  }
   	  class_name = n.f1.f0.toString();
      n.f1.accept(this);
      n.f3.accept(this);
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
   	// Doesn't do anything with inheritance
   	  if (!symbol_table.addClass(n.f1.f0.toString())) {
   	  	PrintErrorAndExit();
   	  }
   	  class_name = n.f1.f0.toString();
      n.f1.accept(this);
      n.f3.accept(this);
      n.f5.accept(this);
      n.f6.accept(this);
      class_name = null;
      return null;
   }

      /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
   public String visit(VarDeclaration n) {
      String type = n.f0.accept(this);
      
   	  if (function_name == null) {
   	  	// Add it to the Class as a member variable.
   	  	if(! symbol_table.getClassSymbolTable(class_name).addField(n.f1.f0.toString(), type)) {
   	  		PrintErrorAndExit();
   	  	}
   	  }
   	  else {
   	  	// Add it to some function in the class as a local variable.
        ClassSymbolTable c = symbol_table.getClassSymbolTable(class_name);
        FunctionSymbolTable f = c.getFunctionSymbolTable(function_name);
   	  	if (!f.addLocalVariable(n.f1.f0.toString(), type)) {
   	  		PrintErrorAndExit();
   	  	}
   	  }
      n.f1.accept(this);
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
      String type = n.f1.accept(this);
      if (! symbol_table.getClassSymbolTable(class_name).addFunction(function_name, type)) {
        PrintErrorAndExit();
      }
      n.f2.accept(this);
      n.f4.accept(this);
      n.f7.accept(this);
      n.f8.accept(this);
      n.f10.accept(this);
      function_name = null;
      return null;
   }

   /**
    * f0 -> Type()
    * f1 -> Identifier()
    */
   public String visit(FormalParameter n) {
      String type = n.f0.accept(this);
      n.f1.accept(this);
   	  if (! symbol_table.getClassSymbolTable(class_name).getFunctionSymbolTable(function_name).addFormalParameter(n.f1.f0.toString(), type)) {
   	  	PrintErrorAndExit();
   	  }
      
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
      return "Array";
   }

   /**
    * f0 -> "boolean"
    */
   public String visit(BooleanType n) {
      return "Boolean";
   }

   /**
    * f0 -> "int"
    */
   public String visit(IntegerType n) {
      return "Integer";
   }

   /**
    * f0 -> <IDENTIFIER>
    */
   public String visit(Identifier n) {
      return n.f0.toString();
   }
}