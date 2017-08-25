package roottemplate.calculator.evaluator;

import roottemplate.calculator.evaluator.Number.NumberManager;

public interface CompiledExpression {

    /**
     * Evaluates expression.
     * <b>WARNING</b>: if any modifiable number which is used to evaluate this expression has different <code>NumberManager</code> from
     * <code>NumberManager</code> returned from <code>getNumberManager()</code>, then method's behaviour is unexpected.
     * @return result
     * @throws EvaluatorException if exception occurs
     */
    Number eval() throws EvaluatorException;

    /**
     * Returns <code>NumberManager</code> for which this <code>CompiledExpression</code> is designed.
     * @return NumberManager
     */
    NumberManager getNumberManager();
}
