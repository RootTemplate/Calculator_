package roottemplate.calculator.evaluator;

import roottemplate.calculator.evaluator.impls.RealNumber;
import roottemplate.calculator.evaluator.impls.SameNumber;

public class Constant extends SameNumber implements Nameable {
    private final String name;
    
    public Constant(String name, double value) {
        this(name, new RealNumber(value));
    }
    public Constant(String name, Number value) {
        super(value);
        if(!Evaluator.isValidName(name))
            throw new IllegalArgumentException("Unacceptable name: " + name);
        this.name = name;
    }

    @Override
    public boolean isModifiable() {
        return false;
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Constant {name: " + name + ", value: " + doubleValue() + "}";
    }
}