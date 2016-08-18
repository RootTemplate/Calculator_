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
import roottemplate.calculator.evaluator.Operator;
import roottemplate.calculator.evaluator.PriorityManager.PriorityStorage;
import roottemplate.calculator.evaluator.Number;
import roottemplate.calculator.evaluator.grapher.ExprSolver;

public class DivideOperator extends Operator {
    public DivideOperator(PriorityStorage prStorage) {
        super(prStorage, "/");
    }
    
    @Override
    public Object createGapKit() {
        return new ExprSolver[] {new ExprSolver(), new ExprSolver()};
    }
    
    @Override
    public Number gapEval(Object kit, boolean rightSide, Number... numbers) throws EvaluatorException {
        ExprSolver[] ess = (ExprSolver[]) kit;
        ess[0].next(numbers[0].doubleValue());
        ess[1].next(numbers[1].doubleValue());
        
        if(ess[1].was(0) >= ExprSolver.SOLUTION_STRONG || ess[1].next == 0) {
            double prev = ess[0].prev, next = ess[0].next;
            if(next * prev > 0 || prev == 0)
                return new RealNumber(next / 0);
            else
                return new RealNumber(Double.NaN);
        } else {
            return eval(numbers);
        }
    }
    
    @Override
    public boolean wasGap(Object kit, Number... numbers) {
        ExprSolver es = ((ExprSolver[]) kit)[1];
        return es.was(0) >= ExprSolver.SOLUTION_STRONG || es.next == 0;
    }
}
