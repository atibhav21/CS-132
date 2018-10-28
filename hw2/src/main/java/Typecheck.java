import syntaxtree.*;
import visitor.*;

public class Typecheck {
	public static void main(String [] args) {
		try {
			Goal goal = new MiniJavaParser(System.in).Goal();
			SymbolTableVisitor symbol_table_visitor = new SymbolTableVisitor();
			symbol_table_visitor.visit(goal);
			TypeCheckVisitor type_check_visitor = new TypeCheckVisitor(symbol_table_visitor.getSymbolTable());
			type_check_visitor.visit(goal);
			System.out.println("Program type checked successfully");
		}
		catch(ParseException e) {
			System.out.println("Type error");
		}
	}

}