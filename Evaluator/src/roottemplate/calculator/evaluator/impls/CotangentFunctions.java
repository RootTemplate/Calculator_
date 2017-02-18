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

import roottemplate.calculator.evaluator.*;
import roottemplate.calculator.evaluator.Number;

public class CotangentFunctions {
    // TODO: casting numbers fix

    public static class CtgFunction extends Function {
        private final Evaluator.Options options;

        public CtgFunction(PriorityManager prManager, String name, Evaluator.Options options) {
            super(prManager, "ctg", name, 1);
            this.options = options;
        }

        @Override
        protected Number eval0(Number... numbers) throws EvaluatorException {
            Number arg = options.ANGLE_MEASURING_UNITS == 1 ? numbers[0] :
                    numbers[0].applyOperation("toRadians", null);
            return new RealNumber(1).applyOperation("/", arg.applyOperation("tan"));
        }
    }

    public static class ArcctgFunction extends Function {
        private final Evaluator.Options options;

        public ArcctgFunction(PriorityManager prManager, String name, Evaluator.Options options) {
            super(prManager, "atan", name, 1);
            this.options = options;
        }

        @Override
        protected Number eval0(Number... numbers) throws EvaluatorException {
            Number result = new RealNumber(Math.PI / 2).applyOperation("-", numbers[0].applyOperation("atan"));
            if(options.ANGLE_MEASURING_UNITS == 2)
                result = result.applyOperation("toDegrees");
            return result;
        }
    }
}
