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
package roottemplate.calculator.evaluator;

import roottemplate.calculator.evaluator.impls.RealNumber;
import roottemplate.calculator.evaluator.impls.SameNumber;

public class Constant extends SameNumber implements Named {
    private final String name;
    
    public Constant(String name, double value) {
        this(name, new RealNumber(value));
    }
    public Constant(String name, Number value) {
        super(value);
        if(!Evaluator.isValidName(name))
            throw new IllegalArgumentException("Unacceptable name: " + name);
        this.name = name;
    }

    @Override
    public boolean isModifiable() {
        return false;
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Constant {name: " + name + ", value: " + doubleValue() + "}";
    }
}