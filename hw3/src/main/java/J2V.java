import syntaxtree.*;
import visitor.*;

public class J2V { 
    
    public static void main (String [] args) {
    	try {
    		Goal goal = new MiniJavaParser(System.in).Goal();
    		SymbolTableVisitor st_visitor = new SymbolTableVisitor();
    		st_visitor.visit(goal);
    		GlobalSymbolTable gst = st_visitor.getGlobalSymbolTable();
    		VaporVisitor vapor_visitor = new VaporVisitor(gst);
    		vapor_visitor.visit(goal);
    		List<String> vapor_code = vapor_visitor.getVaporCode();
    		for(String line: vapor_code) {
    			System.out.println(line);
    		}
    	}
    	catch (ParseException e) {
    		System.out.println("Parse error");
    	} 
    }


}