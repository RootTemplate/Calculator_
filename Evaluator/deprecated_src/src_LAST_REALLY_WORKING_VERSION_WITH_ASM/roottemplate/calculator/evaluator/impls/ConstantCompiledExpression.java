package roottemplate.calculator.evaluator.impls;

import roottemplate.calculator.evaluator.CompiledExpression;
import roottemplate.calculator.evaluator.EvaluatorException;
import roottemplate.calculator.evaluator.Number;

public class ConstantCompiledExpression implements CompiledExpression {
    private final Number number;
    
    public ConstantCompiledExpression(Number n) {
        number = n;
    }
    
    @Override
    public Number eval() throws EvaluatorException {
        return number.isModifiable() ? number.copy() : number;
    }

    @Override
    public Number.NumberManager getNumberManager() {
        return number.getNumberManager();
    }
}
