import syntaxtree.*;
import visitor.*;


public class SymbolTableVisitor extends GJNoArguDepthFirst<String> {
	String class_name;
	String function_name;
	GlobalSymbolTable gst;

	SymbolTableVisitor() {
		class_name = null;
		function_name = null;
		gst = new GlobalSymbolTable();
	}

	public GlobalSymbolTable getGlobalSymbolTable() {
		return gst;
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
      class_name = n.f1.f0.toString();
      gst.addClass(class_name, null);
      function_name = n.f6.toString();
      gst.getClassSymbolTable(class_name).addMemberFunction(function_name, null);
      n.f14.accept(this);
      function_name = null;
      class_name = null;
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
   // TODO
   public String visit(ClassDeclaration n) {
      class_name = n.f1.f0.toString();
      gst.addClass(class_name, null);
      System.out.println("const vmt_" + class_name);
      n.f3.accept(this);
      n.f4.accept(this);
      System.out.println();
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
   // TODO
   public String visit(ClassExtendsDeclaration n) {
   	  class_name = n.f1.f0.toString();
      String parent_name = n.f3.f0.toString();
      gst.addClass(class_name, parent_name);
      System.out.println("const vmt_" + class_name);
      n.f5.accept(this);
      n.f6.accept(this);
      System.out.println();
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
      String variable_name = n.f1.f0.toString();
      if(function_name == null) {
      	// Add it as a class member variable.
      	gst.getClassSymbolTable(class_name).addMemberVariable(variable_name, type);
      }
      else {
      	gst.getClassSymbolTable(class_name).getFunctionSymbolTable(function_name).addLocalVariable(variable_name, type);
      }
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
      String type = n.f1.accept(this);
      function_name = n.f2.f0.toString();
      gst.getClassSymbolTable(class_name).addMemberFunction(function_name, type);
      n.f4.accept(this);
      n.f7.accept(this);
      System.out.println("  :" + class_name + "." + function_name);
      function_name = null;
      return null;
   }

   /**
    * f0 -> FormalParameter()
    * f1 -> ( FormalParameterRest() )*
    */
   public String visit(FormalParameterList n) {
      n.f0.accept(this);
      n.f1.accept(this);
      return null;
   }

   /**
    * f0 -> Type()
    * f1 -> Identifier()
    */
   public String visit(FormalParameter n) {
      String type = n.f0.accept(this);
      String parameter_name = n.f1.f0.toString();
      gst.getClassSymbolTable(class_name).getFunctionSymbolTable(function_name).addFormalParameter(parameter_name, type);
      return null;
   }

   /**
    * f0 -> ","
    * f1 -> FormalParameter()
    */
   public String visit(FormalParameterRest n) {
      n.f1.accept(this);
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