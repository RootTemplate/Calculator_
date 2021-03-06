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

public class TrigonometricFunction extends NativeFunction {
    protected final Evaluator.Options options;
    
    public TrigonometricFunction(PriorityManager prManager, String name, Evaluator.Options options) {
        this(prManager, name, name, options);
    }
    public TrigonometricFunction(PriorityManager prManager, String realName, String name, Evaluator.Options options) {
        super(prManager, realName, name);
        this.options = options;
    }
    protected TrigonometricFunction(TrigonometricFunction bracketCopy) {
        super(bracketCopy);
        this.options = bracketCopy.options;
    }

    @Override
    public Number eval0(Number... numbers) throws EvaluatorException {
        boolean a = realName.startsWith("a");
        Number arg = a || options.ANGLE_MEASURING_UNITS == 1 ? numbers[0] : numbers[0].applyOperation("toRadians", null);
        Number result = super.eval0(arg);
        return a && options.ANGLE_MEASURING_UNITS != 1 ? result.applyOperation("toDegrees", null) : result;
    }

    @Override
    protected Function copyForNoBrackets() {
        return new TrigonometricFunction(this);
    }
}
