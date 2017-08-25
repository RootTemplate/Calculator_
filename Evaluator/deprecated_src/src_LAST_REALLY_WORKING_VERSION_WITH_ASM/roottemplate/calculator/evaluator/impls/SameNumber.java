package roottemplate.calculator.evaluator.impls;

import roottemplate.calculator.evaluator.Number;

public class SameNumber extends Number {
    protected Number n;
    public SameNumber(Number n) {
        if(n == null) throw new NullPointerException();
        this.n = n;
    }

    @Override
    public NumberManager getNumberManager() {
        return n.getNumberManager();
    }
    
    @Override
    public boolean isModifiable() {
        return true;
    }

    @Override
    public double doubleValue() {
        return this.n.doubleValue();
    }
    
    @Override
    public Number copy() {
        return n.copy();
    }

    @Override
    public Number toNumber() {
        return n.toNumber();
    }

    @Override
    public Number applyOperation(String operation, Number with) {
        return this.n.applyOperation(operation, with);
    }
}
