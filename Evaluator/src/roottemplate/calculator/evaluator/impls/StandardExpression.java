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

import java.util.List;
import roottemplate.calculator.evaluator.EVMCompiler;
import roottemplate.calculator.evaluator.Evaluator;
import roottemplate.calculator.evaluator.EvaluatorException;
import roottemplate.calculator.evaluator.Expression;
import roottemplate.calculator.evaluator.ExpressionElement;
import roottemplate.calculator.evaluator.Instruction;
import roottemplate.calculator.evaluator.Number;
import roottemplate.calculator.evaluator.Reader;
import roottemplate.calculator.evaluator.SystemReference;
import roottemplate.calculator.evaluator.util.IndexedString;
import roottemplate.calculator.evaluator.util.ListStruct;

public class StandardExpression extends Expression {
    public static final StandardEVMCompiler EVMCOMPILER = new StandardEVMCompiler();
    
    public static StandardExpression createFromString(String expr, Evaluator namespace) throws EvaluatorException {
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
     * WARNING: after executing this method, <code>list</code> become empty!
     */
    public static StandardExpression createFromListStruct(ListStruct list) throws EvaluatorException {
        if(list.startNode == null || list.endNode == null) throw new EvaluatorException("Empty string given");
        
        StandardEVMCompiler.Kit kit = EVMCOMPILER.compileToKit(list);
        return new StandardExpression(  kit.insns.toArray(new Instruction[kit.insns.size()]),
                kit.result, kit.indexes.getBufferSize()  );
    }
    
    
    private final Instruction[] instructions;
    private final ExpressionElement result;
    private final Number[] buffer;
    
    StandardExpression(Instruction[] instrs, ExpressionElement result, int bufferLength) throws EvaluatorException {
        ExpressionElement.ElementType resultType = result.getElementType();
        if(resultType != ExpressionElement.ElementType.SYSTEM_REFERENCE && resultType != ExpressionElement.ElementType.NUMBER)
            throw new EvaluatorException("Result is not a number");
        if(bufferLength == 0 && result.getElementType() != ExpressionElement.ElementType.NUMBER)
            throw new EvaluatorException("Buffer is empty but result is not a number");
        if(bufferLength > 0 && result.getElementType() != ExpressionElement.ElementType.SYSTEM_REFERENCE)
            throw new EvaluatorException("Buffer is not empty but result is not a reference");
        
        this.instructions = instrs;
        this.result = result;
        this.buffer = instrs == null ? null : new Number[bufferLength];
    }
    
    @Override
    public synchronized Number eval() throws EvaluatorException {
        if(instructions != null) {
            for(Instruction i : instructions)
                i.do_(buffer);

            if(result.getElementType() == ExpressionElement.ElementType.SYSTEM_REFERENCE)
                return buffer[((SystemReference) result).reference];
        }
        // Result = Number
        return ((Number) result).isModifiable() ? ((Number) result).copy() : (Number) result;
    }
    
    
    
    public boolean containsModifiableNumbers() {
        if(instructions == null || instructions.length == 0) {
            // Now we need to check the result Nameable
            // Now result can be only NUMBER
            return ((Number) result).isModifiable();
        }
        return true;
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
    public EVMCompiler getEVMManager() {
        return null;
    }
}
