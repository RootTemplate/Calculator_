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
        
        this.equalsTo = Expression.parse(equalsTo, localNamespace);
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

    public Variable[] getVars() {
        Variable[] res = new Variable[vars.length];
        System.arraycopy(vars, 0, res, 0, res.length);
        return res;
    }

    /*@Override
    public void compile(ExpressionElement[] elems, int referenceOffset, int resultIndex, Object kit,
            EVMCompiler evmm) throws EvaluatorException {
        if(elems.length != vars.length)
            throw new EvaluatorException(toString() + ": actual and expected argument count differs: " + vars.length + " expected, " +
                    elems.length + " received");
        
        int i = 0;
        for(ExpressionElement elem : elems) {
            int nIndex = evmm.compilePutIntoBuffer(kit, vars[i], -1);
            switch(elem.getElementType()) {
                case NUMBER:
                    evmm.compilePutIntoBuffer(kit, (Number) elem, nIndex);
                    break;
                case SYSTEM_REFERENCE:
                    evmm.compilePutIntoBuffer(kit, ((SystemReference) elem).reference + referenceOffset, nIndex);
                    break;
            }
            i++;
        }
        int exprIndex = evmm.compileExpression(equalsTo, kit);
        evmm.compilePutIntoBuffer(kit, exprIndex, resultIndex);
    }*/

    @Override
    public String toString() {
        return super.toString() + "vars: " + Arrays.toString(vars) + ", " + equalsTo + "}";
    }
}
