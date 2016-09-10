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

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import roottemplate.calculator.evaluator.EvaluatorException;
import roottemplate.calculator.evaluator.MoreMath;
import roottemplate.calculator.evaluator.Number;
import roottemplate.calculator.evaluator.util.IndexedString;

public class BigRealNumber extends Number {
    public static final NumberManager NUMBER_MANAGER = new NumberManager();
    public static MathContext MATH_CONTEXT = new MathContext(20, RoundingMode.HALF_UP);
    
    public static BigRealNumber parse(IndexedString expr) throws EvaluatorException {
        return parse(expr.toString());
    }
    public static BigRealNumber parse(String expr) throws EvaluatorException {
        try {
            return new BigRealNumber(NUMBER_MANAGER.parse(expr));
        } catch(Exception ex) {
            throw new EvaluatorException("Bad number: \"" + expr + "\"");
        }
    }
    
    private final BigDecimal number;
    
    public BigRealNumber(double n) {
        number = new BigDecimal(n);
    }
    protected BigRealNumber(BigDecimal n) {
        number = n;
    }
    
    @Override
    public boolean isModifiable() {
        return false;
    }

    @Override
    public double doubleValue() {
        return number.doubleValue();
    }
    
    @Override
    public Number applyOperation(String operation, Number with) {
        BigDecimal n = with == null ? null : ((BigRealNumber) with.toNumber()).number;
        switch(operation) {
            case "+": return new BigRealNumber(number.add(n));
            case "-": 
                if(with == null)
                    return new BigRealNumber(number.negate());
                else
                    return new BigRealNumber(number.subtract(n));
            case "*": return new BigRealNumber(number.multiply(n));
            case "/": return new BigRealNumber(number / n);
            case "%": return new BigRealNumber(number % n);
            case "^": return new BigRealNumber(Math.pow(number, n));
            case "!": return new BigRealNumber(MoreMath.factorial(number));
                
            case "toRadians": return new BigRealNumber(Math.toRadians(number));
            case "toDegrees": return new BigRealNumber(Math.toDegrees(number));
            case "log10": return new BigRealNumber(Math.log10(number));
            case "log":
                if(with == null)
                    return new BigRealNumber(Math.log(number));
                return new BigRealNumber(MoreMath.log(number, n));
            case "abs": return new BigRealNumber(Math.abs(number));
            case "round": return new BigRealNumber(Math.round(number));
            case "floor": return new BigRealNumber(Math.floor(number));
            case "ceil": return new BigRealNumber(Math.ceil(number));
            case "sqrt": return new BigRealNumber(Math.sqrt(number));
            case "cbrt": return new BigRealNumber(Math.cbrt(number));
            case "sin": return new BigRealNumber(MoreMath.sin(number));
            case "cos": return new BigRealNumber(MoreMath.cos(number));
            case "tan": return new BigRealNumber(MoreMath.tan(number));
            case "asin": return new BigRealNumber(Math.asin(number));
            case "acos": return new BigRealNumber(Math.acos(number));
            case "atan": return new BigRealNumber(Math.atan(number));
            case "root": return new BigRealNumber(MoreMath.root(number, Math.round(with == null ? 2 : n)));
            default: throw new IllegalArgumentException("Operation " + operation + " is undefined");
        }
    }

    @Override
    public Number copy() {
        return new BigRealNumber(number);
    }
    
    @Override
    public Number toNumber() {
        return this;
    }

    @Override
    public NumberManager getNumberManager() {
        return NUMBER_MANAGER;
    }
    
    
    
    
    
    public static class NumberManager extends Number.NumberManager {
        private static final DecimalFormat PARSER = new DecimalFormat();
        
        static {
            PARSER.setParseBigDecimal(true);
        }
        
        @Override
        public int getAbstractionLevel() {
            return 2;
        }

        @Override
        public roottemplate.calculator.evaluator.Number cast(Number number) {
            return new BigRealNumber(number.doubleValue());
        }
        
        public BigDecimal parse(String str) throws ParseException {
            return (BigDecimal) PARSER.parse(str);
        }
    }
}
