import cs132.vapor.ast.*;
import java.util.*;

public class MIPSTranslateVisitor extends VInstr.Visitor<java.lang.RuntimeException>{
	LinkedList<String> mips_code;

	public MIPSTranslateVisitor(VaporProgram program) {

	}

	public LinkedList<String> getMIPSCode() {
		return mips_code;
	}
}