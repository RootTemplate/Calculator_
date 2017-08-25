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

import roottemplate.calculator.evaluator.Number.NumberManager;
import roottemplate.calculator.evaluator.util.BufferCmpSet;
import roottemplate.calculator.evaluator.util.ListStruct;

// Expression = Evaluator Virtual Machine
public interface EVMManager_ {
    /* *
     * Initialize buffer. This method:
     * <ol>
     * <li>Initializes buffer field(s) by getting <code>FieldVisitor</code></li>
     * <li>Allocates buffer in constructor method</li>
     * </ol>
     * @param kit The kit object
     * @param className The name of visiting class
     * @param bufferLength Buffer length
     * /
    void compileInitBuffer(Object kit, String className, int bufferLength);

    /**
     * Initialize buffer as a local variable. It declares local buffer variable in method, <code>MethodVisitor</code> of whom is given,
     * that refers to field buffer. This is necessary to reduce the number of instructions getting field buffer.
     * @param mv MethodVisitor
     * @param className The name of visiting class
     * /
    void compileInitLocalBuffer(MethodVisitor mv, String className);

    /**
     * Sets up workspace in method <code>eval</code>. This method converts every <code>Number</code> from a class field "<code>n</code>" type
     * <code>Number[]</code> to a convenient form for the implementation of <code>NumberManager</code>, which must be stored to local buffer.
     * Indexes of this convenient forms in the buffer are stored in an <code>int[]</code> field of the class called "nm".
     * @param mv <code>eval()</code> method visitor
     * @param className The name of visiting class
     
    void compileSetupEvalWorkspace(MethodVisitor mv, String className);*/
    
    Expression compile(Expression expr, Evaluator.Options options) throws EvaluatorException;
    
    /**
     * Writes bytecode for an expression <b>and executes it</b>. This compile operation uses 0 vars.
     * @param expr Expression
     * @param kit Kit
     * @return absolute buffer index with the result
     * @throws EvaluatorException if error occurs
     */
    int compileExpression(Expression expr, Object kit) throws EvaluatorException;
    
    int compileGetVarCountOperatorUses(String op, ExpressionElement[] elems, Object kit);

    /**
     * Writes bytecode for an operator.
     * <b>WARNING</b>: All <code>SystemReference</code> from <code>elems</code> refers to
     * "<code>SystemReference.reference + referenceOffset</code>" index.
     * @param op Operator name
     * @param elems <code>ExpressionElement</code>s that are given to the operator. This may contain:
     * <ol>
     * <li><b><code>NUMBER</code></b> - a number. If the number is unmodifiable, then it should be compiled; otherwise
     * it should be added to numberRefs and received reference should be used instead of it. See <code>numberRefs</code> for details.</li>
     * <li><b><code>SYSTEM_REFERENCE</code></b> - reference to buffer element. See <code>referenceOffset</code> for details.</li>
     * <li><b><code>EXPRESSION</code></b>. See <code>exprs</code> for details.</li>
     * </ol>
     * <hr>
     * @param bufferOffset Every <code>SystemReference.reference</code> does not refer actually to the index of <i>using</i> buffer.
     * <i>Real</i> index is the result of following expression: "<code>SystemReference.reference + bufferOffset</code>".
     * <hr>
     * @param resultIndex The result of the operation should be places in the buffer under index <code>resultIndex</code>. You do not have to
     * append <code>bufferOffset</code> to this int to get real buffer index.
     * <hr>
     * @param kit Kit
     * @param bufferRefs a list with information about some <code>Expression</code>s and <code>Number</code>s. For Expressions this includes:
     * id, buffer offset and buffer result index. For Numbers - its reference in the buffer. You do not need to
     * append <code>referenceOffset</code> to this indexes to get real buffer index.
     * To evaluate an <code>Expression</code>, call <code>compileInvokeExpression</code> method of <code>Expression</code> class. If this
     * list does not contain <code>Expression</code> this method need, then it can be created by calling <code>compileOperators</code>
     * method of the Expression. If this list does not contain modifiable <code>Number</code> this method need, then it can add its own
     * <code>Number</code> to this list
     * <hr>
     * @throws roottemplate.calculator.evaluator.EvaluatorException if exception occurs
     */
    void compileOperator(String op, ExpressionElement[] elems, int bufferOffset, int resultIndex, Object kit) throws EvaluatorException;

    /**
     * Puts a <code>Number</code> into buffer. If <code>number</code> is unmodifiable, this method gets its value now and remembers it.
     * Otherwise, <code>number</code> is put into <code>numberRefs</code> and the value will be put into buffer at runtime.
     * However, you should not call this method if the number is unmodifiable.
     * @param kit Kit
     * @param number The number
     * @param index Absolute index under which the number must be put. Can be -1, then EVMManager chooses empty index.
     * @return Index with the number
     */
    int compilePutIntoBuffer(Object kit, Number number, int index);

    /**
     * Puts the value of the <code>ref</code> index into a cell with index <code>index</code>.
     * @param kit Kit
     * @param srcIndex Reference to buffer cell from where the value is taken
     * @param dstIndex Reference to buffer cell to where the value is put
     */
    void compilePutIntoBuffer(Object kit, int srcIndex, int dstIndex);
    
    int compileGetVarCountPutIntoBufferUses(Object kit);

    /* *
     * Constructs new <code>Number</code> from a convenient form for the implementation of <code>NumberManager</code>.
     * Constructed <code>Number</code> should be placed on the top of the stack.
     * @param mv <code>MethodVisitor</code>
     * @param className Class name
     * @param index Buffer index of convenient form of the number. 
     
    void compileConstructNumber(MethodVisitor mv, String className, int index);*/
    
    /**
     * Returns <code>NumberManager</code> this <code>EVMManager</code> associated with or null if this
     * <code>EVMManager</code> is universal.
     * @return result
     */
    NumberManager getNumberManager();
}
