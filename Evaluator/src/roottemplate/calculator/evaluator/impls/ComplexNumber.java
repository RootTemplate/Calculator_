/*
 * Copyright 2016 RootTemplate Group 1
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

package roottemplate.calculator.evaluator.impls;

import roottemplate.calculator.evaluator.EvaluatorException;
import roottemplate.calculator.evaluator.Number;
import roottemplate.calculator.evaluator.Reader;
import roottemplate.calculator.evaluator.util.Complex;
import roottemplate.calculator.evaluator.util.IndexedString;

import static roottemplate.calculator.evaluator.impls.ComplexNumber.ComplexNumberManager.ABSTRACTION_LEVEL;

public class ComplexNumber extends Number {
    public static final ComplexNumberManager NUMBER_MANAGER = new ComplexNumberManager();

    private final Complex number;

    public ComplexNumber(double re) {
        this(re, 0);
    }
    public ComplexNumber(double re, double im) {
        this(new Complex(re, im));
    }
    protected ComplexNumber(Complex number) {
        this.number = number;
    }

    public double getRe() {
        return number.re;
    }
    public double getIm() {
        return number.im;
    }

    @Override
    public boolean isModifiable() {
        return false;
    }

    @Override
    public NumberManager getNumberManager() {
        return NUMBER_MANAGER;
    }

    @Override
    public Number toNumber() {
        return this;
    }

    @Override
    public double toDouble() {
        return number.asReal();
    }

    @Override
    public String stringValue() {
        StringBuilder sb = new StringBuilder(Double.toString(number.re));
        if(number.im >= 0) sb.append('+');
        sb.append(Double.toString(number.im)).append('i');
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     * Also supports "conjugate" and "arg" (returns the argument (angle) in polar coordinates) operations.
     */
    @Override
    public Number applyOperation(String operation, Number with_) throws EvaluatorException {
        int absLevel;
        if(with_ != null && (absLevel = with_.getNumberManager().getAbstractionLevel()) != ABSTRACTION_LEVEL) {
            if(absLevel > ABSTRACTION_LEVEL)
                return with_.getNumberManager().cast(this).applyOperation(operation, with_);
            else
                with_ = getNumberManager().cast(with_);
        }

        Complex a = new Complex(number);
        Complex b = with_ == null ? null : ((ComplexNumber) with_.toNumber()).number;
        switch (operation) {
            case "+": return new ComplexNumber(a.add(b));
            case "-":
                if(b == null)
                    return new ComplexNumber(a.negate());
                else
                    return new ComplexNumber(a.sub(b));
            case "*": return new ComplexNumber(a.mul(b));
            case "/": return new ComplexNumber(a.div(b));
            case "%": return new ComplexNumber(a.mod(b));
            case "^": return new ComplexNumber(a.pow(b));
            case "!": return new ComplexNumber(a.factorial());
            case "abs": return new ComplexNumber(a.abs());
            case "gcd": return new ComplexNumber(a.gcd(b));
            case "log10": return new ComplexNumber(a.log10());
            case "log":
                if(b == null)
                    return new ComplexNumber(a.log());
                else
                    return new ComplexNumber(a.log(b));
            case "toRadians": return new ComplexNumber(a.mul(new Complex(Math.PI / 180D, 0)));
            case "toDegrees": return new ComplexNumber(a.mul(new Complex(180D / Math.PI, 0)));
            case "floor": return new ComplexNumber(a.floor());
            case "round":
            case "ceil":
                return new ComplexNumber(new Complex(Double.NaN, Double.NaN));
                // round, ceil of a complex number? Sorry, it's too hard to calculate. I'm tired.
            case "sqrt": return new ComplexNumber(a.sqrt());
            case "cbrt":
                b = new Complex(3, 0);
            case "root":
                if(b == null) b = new Complex(2, 0);
                return new ComplexNumber(a.root(b));
            case "sin": return new ComplexNumber(a.sin());
            case "cos": return new ComplexNumber(a.cos());
            case "tan": return new ComplexNumber(a.tan());
            case "asin": return new ComplexNumber(a.asin());
            case "acos": return new ComplexNumber(a.acos());
            case "atan": return new ComplexNumber(a.atan());
            case "sinh": return new ComplexNumber(a.sinh());
            case "cosh": return new ComplexNumber(a.cosh());
            case "tanh": return new ComplexNumber(a.tanh());
            case "asinh": return new ComplexNumber(a.asinh());
            case "acosh": return new ComplexNumber(a.acosh());
            case "atanh": return new ComplexNumber(a.atanh());
            // todo: round, ceil methods

            case "conjugate": return new ComplexNumber(a.conjugate());
            case "arg": return new RealNumber(a.arg());

            default: throw new UnsupportedOperationException("Operation " + operation + " is unknown");
        }
    }

    @Override
    public Number copy() {
        return new ComplexNumber(new Complex(number));
    }


    public static class ComplexNumberManager extends Number.NumberManager {
        public static final int ABSTRACTION_LEVEL = 2;

        @Override
        public Reader.ReadResult<? extends Number> readNumber(IndexedString str, int i) throws EvaluatorException {
            return null; // No string represent of complex number available
        }

        @Override
        public int getAbstractionLevel() {
            return ABSTRACTION_LEVEL;
        }

        @Override
        public Number cast(Number number) {
            // It can be only real number
            return new ComplexNumber(((RealNumber) number.toNumber()).number);
        }

        @Override
        public Number castToLowerAbstractionLevel(Number n) {
            Complex number = ((ComplexNumber) n.toNumber()).number;
            return number.im == 0 ? new RealNumber(number.re) : null;
        }
    }
}
