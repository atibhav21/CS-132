import syntaxtree.*;
import visitor.*;

public class Typecheck {
	public static void main(String [] args) {
		try {
			Goal goal = new MiniJavaParser(System.in).Goal();
			SymbolTableVisitor symbol_table_visitor = new SymbolTableVisitor();
			symbol_table_visitor.visit(goal);
			SymbolTable symbol_table = symbol_table_visitor.getSymbolTable();
			symbol_table.print();
			System.out.println("Program type checked successfully");
		}
		catch(ParseException e) {
			System.out.println("Type error");
		}
	}

}