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

import java.util.List;

import roottemplate.calculator.evaluator.util.IndexedString;
import roottemplate.calculator.evaluator.util.ListStruct;

public class StandardExpression extends Expression {
    public static final StandardEVMCompiler EVMCOMPILER = new StandardEVMCompiler();
    
    public static StandardExpression parse(String expr, Evaluator namespace) throws EvaluatorException {
        return createFromListStruct(Reader.readExpressionElements(new IndexedString(expr), namespace, 0));
    }
    public static StandardExpression createFromNumber(Number n) {
        try {
            return new StandardExpression(null, n, 0);
        } catch (EvaluatorException ex) {
            throw new Error(); // Will never be thrown
        }
    }
    public static StandardExpression createFromList(List<ExpressionElement> list) throws EvaluatorException {
        ListStruct list_ = new ListStruct();
        ListStruct.ListNode beforeNode = null;
        for(ExpressionElement elem : list) {
            beforeNode = list_.pseudoAdd(beforeNode, elem);
        }
        list_.endNode = beforeNode;
        return createFromListStruct(list_);
    }

    /**
     * WARNING: after executing this method, <code>list</code> will become empty!
     */
    public static StandardExpression createFromListStruct(ListStruct list) throws EvaluatorException {
        if(list.startNode == null || list.endNode == null) throw new EvaluatorException("Empty expression given", 0);
        return EVMCOMPILER.compile(list);
    }
    
    
    private final Instruction[] instructions;
    private final ExpressionElement result;
    private final Number[] buffer;
    
    StandardExpression(Instruction[] instrs, ExpressionElement result, int bufferLength) throws EvaluatorException {
        ExpressionElement.ElementType resultType = result.getElementType();
        if(resultType != ExpressionElement.ElementType.SYSTEM_REFERENCE && resultType != ExpressionElement.ElementType.NUMBER)
            throw new EvaluatorException("Result is not a number", -1);
        if(bufferLength == 0 && result.getElementType() != ExpressionElement.ElementType.NUMBER)
            throw new EvaluatorException("Buffer is empty but result is not a number", -1);
        if(bufferLength > 0 && result.getElementType() != ExpressionElement.ElementType.SYSTEM_REFERENCE)
            throw new EvaluatorException("Buffer is not empty but result is not a reference", -1);
        
        this.instructions = instrs;
        this.result = result;
        this.buffer = instrs == null ? null : new Number[bufferLength];
    }
    
    @Override
    public synchronized Number eval() throws EvaluatorException {
        if(instructions != null) {
            for(Instruction i : instructions)
                i.execute(buffer);

            if(result.getElementType() == ExpressionElement.ElementType.SYSTEM_REFERENCE)
                return buffer[((SystemReference) result).reference];
        }

        // Result = Number
        Number res = (Number) result;
        return res.isModifiable() ? res.copy() : res;
    }

    public boolean containsModifiableNumbers() {
        return instructions != null && instructions.length != 0 || ((Number) result).isModifiable();
    }
    
    public Instruction[] getInstructions() {
        return instructions.clone();
    }
    public ExpressionElement getResultElem() {
        return result;
    }
    public int getBufferSize() {
        return buffer == null ? 0 : buffer.length;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Expression {");
        for(Instruction obj : instructions) {
            sb.append(obj.toString()).append(", ");
        }
        sb.append("result: ").append(result);
        return sb.append("}").toString();
    }

    @Override
    public EVMCompiler getEVMCompiler() {
        return EVMCOMPILER;
    }
}
