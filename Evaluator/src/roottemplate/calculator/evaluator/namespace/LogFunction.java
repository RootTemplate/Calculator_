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

import roottemplate.calculator.evaluator.EvaluatorException;
import roottemplate.calculator.evaluator.Function;
import roottemplate.calculator.evaluator.Number;
import roottemplate.calculator.evaluator.PriorityManager;

public class LogFunction extends Function {

    public LogFunction(PriorityManager prManager) {
        super(prManager, "log", -1);
    }
    private LogFunction(LogFunction bracketCopy) {
        super(bracketCopy);
    }

    /**
     * Args:<ol>
     * <li>1 arg: <code>n</code>
     * <li>2 args: <code>base</code>, <code>n</code>
     * </ol>
     */
    @Override
    protected Number eval0(Number... numbers) throws EvaluatorException {
        if(numbers.length > 2)
            throw new EvaluatorException("Log function expects up to 2 arguments, found " + numbers.length);
        if(numbers.length == 1)
            return numbers[0].applyOperation("log");
        return numbers[1].applyOperation("log", numbers[0]);
    }

    @Override
    protected Function copyForNoBrackets() {
        return new LogFunction(this);
    }

}
