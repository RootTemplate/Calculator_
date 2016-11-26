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

import java.util.ArrayList;
import java.util.List;
import roottemplate.calculator.evaluator.EvaluatorException;
import roottemplate.calculator.evaluator.Expression;
import roottemplate.calculator.evaluator.ExpressionElement;
import roottemplate.calculator.evaluator.Instruction;
import roottemplate.calculator.evaluator.Operator;
import roottemplate.calculator.evaluator.Number;
import roottemplate.calculator.evaluator.SystemReference;
import roottemplate.calculator.evaluator.util.BufferIndexManager;
import roottemplate.calculator.evaluator.util.ListStruct;

public class StandardEVMCompiler extends StackEVMCompiler {
    @Override
    public Expression compile(ListStruct exprElems) throws EvaluatorException {
        return StandardExpression.createFromListStruct(exprElems);
    }
    
    Kit compileToKit(ListStruct exprElems) throws EvaluatorException {
        Kit kit = new Kit();
        kit.result = compileExprElems(exprElems, kit);
        return kit;
    }

    @Override
    protected ExpressionElement compileOperator(Operator op, ExpressionElement[] ns, Object kit_) throws EvaluatorException {
        Kit kit = (Kit) kit_;
        BufferIndexManager indexes = kit.indexes;
        List<Instruction> insns = kit.insns;

        int refCount = 0, lastRefIndex = -1;
        boolean hasModifiableNumbers = false, hasResult;
        for(int i = 0; i < ns.length; i++) {
            ExpressionElement elem = ns[i];
            switch(elem.getElementType()) {
                case NUMBER: hasResult = ((Number) elem).isModifiable(); break;
                case SYSTEM_REFERENCE:
                    indexes.freeIndex(((SystemReference) elem).reference);

                    hasResult = false;
                    lastRefIndex = i;
                    refCount++;
                    break;
                default:
                    throw new EvaluatorException(ns[i] + " found in the Expression, which is illegal");
            }
            if(hasResult) hasModifiableNumbers = true;
        }
        boolean hasModifiable = hasModifiableNumbers || refCount > 0;

        if(op.getUses() == Operator.Uses.TWO_NUMBERS && op.isCommutative() && refCount == 1 && !hasModifiableNumbers) {
            SystemReference ref = (SystemReference) ns[lastRefIndex];
            Instruction lastInstr = insns.get(ref.instructionIndex);
            int n1Index;
            if(lastInstr.op.equals(op) && (n1Index = lastInstr.findUnmodifNumberIndex()) != -1) {
                lastInstr.setElement(n1Index, op.eval((Number) lastInstr.elems[n1Index],
                        (Number) ns[1 - lastRefIndex])); // lastRefIndex is either 0 or 1; so calculated index in 1 or 0 respectively
                // last reference was removed by default, return it
                return ref;
            }
        }

        if(hasModifiable) {
            int bIndex = indexes.allocIndex();
            Instruction ins = new Instruction(op, bIndex, ns);
            insns.add(ins);
            return new SystemReference(bIndex, insns.size() - 1); // using curNode to store result
        } else {
            return new Instruction(op, -1, ns).do_(null);
        }
    }
    
    
    
    public static class Kit {
        public final BufferIndexManager indexes = new BufferIndexManager();
        public final List<Instruction> insns = new ArrayList<>();
        
        public ExpressionElement result = null;
    }
}
