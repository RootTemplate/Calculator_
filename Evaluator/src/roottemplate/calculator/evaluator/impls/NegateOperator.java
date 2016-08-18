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

import roottemplate.calculator.evaluator.Function;
import roottemplate.calculator.evaluator.Operator;
import roottemplate.calculator.evaluator.PriorityManager;
import roottemplate.calculator.evaluator.util.IndexedString;

public class NegateOperator extends Operator {
    public NegateOperator(PriorityManager prManager) {
        super(prManager.createPriority(Function.PRIORITY_FRIENDLY_NAME, Function.PRIORITY_LEFT_DIRECTION),
                "-", Operator.Uses.ONE_RIGHT_NUMBER);
    }

    @Override
    public boolean checkUses(Object before, IndexedString expr, boolean exactlyThis) {
        if(exactlyThis || expr.isEmpty()) return true;
        char[] acceptable = new char[] {'.', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'}; // real numbers; not complex
        char at = expr.charAt(0);
        for(int i = 0; i < acceptable.length; i++)
            if(acceptable[i] == at)
                return false;
        return true;
    }
}
