/* 
 * Copyright 2016 RootTemplate Group 1
 *
 * This file is part of Calculator_ Engine (Evaluator).
 *
 * Calculator_ Engine (Evaluator) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Calculator_ Engine (Evaluator) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Calculator_ Engine (Evaluator).  If not, see <http://www.gnu.org/licenses/>.
 */
package roottemplate.calculator.evaluator.impls;

import roottemplate.calculator.evaluator.EvaluatorException;
import roottemplate.calculator.evaluator.MoreMath;
import roottemplate.calculator.evaluator.Number;
import roottemplate.calculator.evaluator.util.IndexedString;

public class RealNumber extends Number {
    public static final NumberManager NUMBER_MANAGER = new RealNumberManager();
    
    public static RealNumber parse(IndexedString expr) throws EvaluatorException {
        return parse(expr.toString());
    }
    public static RealNumber parse(String expr) throws EvaluatorException {
        try {
            return new RealNumber(Double.parseDouble(expr));
        } catch(NumberFormatException ex) {
            throw new EvaluatorException("Bad number: \"" + expr + "\"");
        }
    }
    
    private double number;
    private final boolean isModifiable;
    
    public RealNumber(double n) {
        number = n;
        isModifiable = false;
    }
    protected RealNumber(double n, boolean isModifiable) {
        number = n;
        this.isModifiable = isModifiable;
    }
    
    @Override
    public boolean isModifiable() {
        return isModifiable;
    }
    
    protected void setValue(double n) {
        if(!isModifiable) throw new UnsupportedOperationException();
        number = n;
    }
    protected void addDelta(double n) {
        if(!isModifiable) throw new UnsupportedOperationException();
        number += n;
    }

    @Override
    public double doubleValue() {
        return number;
    }
    
    @Override
    public Number applyOperation(String operation, Number with) {
        double n = with == null ? 0 : with.doubleValue();
        switch(operation) {
            case "+": return new RealNumber(number + n);
            case "-": 
                if(with == null)
                    return new RealNumber(-number);
                else
                    return new RealNumber(number - n);
            case "*": return new RealNumber(number * n);
            case "/": return new RealNumber(number / n);
            case "%": return new RealNumber(number % n);
            case "^": return new RealNumber(Math.pow(number, n));
            case "!": return new RealNumber(MoreMath.factorial(number));
                
            case "toRadians": return new RealNumber(Math.toRadians(number));
            case "toDegrees": return new RealNumber(Math.toDegrees(number));
            case "log10": return new RealNumber(Math.log10(number));
            case "log":
                if(with == null)
                    return new RealNumber(Math.log(number));
                return new RealNumber(MoreMath.log(number, n));
            case "abs": return new RealNumber(Math.abs(number));
            case "round": return new RealNumber(Math.round(number));
            case "floor": return new RealNumber(Math.floor(number));
            case "ceil": return new RealNumber(Math.ceil(number));
            case "sqrt": return new RealNumber(Math.sqrt(number));
            case "cbrt": return new RealNumber(Math.cbrt(number));
            case "sin": return new RealNumber(MoreMath.sin(number));
            case "cos": return new RealNumber(MoreMath.cos(number));
            case "tan": return new RealNumber(MoreMath.tan(number));
            case "asin": return new RealNumber(Math.asin(number));
            case "acos": return new RealNumber(Math.acos(number));
            case "atan": return new RealNumber(Math.atan(number));
            case "root": return new RealNumber(MoreMath.root(number, Math.round(with == null ? 2 : n)));
            default: throw new IllegalArgumentException("Operation " + operation + " is undefined");
        }
    }

    @Override
    public Number copy() {
        return new RealNumber(number);
    }
    
    @Override
    public Number toNumber() {
        return this;
    }

    @Override
    public NumberManager getNumberManager() {
        return NUMBER_MANAGER;
    }
    
    
    
    
    
    public static class RealNumberManager extends NumberManager {
        @Override
        public int getAbstractionLevel() {
            return 1;
        }

        @Override
        public roottemplate.calculator.evaluator.Number cast(Number number) {
            throw new UnsupportedOperationException("There are no numbers that have lower abstraction levels than Real");
        }
    }
}
