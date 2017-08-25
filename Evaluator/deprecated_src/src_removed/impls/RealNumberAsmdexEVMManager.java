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

/*import java.util.Map;
import org.ow2.asmdex.AnnotationVisitor;
import org.ow2.asmdex.ApplicationWriter;
import org.ow2.asmdex.ClassVisitor;
import org.ow2.asmdex.FieldVisitor;
import org.ow2.asmdex.MethodVisitor;
import org.ow2.asmdex.Opcodes;
import roottemplate.calculator.evaluator.CompiledExpression;
import roottemplate.calculator.evaluator.EVMManager;
import roottemplate.calculator.evaluator.Evaluator;
import roottemplate.calculator.evaluator.EvaluatorException;
import roottemplate.calculator.evaluator.Expression;
import roottemplate.calculator.evaluator.ExpressionElement;
import roottemplate.calculator.evaluator.ExpressionElement.ElementType;
import roottemplate.calculator.evaluator.Instruction;
import roottemplate.calculator.evaluator.MoreMath;
import roottemplate.calculator.evaluator.Number;
import roottemplate.calculator.evaluator.SystemReference;
import roottemplate.calculator.evaluator.util.BufferCmpSet;
import roottemplate.calculator.evaluator.util.CompileUtil;*/

public class RealNumberAsmdexEVMManager /* implements EVMManager, Opcodes */{
    /*private final ClassLoaderHelper clLoader;
    
    public RealNumberAsmdexEVMManager(ClassLoaderHelper clLoader) {
        this.clLoader = clLoader;
    }

    @Override
    public CompiledExpression compile(Expression expr, Evaluator.Options options) throws EvaluatorException {
        if(expr.getBufferSize() == 0)
            return new ConstantCompiledExpression((Number) expr.getResultElem());
        
        ApplicationWriter aw = new ApplicationWriter();
        aw.visit();
        
        String className = "Lroottemplate/calculator/evaluator/exprs/" + CompileUtil.nextClassName() + ";";
        ClassVisitor cv;
        MethodVisitor mv;

        cv = aw.visitClass(ACC_PUBLIC, className, null, "Lroottemplate/calculator/evaluator/impls/RealNumberCompiledExpression;", null);
        cv.visit(0, ACC_PUBLIC, className, null, "Lroottemplate/calculator/evaluator/impls/RealNumberCompiledExpression;", null);
        
        Kit kit = new Kit();
        kit.cv = cv;
        kit.className = className;
        int resultIndex = compileExpression(expr, kit);
        Map.Entry<Number[], int[]> numbersAndTheirRefs = kit.bufferIndexes.getNumbersAndRefs();
        
        {
            mv = cv.visitMethod(ACC_PUBLIC + ACC_CONSTRUCTOR, "<init>",
                    "V" +
                    "[Lroottemplate/calculator/evaluator/Number;" +
                    "Lroottemplate/calculator/evaluator/EVMManager;" +
                    "Lroottemplate/calculator/evaluator/Evaluator$Options;", null, null);
            mv.visitCode();
            mv.visitMaxs(5, 0);
            mv.visitVarInsn(INSN_CONST_16, 0, kit.bufferIndexes.getBufferSize());
            mv.visitMethodInsn(INSN_INVOKE_DIRECT, "Lroottemplate/calculator/evaluator/impls/RealNumberCompiledExpression;", "<init>", "VI[Lroottemplate/calculator/evaluator/Number;Lroottemplate/calculator/evaluator/EVMManager;Lroottemplate/calculator/evaluator/Evaluator$Options;", new int[] { 1, 0, 2, 3, 4 });
            mv.visitInsn(INSN_RETURN_VOID);
            mv.visitEnd();
        }
        {
            mv = cv.visitMethod(ACC_PUBLIC + ACC_DECLARED_SYNCHRONIZED, "eval0", "D", null, null);
            mv.visitCode();
            mv.visitMaxs(5, 0);
            mv.visitIntInsn(INSN_MONITOR_ENTER, 4);
            mv.visitFieldInsn(INSN_IGET_OBJECT, className, "b", "[D", 0, 4);
            
            int i = 0;
            //System.out.println("n[]: " + Arrays.toString(numbersAndTheirRefs.getKey()));
            //System.out.println("nm[]: " + Arrays.toString(numbersAndTheirRefs.getValue()));
            for(int n : numbersAndTheirRefs.getValue()) {
                mv.visitVarInsn(INSN_CONST_4, 1, n); // Buffer index
                mv.visitFieldInsn(INSN_IGET_OBJECT, className, "n", "[Lroottemplate/calculator/evaluator/Number;", 2, 4);
                mv.visitVarInsn(INSN_CONST_4, 3, i); // n index
                mv.visitArrayOperationInsn(INSN_AGET_OBJECT, 2, 2, 3);
                mv.visitMethodInsn(INSN_INVOKE_VIRTUAL, "Lroottemplate/calculator/evaluator/Number;", "doubleValue", "D", new int[] { 2 });
                mv.visitIntInsn(INSN_MOVE_RESULT_WIDE, 2);
                mv.visitArrayOperationInsn(INSN_APUT_WIDE, 2, 0, 1);
                i++;
            }
            
            mv.visitMethodInsn(INSN_INVOKE_DIRECT, className, "e0", "V", new int[] { 4 });
            mv.visitVarInsn(INSN_CONST_16, 1, resultIndex);
            mv.visitArrayOperationInsn(INSN_AGET_WIDE, 2, 0, 1);
            mv.visitIntInsn(INSN_MONITOR_EXIT, 4);
            mv.visitIntInsn(INSN_RETURN_WIDE, 2);
            mv.visitIntInsn(INSN_MOVE_EXCEPTION, 1);
            mv.visitIntInsn(INSN_MONITOR_EXIT, 4);
            mv.visitIntInsn(INSN_THROW, 1);
            mv.visitEnd();
        }
        
        cv.visitEnd();
        aw.visitEnd();
        byte[] bytes = aw.toByteArray();
        System.out.println("Bytes: " + bytesToHex(bytes));
        try {
            Object clInstance = clLoader.loadClass(bytes, className.substring(1, className.length() - 1).replaceAll("/", "."))
                    .getConstructor(Number[].class, EVMManager.class, Evaluator.Options.class)
                    .newInstance(numbersAndTheirRefs.getKey(), this, options);
            return (CompiledExpression) clInstance;
        } catch (Exception ex) {
            throw new EvaluatorException(ex);
        }
    }
    
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    @Override
    public int compileExpression(Expression expr, Object kit_) throws EvaluatorException {
        Kit kit = (Kit) kit_;
        BufferCmpSet buffer = kit.bufferIndexes;
        int exprId = buffer.getId(expr);
        
        if(exprId == -1) {
            ExpressionElement result = expr.getResultElem();
            if(result.getElementType() == ElementType.NUMBER)
                return compilePutIntoBuffer(kit_, (Number) result, -1);

            MethodVisitor lastMv = kit.mv;
            int lastThisR = kit.thisR;
            //System.out.println(expr);

            int bufferOffset = buffer.add(expr);
            if(bufferOffset == -1)
                throw new EvaluatorException("BufferCmpSet cannot register Expression. Maybe, Expression can be compressed to a Number?");

            exprId = buffer.getId(expr);
            MethodVisitor mv = kit.mv = kit.cv.visitMethod(ACC_PRIVATE, "e" + exprId, "V", null, null);
            //System.out.println("Method e" + exprId + "()");
            mv.visitCode();

            Instruction[] insns = expr.getInstructions();
            int registers = 0;
            for(Instruction insn : insns) {
                int uses = insn.op.getVarCountOperatorUses(insn.elems, null, this);
                if(registers < uses)
                    registers = uses;
            }
            int thisR = kit.thisR = 2 + registers * 2; // "this" register
            //System.out.println("ThisR = " + thisR);

            mv.visitMaxs(1 + thisR, 0);
            mv.visitFieldInsn(INSN_IGET_OBJECT, kit.className, "b", "[D", 0, thisR);

            for(Instruction insn : insns) {
                insn.op.compile(insn.elems, bufferOffset, bufferOffset + insn.resultIndex, kit, this);
            }

            mv.visitInsn(INSN_RETURN_VOID);
            mv.visitEnd();
            kit.mv = lastMv;
            kit.thisR = lastThisR;
        }
        
        if(kit.mv != null)
            kit.mv.visitMethodInsn(INSN_INVOKE_DIRECT, kit.className, "e" + exprId, "V", new int[] { kit.thisR });
        
        return buffer.getResultReference(expr);
    }

    @Override
    public void compileOperator(String op, ExpressionElement[] elems, int bufferOffset, int resultIndex, Object kit_) throws EvaluatorException {
        if(!op.equals("root") && !op.equals("-")) {
            if(op.length() == 1 && !op.equals("!") && elems.length != 2)
                throw new EvaluatorException("Operator " + op + " uses TWO numbers, but got " + elems.length);
            if((op.length() > 1 || op.equals("!")) && elems.length != 1)
                throw new EvaluatorException("Operator " + op + " uses ONE number, but got " + elems.length);
        }
        
        Kit kit = (Kit) kit_;
        MethodVisitor mv = kit.mv;
        BufferCmpSet buffer = kit.bufferIndexes;
        //System.out.println("Comp. op = " + op + "; el = " + Arrays.toString(elems));
        
        int register = 2;
        for(ExpressionElement elem : elems) {
            int reference = -1;
            switch(elem.getElementType()) {
                case NUMBER:
                    Number n = (Number) elem;
                    if(!n.isModifiable())
                        mv.visitVarInsn(INSN_CONST_WIDE_HIGH16, register, Double.doubleToLongBits(n.doubleValue()));
                    else
                        reference = buffer.add(n);
                    break;
                case SYSTEM_REFERENCE:
                    reference = ((SystemReference) elem).reference + bufferOffset;
                    break;
            }

            if(reference != -1) {
                //System.out.println("Elem " + elem + " at ref. " + reference + " register = " + register);
                mv.visitVarInsn(INSN_CONST_16, register, reference);
                mv.visitArrayOperationInsn(INSN_AGET_WIDE, register, 0, register);
            }
            
            register += 2;
        }
        
        // Result register = 2
        switch(op) {
            case "+": mv.visitOperationInsn(INSN_ADD_DOUBLE_2ADDR, 2, 2, 4, 0); break;
            case "-":
                if(elems.length == 2)
                    mv.visitOperationInsn(INSN_SUB_DOUBLE_2ADDR, 2, 2, 4, 0);
                else
                    mv.visitOperationInsn(INSN_NEG_DOUBLE, 2, 2, 0, 0);
                break;
            case "*": mv.visitOperationInsn(INSN_MUL_DOUBLE_2ADDR, 2, 2, 4, 0); break;
            case "/": mv.visitOperationInsn(INSN_DIV_DOUBLE_2ADDR, 2, 2, 4, 0); break;
            case "%": mv.visitOperationInsn(INSN_REM_DOUBLE_2ADDR, 2, 2, 4, 0); break;

            case "round":
                mv.visitMethodInsn(INSN_INVOKE_STATIC, "Ljava/lang/Math;", "round", "JD", new int[] { 2, 3 });
                mv.visitIntInsn(INSN_MOVE_RESULT_WIDE, 2);
                mv.visitOperationInsn(INSN_LONG_TO_DOUBLE, 2, 2, 0, 0);
                break;
            case "toRadians":
            case "toDegrees":
            case "log10":
            case "log":
            case "abs":
            case "sqrt":
            case "cbrt":
                mv.visitMethodInsn(INSN_INVOKE_STATIC, "Ljava/lang/Math;", op, "DD", new int[] { 2, 3 });
                mv.visitIntInsn(INSN_MOVE_RESULT_WIDE, 2);
                break;
            case "^":
                mv.visitMethodInsn(INSN_INVOKE_STATIC, "Ljava/lang/Math;", "pow", "DDD", new int[] { 2, 3, 4, 5 });
                mv.visitIntInsn(INSN_MOVE_RESULT_WIDE, 2);
                break;

            case "root":
                if(elems.length == 1) {
                    mv.visitVarInsn(INSN_CONST_WIDE, register, 2L);
                    register += 2;
                } else {
                    mv.visitMethodInsn(INSN_INVOKE_STATIC, "Ljava/lang/Math;", "round", "JD", new int[] { 4, 5 });
                    mv.visitIntInsn(INSN_MOVE_RESULT_WIDE, 4);
                }
                mv.visitMethodInsn(INSN_INVOKE_STATIC, "Lroottemplate/calculator/evaluator/MoreMath;", "root", "DDJ", new int[] { 2, 3, 4, 5 });
                mv.visitIntInsn(INSN_MOVE_RESULT_WIDE, 2);
                break;
            case "!":
                mv.visitMethodInsn(INSN_INVOKE_STATIC, "Lroottemplate/calculator/evaluator/MoreMath;", "factorial", "DD", new int[] { 2, 3 });
                mv.visitIntInsn(INSN_MOVE_RESULT_WIDE, 2);
                break;
                
            case "sin":
            case "cos":
            case "tan":
            case "asin":
            case "acos":
            case "atan":
                mv.visitFieldInsn(INSN_IGET_OBJECT, kit.className, "opt", "Lroottemplate/calculator/evaluator/Evaluator$Options;", 1, kit.thisR);
                mv.visitFieldInsn(INSN_IGET, "Lroottemplate/calculator/evaluator/Evaluator$Options;", "ANGLE_MEASURING_UNITS", "I", 1, 1);
                mv.visitMethodInsn(INSN_INVOKE_STATIC, "Lroottemplate/calculator/evaluator/impls/RealNumberAsmdexEVMManager;",
                        "execTrOp" + op, "DID", new int[] { 1, 2, 3 });
                mv.visitIntInsn(INSN_MOVE_RESULT_WIDE, 2);
                break;
                
            default:
                throw new EvaluatorException("Unknown operator " + op);
        }

        mv.visitVarInsn(INSN_CONST_16, 1, resultIndex);
        mv.visitArrayOperationInsn(INSN_APUT_WIDE, 2, 0, 1);
        //System.out.println("Result index = " + resultIndex);
    }

    @Override
    public int compilePutIntoBuffer(Object kit_, Number number, int index) {
        Kit kit = (Kit) kit_;
        MethodVisitor mv = kit.mv;
        
        if(!number.isModifiable()) {
            mv.visitVarInsn(INSN_CONST_WIDE_HIGH16, 2, Double.doubleToLongBits(number.doubleValue()));
            if(index == -1)
                index = kit.bufferIndexes.reserveNextIndex();
        } else {
            int nIndex = kit.bufferIndexes.add(number);
            if(index == -1 || nIndex == index) return nIndex;
            mv.visitVarInsn(INSN_CONST_16, 2, nIndex);
            mv.visitArrayOperationInsn(INSN_AGET_WIDE, 2, 0, 2);
        }
        
        mv.visitVarInsn(INSN_CONST_16, 4, index);
        mv.visitArrayOperationInsn(INSN_APUT_WIDE, 2, 0, 4);
        return index;
    }

    @Override
    public void compilePutIntoBuffer(Object kit, int ref, int destIndex) {
        if(ref == destIndex) return;
        MethodVisitor mv = ((Kit) kit).mv;
        
        mv.visitVarInsn(INSN_CONST_16, 2, ref);
        mv.visitArrayOperationInsn(INSN_AGET_WIDE, 2, 0, 2);
        mv.visitVarInsn(INSN_CONST_16, 4, destIndex);
        mv.visitArrayOperationInsn(INSN_APUT_WIDE, 2, 0, 4);
    }

    @Override
    public int compileGetVarCountOperatorUses(String op, ExpressionElement[] elems, Object kit) {
        if(op.equals("root")) {
            return 2;
        } else
            return elems.length;
    }

    @Override
    public int compileGetVarCountPutIntoBufferUses(Object kit) {
        return 2;
    }

    @Override
    public Number.NumberManager getNumberManager() {
        return RealNumber.NUMBER_MANAGER;
    }
    
    
    
    public static interface ClassLoaderHelper {
        Class<?> loadClass(byte[] bytes, String className);
    }
    private static class Kit {
        public ClassVisitor cv;
        public MethodVisitor mv = null;
        public BufferCmpSet bufferIndexes = new BufferCmpSet();
        public String className;
        public int thisR = -1;
        public int register = -1;
    }
    
    private static double execTrOp(int amu, double x, int op) {
        if(amu == 2)
            x = Math.toRadians(x);
        switch(op) {
            case 0: return MoreMath.sin(x);
            case 1: return MoreMath.cos(x);
            case 2: return MoreMath.tan(x);
            default: throw new Error();
        }
    }
    private static double execTrOpa(int amu, double x) {
        if(amu == 2)
            x = Math.toDegrees(x);
        return x;
    }
    public static double execTrOpsin(int amu, double x) { return execTrOp(amu, x, 0); }
    public static double execTrOpcos(int amu, double x) { return execTrOp(amu, x, 1); }
    public static double execTrOptan(int amu, double x) { return execTrOp(amu, x, 2); }
    public static double execTrOpasin(int amu, double x) { return execTrOpa(amu, Math.asin(x)); }
    public static double execTrOpacos(int amu, double x) { return execTrOpa(amu, Math.acos(x)); }
    public static double execTrOpatan(int amu, double x) { return execTrOpa(amu, Math.atan(x)); }
    */
}
