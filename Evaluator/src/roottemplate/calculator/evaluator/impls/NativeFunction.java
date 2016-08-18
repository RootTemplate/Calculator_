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

import roottemplate.calculator.evaluator.EvaluatorException;
import roottemplate.calculator.evaluator.Function;
import roottemplate.calculator.evaluator.Number;
import roottemplate.calculator.evaluator.PriorityManager;

public class NativeFunction extends Function {
    public NativeFunction(PriorityManager prManager, String name) {
        this(prManager, name, name);
    }
    public NativeFunction(PriorityManager prManager, String realName, String name) {
        super(prManager, realName, name, 1);
    }
    protected NativeFunction(PriorityManager.PriorityStorage prStorage, String realName, String name) {
        super(prStorage, realName, name, 1);
    }

    @Override
    protected Number eval0(Number... numbers) throws EvaluatorException {
        return numbers[0].applyOperation(realName);
    }

    @Override
    public String toString() {
        return super.toString() + "method: " + realName + "}";
    }
}
