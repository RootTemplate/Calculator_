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

public class PercentOperator extends Operator {
    private static final RealNumber ONE = new RealNumber(1D);
    private static final RealNumber ONE_HUNDREDTH = new RealNumber(0.01D);

    public static boolean isPercentOperator(ExpressionElement elem) {
        if(elem.getElementType() != ElementType.OPERATOR) return false;
        Operator op = (Operator) elem;
        return "%".equals(op.getName()) && op.getUses() == Uses.ONE_LEFT_NUMBER;
    }


    private final PriorityManager.PriorityStorage hPercentSt;

    public PercentOperator(PriorityManager.PriorityStorage prStorage, PriorityManager.PriorityStorage hPercentSt) {
        super(prStorage, "%", Uses.ONE_LEFT_NUMBER);
        this.hPercentSt = hPercentSt;
    }

    @Override
    public Number eval(Number... numbers) throws EvaluatorException {
        return numbers[0].applyOperation("*", ONE_HUNDREDTH);
    }

    public HelperPercentOperator createHelperPercent(boolean plus) {
        return new HelperPercentOperator(plus);
    }

    public class HelperPercentOperator extends Operator {
        private final boolean plus;

        public HelperPercentOperator(boolean plus) {
            super(hPercentSt, "%", Uses.ONE_LEFT_NUMBER);
            this.plus = plus;
        }

        @Override
        public Number eval(Number... numbers) throws EvaluatorException {
            if(plus) {
                return numbers[0].applyOperation("*", ONE_HUNDREDTH).applyOperation("+", ONE);
            } else {
                return ONE.applyOperation("-", numbers[0].applyOperation("*", ONE_HUNDREDTH));
            }
        }
    }
}
