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

public class GCDFunction extends Function {
    public GCDFunction(PriorityManager prManager) {
        super(prManager, "gcd", -1);
    }
    private GCDFunction(GCDFunction self) {
        super(self);
    }

    @Override
    protected Number eval0(Number... numbers) throws EvaluatorException {
        if(numbers.length < 2)
            throw new EvaluatorException("GCD(...) takes at least 2 arguments");

        Number result = numbers[0];
        for(int i = 1; i < numbers.length; i++)
            result = result.applyOperation("gcd", numbers[i]);
        return result;
    }

    @Override
    protected Function copyForNoBrackets() {
        return new GCDFunction(this);
    }
}
