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
package roottemplate.calculator.evaluator;

import roottemplate.calculator.evaluator.util.IndexedString;
import roottemplate.calculator.evaluator.impls.RealNumber;
import roottemplate.calculator.evaluator.util.Util;

public abstract class Number extends java.lang.Number implements ExpressionElement {
    
    /**
     * Checks if this number can change its value at runtime
     * @return true if this number is modifiable
     */
    public abstract boolean isModifiable();
    
    public abstract NumberManager getNumberManager();
    
    @Override
    public final ElementType getElementType() {
        return ElementType.NUMBER;
    }
    
    @Override
    public long longValue() {
        return Math.round(doubleValue());
    }

    @Override
    public float floatValue() {
        return (float) doubleValue();
    }

    @Override
    public int intValue() {
        return (int) longValue();
    }
    
    /**
     * @return Real this number
     */
    public abstract Number toNumber();
    
    /**
     * Generates and returns a printable string of the value
     * @return the stringed value
     */
    public String stringValue() {
        return Util.doubleToString(doubleValue(), 10);
    }
    
    /**
     * Applies arithmetic operation on this number. Supported operations:
     * "+", "-" (minus), "-" (negate; <code>with</code> must be <code>null</code>), "*", "/", "%" (modulo), "^" (pow), "!",
     * "toRadians", "toDegrees", "log10" (10-base log), "log" (1 arg: <code>n</code>. Returns base-e log of <code>n</code>),
     * "log" (2 args: <code>n</code>, <code>base</code>), "abs", "round", "floor", "ceil", "sqrt", "cbrt", "sin", "cos", "tan",
     * "asin", "acos", "atan", "root" (returns the <code>with</code>-th root of this number; <code>with</code> must be equivalent
     * to mathematical integer, otherwise it will be rounded).
     * @param operation The operation to be applied
     * @param with Number with that operation will be applied
     * @return Result number
     * @throws ClassCastException if this and <code>with</code> numbers have different abstraction levels
     * (but the throw is not guaranteed)
     */
    public abstract Number applyOperation(String operation, Number with);
    
    public Number applyOperation(String operation) {
        return applyOperation(operation, null);
    }

    /**
     * Creates a copy (clone) of this number. Returned number is unmodifiable and has the same value as this number
     * @return a copy of this number
     */
    public abstract Number copy();

    @Override
    public String toString() {
        return "Number {value: " + doubleValue() + "}";
    }
    
    
    
    public static abstract class NumberManager {
        /**
         * Returns number abstraction level. For example, complex numbers
         * have higher level than real numbers.
         * @return number abstraction level
         */
        public abstract int getAbstractionLevel();
        
        /**
         * Casts a number with lower abstraction level to number with the same
         * abstraction level as this one.
         * @param number The number needed to be cast
         * @return Casted number
         */
        public abstract Number cast(Number number);
    }
    
    
    
    static class NumberReader extends Modificator {
        @Override
        public int getModifies() {
            return MODIFIES_READER;
        }
        
        @Override
        public Reader.ReadResult<? extends ExpressionElement> read(IndexedString expr, Evaluator namespace, int i) throws EvaluatorException {
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
                        throw new EvaluatorException("Two points in one number. At: " + i);
                    else if(gotE)
                        throw new EvaluatorException("In exponent section points are prohibited");
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
                        throw new EvaluatorException("Two E in one number. At: " + i);
                    gotE = true;
                    gotSign = false;
                } else
                    break;
            }

            return new Reader.ReadResult<>(RealNumber.parse(expr.substring(0, j)), j);
        }
    }
}
