package roottemplate.calculator.evaluator.impls;

import roottemplate.calculator.evaluator.EvaluatorException;
import roottemplate.calculator.evaluator.Function;
import roottemplate.calculator.evaluator.Number;
import roottemplate.calculator.evaluator.PriorityManager;

public class NativeFunction extends Function {
    public NativeFunction(PriorityManager prManager, String name) {
        this(prManager, name, name);
    }
    public NativeFunction(PriorityManager prManager, String realName, String name) {
        super(prManager, realName, name, 1);
    }
    protected NativeFunction(PriorityManager.PriorityStorage prStorage, String realName, String name) {
        super(prStorage, realName, name, 1);
    }

    @Override
    protected Number eval0(Number... numbers) throws EvaluatorException {
        return numbers[0].applyOperation(realName);
    }

    @Override
    public String toString() {
        return super.toString() + "method: " + realName + "}";
    }
}
