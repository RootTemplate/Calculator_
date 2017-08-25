package roottemplate.calculator.evaluator.util;

public class CompileUtil {
    private static int classNameId = 0;
    public static String nextClassName() {
        return "C" + classNameId++;
    }
}
