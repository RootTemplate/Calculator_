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

import java.util.Arrays;

public class DefaultFunction extends Function {
    public final Expression equalsTo;
    private final Variable[] vars;
        
    public DefaultFunction(Evaluator namespace, String name, String equalsTo, Variable... vars) throws EvaluatorException {
        super(namespace.getPriorityManager(), name, vars.length);
        
        Evaluator localNamespace = new Evaluator(namespace);
        for(Variable var : vars)
            localNamespace.add(var);
        
        this.equalsTo = StandardExpression.parse(equalsTo, localNamespace);
        this.vars = vars;
    }
    
    public DefaultFunction(PriorityManager prManager, String name, Expression equalsTo, Variable... vars) {
        super(prManager, name, vars.length);
        
        this.equalsTo = equalsTo;
        this.vars = vars;
    }

    @Override
    protected Number eval0(Number... numbers) throws EvaluatorException {
        for(int i = 0; i < numbers.length; i++)
            vars[i].changeValue(numbers[i]);
        return equalsTo.eval();
    }

    @Override
    protected Function copyForNoBrackets() {
        return null; // This will simply prohibit to use DefaultFunction without brackets
    }

    public Variable[] getVars() {
        Variable[] res = new Variable[vars.length];
        System.arraycopy(vars, 0, res, 0, res.length);
        return res;
    }

    @Override
    public String toString() {
        return super.toString() + "vars: " + Arrays.toString(vars) + ", " + equalsTo + "}";
    }
}
