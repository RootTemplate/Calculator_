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
import roottemplate.calculator.evaluator.AnalyzingExpression;
import roottemplate.calculator.evaluator.EVMCompiler;
import roottemplate.calculator.evaluator.Evaluator;
import roottemplate.calculator.evaluator.EvaluatorException;
import roottemplate.calculator.evaluator.ExpressionElement;
import roottemplate.calculator.evaluator.Instruction;
import roottemplate.calculator.evaluator.Number;
import roottemplate.calculator.evaluator.Reader;
import roottemplate.calculator.evaluator.SystemReference;
import roottemplate.calculator.evaluator.util.IndexedString;
import roottemplate.calculator.evaluator.util.ListStruct;

public class StandardExpression extends AnalyzingExpression {
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
            beforeNode = list_.preudoAdd(beforeNode, elem);
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
    private boolean gap = false;
    
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
    
    
    /*@Override
    public Object createGapKit() {
        Object[] kits = new Object[instructions.length];
        for(int i = 0; i < kits.length; i++) {
            kits[i] = instructions[i].op.createGapKit();
        }
        return kits;
    }
    
    @Override
    public Number gapEval(Object kits_, boolean rightSide) throws EvaluatorException {
        gap = false;
        
        if(instructions != null) {
            Object[] kits = (Object[]) kits_;
            for(int i = 0; i < instructions.length; i++) {
                Instruction insn = instructions[i];
                buffer[insn.resultIndex] = insn.op.gapEval(kits[i], rightSide, insn.genNsArray(buffer));
                if(insn.op.wasGap(kits[i], insn.cacheArray))
                    gap = true;
            }

            if(result.getElementType() == ExpressionElement.ElementType.SYSTEM_REFERENCE)
                return buffer[((SystemReference) result).reference];
        }

        return ((Number) result).isModifiable() ? ((Number) result).copy() : (Number) result;
    }
    
    @Override
    public boolean wasGap(Object kit) {
        return gap;
    }*/
    
    
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
        boolean first = true;
        for(Instruction obj : instructions) {
            if(first)
                first = false;
            else
                sb.append(", ");
            sb.append(obj.toString());
        }
        sb.append(", result: ").append(result);
        return sb.append("}").toString();
    }

    @Override
    public EVMCompiler getEVMManager() {
        return null;
    }
    
    
    
    /*public Expression compile() throws CompileException {
        if(compiledExpr != null) return compiledExpr;
        
        if(instructions == null || instructions.length == 0)
            return new ConstantCompiledExpression((Number) result);
        
        NumberManager nm = numberManager == null ? anyModifNumber.getNumberManager() : numberManager;
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        //ClassVisitor cw = new CheckClassAdapter(cw_);
        FieldVisitor fv;
        MethodVisitor mv;

        String className = "roottemplate/calculator/evaluator/exprs/" + CompileUtil.nextClassName();
        cw.visit(V1_7, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object",
                new String[] { "roottemplate/calculator/evaluator/CompiledExpression" });
        
        BufferCmpSet bufferRefs = new BufferCmpSet();
        
        { // Array of modifiable numbers (given in constructor)
            fv = cw.visitField(ACC_PRIVATE + ACC_FINAL, "n", "[Lroottemplate/calculator/evaluator/Number;", null, null);
            fv.visitEnd();
        }
        { // Array of modifiable numbers references
            fv = cw.visitField(ACC_PRIVATE + ACC_FINAL, "nm", "[I", null, null);
            fv.visitEnd();
        }
        compileOperators(cw, className, bufferRefs);
        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "([Lroottemplate/calculator/evaluator/Number;[I)V", null, null);
            mv.visitCode();
            nm.compileInitBuffer(cw, mv, className, bufferRefs.getBufferSize());
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitFieldInsn(PUTFIELD, className, "n", "[Lroottemplate/calculator/evaluator/Number;");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitFieldInsn(PUTFIELD, className, "nm", "[I");
            mv.visitInsn(RETURN);
            mv.visitMaxs(30, 30);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC + ACC_SYNCHRONIZED, "eval", "()Lroottemplate/calculator/evaluator/Number;", null,
                    new String[] { "roottemplate/calculator/evaluator/EvaluatorException" });
            mv.visitCode();
            
            nm.compileInitLocalBuffer(mv, className);
            nm.compileSetupEvalWorkspace(mv, className);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, className, "e0", "()V", false);
            nm.compileConstructNumber(mv, className, bufferRefs.getResultReference(this));
            mv.visitInsn(ARETURN);
            
            mv.visitMaxs(30, 30);
            mv.visitEnd();
        }
        cw.visitEnd();
        
        /*try {
            new FileOutputStream(new File("C:\\Users\\Gleb\\Desktop\\folder\\t.class")).write(cw_.toByteArray());
        } catch (Exception ex) {
            ex.printStackTrace();
        }* /
        Class<?> c = CLASS_LOADER.defineClass(className, cw.toByteArray());
        Map.Entry<Number[], int[]> nArrs = bufferRefs.getNumbersAndRefs();
        //System.out.println(Arrays.toString(nArrs.getKey()));
        //System.out.println(Arrays.toString(nArrs.getValue()));
        
        try {
            compiledExpr = (Expression) c.getConstructor(Number[].class, int[].class)
                    .newInstance((Object) nArrs.getKey(), (Object) nArrs.getValue());
            return compiledExpr;
        } catch (Exception ex) {
            throw new CompileException(ex);
        }
    }
    public int compileOperators(ClassVisitor cw, String className, BufferCmpSet bufferRefs) throws CompileException {
        int id = bufferRefs.getId(this);
        if(id != -1) return id;
        
        int bufferOffset = bufferRefs.add(this);
        id = bufferRefs.getId(this);
        NumberManager nm = numberManager == null ? anyModifNumber.getNumberManager() : numberManager;
        
        MethodVisitor mv = cw.visitMethod(ACC_PRIVATE, "e" + id, "()V", null, null);
        mv.visitCode();
        nm.compileInitLocalBuffer(mv, className);
        
        if(instructions != null)
            for(Instruction instr : instructions) {
                int i = 0;
                ExpressionElement[] elems = new ExpressionElement[instr.cacheArray.length];
                for(ExpressionElement e : instr.elems)
                    if(e.getElementType() == ElementType.EXPRESSION_ENUM)
                        for(Expression ex : ((ExpressionEnum) e).enum_)
                            elems[i++] = ex;
                    else
                        elems[i++] = e;
                instr.op.compile(elems, bufferOffset, instr.resultIndex + bufferOffset, cw, mv, className, bufferRefs, nm);
            }
        
        switch(result.getElementType()) {
            case NUMBER:
                nm.compilePutIntoBuffer(mv, (Number) result, bufferOffset, bufferRefs);
                break;
            case EXPRESSION:
                Expression e = (Expression) result;
                compileInvokeExpression(mv, className, e.compileOperators(cw, className, bufferRefs));
                break;
        }
        
        mv.visitInsn(RETURN);
        mv.visitMaxs(30, 30);
        mv.visitEnd();
        return id;
    }
    public static void compileInvokeExpression(MethodVisitor mv, String className, int exprId) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, className, "e" + exprId, "()V", false);
    }*/
    
}
