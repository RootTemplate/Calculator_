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
package roottemplate.calculator.evaluator;

import roottemplate.calculator.evaluator.namespace.RealNumber;
import roottemplate.calculator.evaluator.util.SameNumber;

public class Constant extends SameNumber implements Named {
    private final String name;
    private final boolean canBeOverridden;
    
    public Constant(String name, double value) {
        this(name, new RealNumber(value));
    }

    public Constant(String name, Number value) {
        this(name, value, false);
    }

    public Constant(String name, Number value, boolean canBeOverridden) {
        super(value);
        this.canBeOverridden = canBeOverridden;
        if(!Evaluator.isValidName(name))
            throw new IllegalArgumentException("Unacceptable name: " + name);
        this.name = name;
    }

    @Override
    public boolean isModifiable() {
        return false;
    }
    public boolean canBeOverridden() {
        return canBeOverridden;
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Constant {name: " + name + ", value: " + stringValue() + "}";
    }
}