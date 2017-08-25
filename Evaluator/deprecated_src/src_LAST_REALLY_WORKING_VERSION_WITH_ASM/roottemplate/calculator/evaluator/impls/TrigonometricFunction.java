package roottemplate.calculator.evaluator.impls;

import roottemplate.calculator.evaluator.Evaluator;
import roottemplate.calculator.evaluator.EvaluatorException;
import roottemplate.calculator.evaluator.PriorityManager;
import roottemplate.calculator.evaluator.Number;
import roottemplate.calculator.evaluator.Reader;

public class TrigonometricFunction extends NativeFunction {
    protected final Evaluator.Options options;
    
    public TrigonometricFunction(PriorityManager prManager, String name, Evaluator.Options options) {
        this(prManager, name, name, options);
    }
    public TrigonometricFunction(PriorityManager prManager, String realName, String name, Evaluator.Options options) {
        super(prManager, realName, name);
        this.options = options;
    }
    
    @Override
    public boolean checkUses(Object before, Reader.IndexedString expr, boolean exactlyThis) {
        return true; // Now sin 90 works
    }

    @Override
    public Number eval0(Number... numbers) throws EvaluatorException {
        boolean a = realName.startsWith("a");
        Number arg = a || options.ANGLE_MEASURING_UNITS == 1 ? numbers[0] : numbers[0].applyOperation("toRadians", null);
        Number result = super.eval0(arg);
        return a && options.ANGLE_MEASURING_UNITS != 1 ? result.applyOperation("toDegrees", null) : result;
    }
}
