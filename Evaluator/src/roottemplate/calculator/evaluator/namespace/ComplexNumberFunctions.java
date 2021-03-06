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

package roottemplate.calculator.evaluator.namespace;

import roottemplate.calculator.evaluator.*;
import roottemplate.calculator.evaluator.Number;

public class ComplexNumberFunctions {
    public static class Arg extends NativeFunction {
        private final Evaluator.Options options;

        public Arg(PriorityManager prManager, Evaluator.Options options) {
            super(prManager, "arg");
            this.options = options;
        }
        protected Arg(Arg bracketCopy) {
            super(bracketCopy);
            options = bracketCopy.options;
        }

        @Override
        protected Number eval0(Number... numbers) throws EvaluatorException {
            if(numbers[0].getNumberManager().getAbstractionLevel() < 2)
                return new RealNumber(0);

            Number res = super.eval0(numbers);
            if(options.ANGLE_MEASURING_UNITS == 2)
                res = res.applyOperation("toDegrees");
            return res;
        }

        @Override
        protected Function copyForNoBrackets() {
            return new Arg(this);
        }
    }

    public static class Conjugate extends NativeFunction {
        public Conjugate(PriorityManager prManager) {
            super(prManager, "conjugate");
        }
        protected Conjugate(Conjugate bracketCopy) {
            super(bracketCopy);
        }

        @Override
        protected Number eval0(Number... numbers) throws EvaluatorException {
            if(numbers[0].getNumberManager().getAbstractionLevel() < 2)
                return numbers[0].copy();
            return super.eval0(numbers);
        }

        @Override
        protected Function copyForNoBrackets() {
            return new Conjugate(this);
        }
    }
}
