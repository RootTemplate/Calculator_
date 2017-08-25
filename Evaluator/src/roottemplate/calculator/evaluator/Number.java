/*
 * Copyright 2016-2017 RootTemplate Group 1
 *
 * This file is part of Calculator_ Engine (Evaluator).
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

import java.util.List;

public abstract class Number implements ExpressionElement {
    public static final long serialVersionUID = 1;
    
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
    
    /**
     * @return Real this number
     */
    public abstract Number toNumber();

    public abstract double toDouble();
    
    /**
     * Generates and returns a printable string of the value
     * @return the stringed value
     */
    public abstract String stringValue();

    /**
     * Applies arithmetic operation on this number. Supported operations:
     * <ul>
     * <li>"+", "-" (minus), "-" (negate; when <code>with</code> is <code>null</code>), "*", "/", "%" (modulo), "^" (pow)</li>
     * <li>"!", "abs", "gcd" (greatest common divisor)</li>
     * <li>"log10" (10-base log), "log" (1 arg: <code>n</code>. Returns base-e log of <code>n</code>),
     * "log" (2 args: <code>n</code>, <code>base</code>)</li>
     * <li>"toRadians", "toDegrees"</li>
     * <li>"round", "floor", "ceil"</li>
     * <li>"sqrt", "cbrt", "root" (returns the <code>with</code>-th root of this number; <code>with</code> must be equivalent
     * to mathematical integer, otherwise it will be rounded)</li>
     * <li>"sin", "cos", "tan", "asin", "acos", "atan"</li>
     * <li>"sinh", "cosh", "tanh", "asinh", "acosh", "atanh"</li>
     * </ul>
     * @param operation The operation to be applied
     * @param with Number with that operation will be applied (may be {@code null}). It can have any abstraction level.
     * @return Result number.
     * @throws UnsupportedOperationException if {@code operation} is unknown (not from the list above).
     */
    public abstract Number applyOperation(String operation, Number with) throws EvaluatorException;
    
    public Number applyOperation(String operation) throws EvaluatorException {
        return applyOperation(operation, null);
    }

    /**
     * Creates a copy (clone) of this number. Returned number is unmodifiable and has the same value as this number
     * @return a copy of this number
     */
    public abstract Number copy();

    @Override
    public String toString() {
        return "Number {value: " + stringValue() + "}";
    }
    
    
    
    public static abstract class NumberManager {
        /**
         * Reads number from given string.
         * @param str The string to be read (the string starts with the number)
         * @param i {@code errorIndex}, which may be used in {@code EvaluatorException}, if such will be thrown.
         * @return {@code ReadResult} or {@code null} if number represented in the string is not this (with this
         * abstraction level) number.
         * @throws EvaluatorException when NumberManager understands that number in given string is this
         * (with this abstraction level) number but contains errors.
         */
        public abstract Reader.ReadResult<? extends Number> readNumber(IndexedString str, int i) throws EvaluatorException;

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

        /**
         * Tries to cast this number to number whose abstraction level is 1 less.
         * @param n The number trying to cast (abstraction level = {@link #getAbstractionLevel()}).
         * @return casted number or {@code null} if the number cannot be casted.
         */
        public abstract Number castToLowerAbstractionLevel(Number n);
    }
    
    
    
    static class NumberReader extends Modifier {
        public static final String FRIENDLY_NAME = "NUMBER_MODIFIER";
        private final List<NumberManager> numberManagers;

        NumberReader(List<NumberManager> numberManagers) {
            this.numberManagers = numberManagers;
        }

        @Override
        public int getModifies() {
            return MODIFIES_READER;
        }

        @Override
        public String getFriendlyName() {
            return FRIENDLY_NAME;
        }

        @Override
        public Reader.ReadResult<? extends ExpressionElement> read(IndexedString expr, Evaluator namespace, int i) throws EvaluatorException {
            Reader.ReadResult<? extends Number> result = null;
            for(NumberManager nm : numberManagers) {
                result = nm.readNumber(expr, i);
                if(result != null) break;
            }
            return result;
        }
    }
}
