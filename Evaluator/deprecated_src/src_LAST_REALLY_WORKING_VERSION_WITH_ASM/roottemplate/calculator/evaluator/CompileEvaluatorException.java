package roottemplate.calculator.evaluator;

public class CompileEvaluatorException extends EvaluatorException {
    public CompileEvaluatorException(String message) {
        super(message);
    }

    public CompileEvaluatorException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public CompileEvaluatorException(Throwable cause) {
        super(cause.getMessage(), cause);
    }
}
