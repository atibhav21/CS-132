import cs132.util.ProblemException;
import cs132.vapor.parser.VaporParser;
import cs132.vapor.ast.VaporProgram;
import cs132.vapor.ast.VBuiltIn.Op;

import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintStream;

public class V2VM {
	public static VaporProgram parseVapor() throws IOException {
	  Op[] ops = {
    		Op.Add, Op.Sub, Op.MulS, Op.Eq, Op.Lt, Op.LtS,
    		Op.PrintIntS, Op.HeapAllocZ, Op.Error,
	  };
	  boolean allowLocals = false;
	  String[] registers = {
    		"v0", "v1",
    		"a0", "a1", "a2", "a3",
    		"t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7",
    		"s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7",
    		"t8",
  	  };
	  boolean allowStack = true;

	  VaporProgram program;
	  try {
	    program = VaporParser.run(new InputStreamReader(System.in), 1, 1,
	                              java.util.Arrays.asList(ops),
	                              allowLocals, registers, allowStack);
	  }
	  catch (ProblemException ex) {
	    System.err.println(ex.getMessage());
	    return null;
	  }

	  return program;
 	}

	public static void main(String[] args) {
		try {
			VaporProgram program = parseVapor();
			MIPSTranslateVisitor visitor = new MIPSTranslateVisitor(program);
			LinkedList<String> mips_code = visitor.getMIPSCode();
			for(String line : mips_code) {
				// TODO: Change from err to out.
				System.err.println(line);
			}
		}
		catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}
}