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

import roottemplate.calculator.evaluator.impls.StandardExpression;

// Warning: Expression is not an Operator. Impossible to understand if Expression contains modifiable numbers
public abstract class Expression {
    public static Expression parse(String expr, Evaluator namespace) throws EvaluatorException {
        return StandardExpression.createFromString(expr, namespace);
    }
    
    /**
     * Evaluates expression.
     * @return result
     * @throws EvaluatorException if exception occurs
     */
    public abstract Number eval() throws EvaluatorException;

    /**
     * Returns <code>EVMCompiler</code> which have compiled this <code>Expression</code>.
     * @return EVMCompiler
     */
    public abstract EVMCompiler getEVMManager();
}
