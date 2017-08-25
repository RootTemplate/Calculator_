package roottemplate.calculator.evaluator;

import roottemplate.calculator.evaluator.PriorityManager.PriorityStorage;

public abstract class Function extends Operator {
    public static final String PRIORITY_FRIENDLY_NAME = "F";
    public static final boolean PRIORITY_LEFT_DIRECTION = false;
    public static final Uses FUNCTION_USES = Uses.RIGHT_NUMBER_ENUM;
    
    
    
    private final int argsCount;
    
    public Function(PriorityManager prManager, String name, int argsCount) {
        this(prManager, name, name, argsCount);
    }
    public Function(PriorityManager prManager, String realName, String name, int argsCount) {
        super(prManager.createPriority(PRIORITY_FRIENDLY_NAME, PRIORITY_LEFT_DIRECTION), realName, name, FUNCTION_USES);
        this.argsCount = argsCount;
    }

    protected Function(PriorityStorage prStorage, String realName, String name, int argsCount) {
        super(prStorage, realName, name, FUNCTION_USES);
        this.argsCount = argsCount;
    }

    @Override
    public Number eval(Number... numbers) throws EvaluatorException {
        if(argsCount != -1 && numbers.length != argsCount)
            throw new EvaluatorException("Expected " + argsCount + " but found "
                    + numbers.length + " arguments after function \"" + name + "\"");
        return eval0(numbers);
    }
    protected abstract Number eval0(Number... numbers) throws EvaluatorException;
    
    @Override
    public boolean checkUses(Object before, Reader.IndexedString expr, boolean exactlyThis) {
        if(!exactlyThis) {
            int startingIndex = expr.index;
            while(!expr.isEmpty()) {
                char at = expr.charAt();
                if(Character.isWhitespace(at))
                    expr.index++;
                else {
                    expr.index = startingIndex;
                    return at == '(';
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "Function {name: " + name + ", ";
    }
}
