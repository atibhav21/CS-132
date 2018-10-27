import syntaxtree.*;
import visitor.*;


public class SymbolTableVisitor extends DepthFirstVisitor {
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
   public void visit(MainClass n) {
   	  if (class_name != null || function_name != null) {
   	  	// Main class cannot be defined within some other class or function
   	  	PrintErrorAndExit();
   	  }
   	  // System.out.println(class_name);
   	  if (!symbol_table.addClass(n.f1.f0.toString())) {
   	  	PrintErrorAndExit();
   	  }
   	  class_name = n.f1.f0.toString();
   	  if (!symbol_table.getClassSymbolTable(class_name).addFunction(n.f6.toString(), null)) {
   	  	PrintErrorAndExit();
   	  }
   	  function_name = n.f6.toString();
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
      n.f13.accept(this);
      n.f14.accept(this);
      n.f15.accept(this);
      n.f16.accept(this);
      function_name = null;
      n.f17.accept(this);
      class_name = null;
   }


	/**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> ( VarDeclaration() )*
    * f4 -> ( MethodDeclaration() )*
    * f5 -> "}"
    */
   public void visit(ClassDeclaration n) {
   	  if (class_name != null || function_name != null) {
   	  	// Class cannot be defined within some other class or function in MiniJava
   	  	PrintErrorAndExit();
   	  }
   	  if (!symbol_table.addClass(n.f1.f0.toString())) {
   	  	PrintErrorAndExit();
   	  }
   	  class_name = n.f1.f0.toString();
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      n.f3.accept(this);
      n.f4.accept(this);
      n.f5.accept(this);
      class_name = null;
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
   public void visit(ClassExtendsDeclaration n) {
   	// Doesn't do anything with inheritance
   	  if (class_name != null || function_name != null) {
   	  	// Class cannot be defined within some other class or function in MiniJava
   	  	PrintErrorAndExit();
   	  }
   	  if (!symbol_table.addClass(n.f1.f0.toString())) {
   	  	PrintErrorAndExit();
   	  }
   	  class_name = n.f1.f0.toString();
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      n.f3.accept(this);
      n.f4.accept(this);
      n.f5.accept(this);
      n.f6.accept(this);
      n.f7.accept(this);
      class_name = null;
   }

      /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
   public void visit(VarDeclaration n) {
   	  if (class_name == null) {
   	  	// Variables must be defined inside a class/inside a function in a class.
   	  	PrintErrorAndExit();
   	  }
   	  if (function_name == null) {
   	  	// Add it to the Class as a member variable.
   	  	symbol_table.getClassSymbolTable(class_name).addField(n.f1.f0.toString(), n.f0);
   	  }
   	  else {
   	  	// Add it to some function in the class as a local variable.
   	  	symbol_table.getClassSymbolTable(class_name).getFunctionSymbolTable(function_name).addLocalVariable(n.f1.f0.toString(), n.f0);
   	  }
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
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
   public void visit(MethodDeclaration n) {
   	  if (class_name == null || function_name != null) {
   	  	// Function should be defined within a class, and not within any other function
   	  	PrintErrorAndExit();
   	  }
   	  if (! symbol_table.getClassSymbolTable(class_name).addFunction(n.f2.f0.toString(), n.f1)) {
   	  	PrintErrorAndExit();
   	  }
   	  function_name = n.f2.f0.toString();
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
      function_name = null;
   }

   /**
    * f0 -> Type()
    * f1 -> Identifier()
    */
   public void visit(FormalParameter n) {
   	  if (class_name == null || function_name == null) {
   	  	// Parameters should be defined for a function inside a class.
   	  	PrintErrorAndExit();
   	  }
   	  if (! symbol_table.getClassSymbolTable(class_name).getFunctionSymbolTable(function_name).addFormalParameter(n.f1.f0.toString(), n.f0)) {
   	  	PrintErrorAndExit();
   	  }
      n.f0.accept(this);
      n.f1.accept(this);
   }
}