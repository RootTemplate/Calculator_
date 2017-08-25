package roottemplate.calculator.evaluator.impls;

import roottemplate.calculator.evaluator.Function;
import roottemplate.calculator.evaluator.Operator;
import roottemplate.calculator.evaluator.PriorityManager;
import roottemplate.calculator.evaluator.Reader.IndexedString;

public class NegateOperator extends Operator {
    public NegateOperator(PriorityManager prManager) {
        super(prManager.createPriority(Function.PRIORITY_FRIENDLY_NAME, Function.PRIORITY_LEFT_DIRECTION),
                "-", Operator.Uses.ONE_RIGHT_NUMBER);
    }

    @Override
    public boolean checkUses(Object before, IndexedString expr, boolean exactlyThis) {
        if(exactlyThis || expr.isEmpty()) return true;
        char[] acceptable = new char[] {'.', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'}; // real numbers; not complex
        char at = expr.charAt(0);
        for(int i = 0; i < acceptable.length; i++)
            if(acceptable[i] == at)
                return false;
        return true;
    }
}
