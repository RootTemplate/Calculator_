package roottemplate.calculator.evaluator.impls;

import roottemplate.calculator.evaluator.Evaluator;
import roottemplate.calculator.evaluator.EvaluatorException;
import roottemplate.calculator.evaluator.ExpressionElement;
import roottemplate.calculator.evaluator.ExpressionEnum;
import roottemplate.calculator.evaluator.Nameable;
import roottemplate.calculator.evaluator.Reader;

public class ExpressionAndItsEnumReader extends Reader implements Nameable {
    @Override
    public ReadResult<? extends ExpressionElement> read(IndexedString expr, Evaluator namespace, int i) throws EvaluatorException {
        ReadResult<IndexedString> result = Reader.readBrackets(expr, 0, null);
        ExpressionEnum parsed = ExpressionEnum.parse(result.n, namespace, i + 1);
        if(parsed.canBeCastedToExpression())
            return new ReadResult<>(parsed.castToExpression(), result.charsUsed);
        else
            return new ReadResult<>(parsed, result.charsUsed);
    }

    @Override
    public String getName() {
        return "(";
    }
}
