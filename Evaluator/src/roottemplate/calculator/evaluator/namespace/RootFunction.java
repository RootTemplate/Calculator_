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

public class RootFunction extends Function {
    public RootFunction(PriorityManager prManager, String name) {
        super(prManager, name, -1);
    }
    public RootFunction(PriorityManager.PriorityStorage prSt, String name) {
        super(prSt, null, name, name, -1);
    }
    protected RootFunction(Function bracketCopy) {
        super(bracketCopy);
    }

    @Override
    protected Function copyForNoBrackets() {
        return prStNoBrackets == null ? this : new RootFunction(this);
    }

    /**
     * Args:<ol>
     * <li>1 arg: <code>n</code>
     * <li>2 args: <code>thRoot</code>, <code>n</code>
     * </ol>
     */
    @Override
    public Number eval0(Number... numbers) throws EvaluatorException {
        if(numbers.length > 2)
            throw new EvaluatorException("Root function expects up to 2 arguments, found " + numbers.length);
        if(numbers.length == 1)
            return numbers[0].applyOperation("root", null);
        return numbers[1].applyOperation("root", numbers[0]);
    }
}
