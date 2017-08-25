package roottemplate.calculator.evaluator.impls;

import roottemplate.calculator.evaluator.EvaluatorException;
import roottemplate.calculator.evaluator.Function;
import roottemplate.calculator.evaluator.Number;
import roottemplate.calculator.evaluator.PriorityManager;
import roottemplate.calculator.evaluator.Reader;

public class RootFunction extends Function {
    private final boolean supportsNoBrackets;
    
    public RootFunction(PriorityManager prManager, String name, boolean supportsNoBrackets) {
        super(prManager, name, -1);
        this.supportsNoBrackets = supportsNoBrackets;
    }
    
    @Override
    public boolean checkUses(Object before, Reader.IndexedString expr, boolean exactlyThis) {
        if(supportsNoBrackets)
            return true; // Now "<UNICODE_SQRT>4" works
        return super.checkUses(before, expr, exactlyThis);
    }
    
    @Override
    public Number eval0(Number... numbers) throws EvaluatorException {
        if(numbers.length > 2)
            throw new EvaluatorException("Root function expects up to 2 arguments, found " + numbers.length);
        return numbers[0].applyOperation("root", numbers.length > 1 ? numbers[1] : null);
    }
}
