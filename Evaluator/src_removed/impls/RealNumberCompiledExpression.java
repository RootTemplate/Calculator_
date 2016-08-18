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

import roottemplate.calculator.evaluator.Evaluator;
import roottemplate.calculator.evaluator.EvaluatorException;
import roottemplate.calculator.evaluator.Number;
import roottemplate.calculator.evaluator.EVMCompiler;
import roottemplate.calculator.evaluator.Expression;

public abstract class RealNumberCompiledExpression /*implements Expression*/  {
    /*protected final Number[] n; // numbers
    protected final double[] b; // buffer
    protected final EVMCompiler evmm; // number manager
    protected final Evaluator.Options opt; // options

    public RealNumberCompiledExpression(int bufferSize, Number[] modifiableNumbers, EVMCompiler evmManager, Evaluator.Options options) {
        n = modifiableNumbers;
        evmm = evmManager;
        b = new double[bufferSize];
        opt = options;
    }

    @Override
    public synchronized Number eval() throws EvaluatorException {
        return new RealNumber(eval0());
    }

    public abstract double eval0();

    @Override
    public EVMCompiler getEVMManager() {
        return evmm;
    }*/
}
