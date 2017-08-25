package roottemplate.calculator.evaluator;

import roottemplate.calculator.evaluator.impls.RealNumber;
import roottemplate.calculator.evaluator.impls.SameNumber;

public class Variable extends SameNumber implements Nameable {
    private final String name;
    
    public Variable(String name) {
        this(name, Double.NaN);
    }
    public Variable(String name, double value) {
        this(name, new RealNumber(value));
    }
    public Variable(String name, Number value) {
        super(value);
        if(!Evaluator.isValidName(name))
            throw new IllegalArgumentException("Unacceptable name: " + name);
        this.name = name;
    }
    
    public void changeValue(Number to) {
        n = to.toNumber();
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Variable {name: " + name + ", value: " + doubleValue() + "}";
    }
}
