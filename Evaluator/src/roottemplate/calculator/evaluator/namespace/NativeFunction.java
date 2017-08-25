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

public class NativeFunction extends Function {
    public NativeFunction(PriorityManager prManager, String name) {
        this(prManager, name, name);
    }
    public NativeFunction(PriorityManager prManager, String realName, String name) {
        this(prManager, realName, name, false);
    }
    public NativeFunction(PriorityManager prManager, String realName, String name, boolean twoArgs) {
        super(prManager, realName, name, twoArgs ? 2 : 1);
    }
    protected NativeFunction(NativeFunction bracketCopy) {
        super(bracketCopy);
    }

    @Override
    protected Number eval0(Number... numbers) throws EvaluatorException {
        return numbers[0].applyOperation(realName, argsCount > 1 ? numbers[1] : null);
    }

    @Override
    protected Function copyForNoBrackets() {
        return new NativeFunction(this);
    }

    @Override
    public String toString() {
        return super.toString() + "method: " + realName + ", argsCount: " + argsCount + "}";
    }
}
