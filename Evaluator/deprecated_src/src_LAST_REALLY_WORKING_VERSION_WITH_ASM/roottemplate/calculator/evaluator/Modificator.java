package roottemplate.calculator.evaluator;

import roottemplate.calculator.evaluator.Reader.IndexedString;
import roottemplate.calculator.evaluator.Reader.ListNode;

public abstract class Modificator {
    public static final int MODIFIES_READER = 0b1;
    public static final int MODIFIES_PREEVALUATOR = 0b10;
    
    public Reader.ReadResult<? extends ExpressionElement> read(IndexedString expr, Evaluator namespace, int i) throws EvaluatorException {
        return null;
    }
    public void preevaluate(ExpressionElement before, ExpressionElement now, ExpressionElement next, ListNode node, Evaluator namespace) {}
    
    public abstract int getModifies();
}
