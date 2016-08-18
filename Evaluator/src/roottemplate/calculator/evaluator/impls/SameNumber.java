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

import roottemplate.calculator.evaluator.Number;

public class SameNumber extends Number {
    protected Number n;
    public SameNumber(Number n) {
        if(n == null) throw new NullPointerException();
        this.n = n;
    }

    @Override
    public NumberManager getNumberManager() {
        return n.getNumberManager();
    }
    
    @Override
    public boolean isModifiable() {
        return true;
    }

    @Override
    public double doubleValue() {
        return this.n.doubleValue();
    }
    
    @Override
    public Number copy() {
        return n.copy();
    }

    @Override
    public Number toNumber() {
        return n.toNumber();
    }

    @Override
    public Number applyOperation(String operation, Number with) {
        return this.n.applyOperation(operation, with);
    }
}
