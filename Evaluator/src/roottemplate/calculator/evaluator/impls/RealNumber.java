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
import roottemplate.calculator.evaluator.Reader;
import roottemplate.calculator.evaluator.util.IndexedString;

import static roottemplate.calculator.evaluator.impls.RealNumber.RealNumberManager.ABSTRACTION_LEVEL;

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
    
    public final double number;
    
    public RealNumber(double n) {
        number = n;
    }

    @Override
    public boolean isModifiable() {
        return false;
    }
    
    @Override
    public Number applyOperation(String operation, Number with) throws EvaluatorException {
        int absLevel;
        if(with != null && (absLevel = with.getNumberManager().getAbstractionLevel()) != ABSTRACTION_LEVEL) {
            if(absLevel > ABSTRACTION_LEVEL)
                return with.getNumberManager().cast(this).applyOperation(operation, with);
            else
                throw new UnsupportedOperationException("There are no numbers with abstraction level < 1");
        }

        double n = with == null ? 0 : ((RealNumber) with.toNumber()).number;
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
            case "^":
                double res = Math.pow(number, n);
                if(Double.isNaN(res)) break; // reapply operation in complex
                return new RealNumber(res);
            case "!": return new RealNumber(MoreMath.factorial(number));
                
            case "toRadians": return new RealNumber(Math.toRadians(number));
            case "toDegrees": return new RealNumber(Math.toDegrees(number));
            case "log10": return new RealNumber(Math.log10(number));
            case "log":
                if(with == null)
                    return new RealNumber(Math.log(number));
                return new RealNumber(MoreMath.log(number, n));
            case "abs": return new RealNumber(Math.abs(number));
            case "gcd": return new RealNumber(MoreMath.gcd(number, n));
            case "round": return new RealNumber(Math.round(number));
            case "floor": return new RealNumber(Math.floor(number));
            case "ceil": return new RealNumber(Math.ceil(number));
            case "sqrt":
            case "root":
                res = MoreMath.root(number, Math.round(with == null ? 2 : n));
                if(Double.isNaN(res)) break;
                return new RealNumber(res);
            case "cbrt": return new RealNumber(Math.cbrt(number));
            case "sin": return new RealNumber(MoreMath.sin(number));
            case "cos": return new RealNumber(MoreMath.cos(number));
            case "tan": return new RealNumber(MoreMath.tan(number));
            case "asin": return new RealNumber(Math.asin(number));
            case "acos": return new RealNumber(Math.acos(number));
            case "atan": return new RealNumber(Math.atan(number));
            case "sinh": return new RealNumber(Math.sinh(number));
            case "cosh": return new RealNumber(Math.cosh(number));
            case "tanh": return new RealNumber(Math.tanh(number));
            case "asinh": return new RealNumber(MoreMath.asinh(number));
            case "acosh": return new RealNumber(MoreMath.acosh(number));
            case "atanh": return new RealNumber(MoreMath.atanh(number));
            default: throw new UnsupportedOperationException("Operation " + operation + " is undefined");
        }

        // If we are here, case this number to complex (abs. level + 1)
        // TODO: if result is NaN, then cast this number to higher abs. level (using getNumberManagers() in
        // Evaluator) and reapply operation
        return ComplexNumber.NUMBER_MANAGER.cast(this).applyOperation(operation, with);
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
    public double toDouble() {
        return number;
    }

    @Override
    public String stringValue() {
        return Double.toString(number);
    }

    @Override
    public NumberManager getNumberManager() {
        return NUMBER_MANAGER;
    }


    
    
    public static class RealNumberManager extends NumberManager {
        public static final int ABSTRACTION_LEVEL = 1;

        @Override
        public Reader.ReadResult<? extends Number> readNumber(IndexedString expr, int i) throws EvaluatorException {
            int j;
            boolean gotPoint = false;
            boolean gotE = false;
            boolean gotSign = false;
            boolean gotDigit = false;
            for(j = 0; j < expr.length(); j++) {
                char at = expr.charAt(j);
                if(at == '+' || at == '-') {
                    if(gotSign) break;
                    gotSign = true;
                } else if(at == '.') {
                    if(gotPoint)
                        throw new EvaluatorException("Two points in one number. At: " + i, i);
                    else if(gotE)
                        throw new EvaluatorException("In exponent section points are prohibited", i);
                    else {
                        gotPoint = true;
                    }
                } else if(Character.isDigit(at)) {
                    gotSign = true;
                    gotDigit = true;
                } else if(!gotDigit)
                    return null; // Not a number
                else if(at == 'E') {
                    if(gotE)
                        throw new EvaluatorException("Two E in one number. At: " + i, i);
                    gotE = true;
                    gotSign = false;
                } else
                    break;
            }

            IndexedString str = expr.substring(0, j);
            return new Reader.ReadResult<>(RealNumber.parse(str), j);
        }

        @Override
        public int getAbstractionLevel() {
            return ABSTRACTION_LEVEL;
        }

        @Override
        public roottemplate.calculator.evaluator.Number cast(Number number) {
            throw new UnsupportedOperationException("There are no numbers that have lower abstraction levels than Real");
        }

        @Override
        public Number castToLowerAbstractionLevel(Number n) {
            return null;
        }
    }
}
