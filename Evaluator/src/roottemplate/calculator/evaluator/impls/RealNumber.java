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

import roottemplate.calculator.evaluator.EvaluatorException;
import roottemplate.calculator.evaluator.MoreMath;
import roottemplate.calculator.evaluator.Number;
import roottemplate.calculator.evaluator.util.IndexedString;

public class RealNumber extends Number {
    public static final NumberManager NUMBER_MANAGER = new RealNumberManager();
    
    public static RealNumber parse(IndexedString expr) throws EvaluatorException {
        return parse(expr.toString());
    }
    public static RealNumber parse(String expr) throws EvaluatorException {
        try {
            return new RealNumber(Double.parseDouble(expr));
        } catch(NumberFormatException ex) {
            throw new EvaluatorException("Bad number: \"" + expr + "\"");
        }
    }
    
    private double number;
    private final boolean isModifiable;
    
    public RealNumber(double n) {
        number = n;
        isModifiable = false;
    }
    protected RealNumber(double n, boolean isModifiable) {
        number = n;
        this.isModifiable = isModifiable;
    }
    
    @Override
    public boolean isModifiable() {
        return isModifiable;
    }
    
    protected void setValue(double n) {
        if(!isModifiable) throw new UnsupportedOperationException();
        number = n;
    }
    protected void addDelta(double n) {
        if(!isModifiable) throw new UnsupportedOperationException();
        number += n;
    }

    @Override
    public double doubleValue() {
        return number;
    }
    
    @Override
    public Number applyOperation(String operation, Number with) {
        double n = with == null ? 0 : with.doubleValue();
        switch(operation) {
            case "+": return new RealNumber(number + n);
            case "-": 
                if(with == null)
                    return new RealNumber(-number);
                else
                    return new RealNumber(number - n);
            case "*": return new RealNumber(number * n);
            case "/": return new RealNumber(number / n);
            case "%": return new RealNumber(number % n);
            case "^": return new RealNumber(Math.pow(number, n));
            case "!": return new RealNumber(MoreMath.factorial(number));
                
            case "toRadians": return new RealNumber(Math.toRadians(number));
            case "toDegrees": return new RealNumber(Math.toDegrees(number));
            case "log10": return new RealNumber(Math.log10(number));
            case "log":
                if(with == null)
                    return new RealNumber(Math.log(number));
                return new RealNumber(MoreMath.log(number, n));
            case "abs": return new RealNumber(Math.abs(number));
            case "round": return new RealNumber(Math.round(number));
            case "floor": return new RealNumber(Math.floor(number));
            case "ceil": return new RealNumber(Math.ceil(number));
            case "sqrt": return new RealNumber(Math.sqrt(number));
            case "cbrt": return new RealNumber(Math.cbrt(number));
            case "sin": return new RealNumber(MoreMath.sin(number));
            case "cos": return new RealNumber(MoreMath.cos(number));
            case "tan": return new RealNumber(MoreMath.tan(number));
            case "asin": return new RealNumber(Math.asin(number));
            case "acos": return new RealNumber(Math.acos(number));
            case "atan": return new RealNumber(Math.atan(number));
            case "root": return new RealNumber(MoreMath.root(number, Math.round(with == null ? 2 : n)));
            default: throw new IllegalArgumentException("Operation " + operation + " is undefined");
        }
    }

    @Override
    public Number copy() {
        return new RealNumber(number);
    }
    
    @Override
    public Number toNumber() {
        return this;
    }

    @Override
    public NumberManager getNumberManager() {
        return NUMBER_MANAGER;
    }
    
    
    
    
    
    public static class RealNumberManager extends NumberManager /*implements Opcodes*/ {
        @Override
        public int getAbstractionLevel() {
            return 1;
        }

        @Override
        public roottemplate.calculator.evaluator.Number cast(roottemplate.calculator.evaluator.Number number) {
            throw new UnsupportedOperationException("There are no numbers that have lower abstraction levels than Real");
        }

        /*@Override
        public void compileInitBuffer(ClassVisitor cw, MethodVisitor initMw, String className, int bufferLength) {
            cw.visitField(ACC_PRIVATE + ACC_FINAL, "b", "[D", null, null).visitEnd(); // double[] b; - buffer

            initMw.visitVarInsn(ALOAD, 0);
            initMw.visitLdcInsn(bufferLength);
            initMw.visitIntInsn(NEWARRAY, T_DOUBLE);
            initMw.visitFieldInsn(PUTFIELD, className, "b", "[D");
        }

        @Override
        public void compileInitLocalBuffer(MethodVisitor mv, String className) {
            mv.visitVarInsn(ALOAD, 0); // this
            mv.visitFieldInsn(GETFIELD, className, "b", "[D");
            mv.visitVarInsn(ASTORE, 1); // double[] b = this.b; b is at local variable #1
        }

        @Override
        public void compileSetupEvalWorkspace(MethodVisitor mv, String className) {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, className, "nm", "[I");
            mv.visitVarInsn(ASTORE, 2);
            mv.visitInsn(ICONST_0); // int i = 0;
            mv.visitVarInsn(ISTORE, 3); // i to local v. #3
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitFrame(Opcodes.F_APPEND, 3, new Object[] {"[D", "[I", Opcodes.INTEGER}, 0, null);
            mv.visitVarInsn(ILOAD, 3);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, className, "n", "[Lroottemplate/calculator/evaluator/Number;");
            mv.visitInsn(ARRAYLENGTH);
            Label l1 = new Label();
            mv.visitJumpInsn(IF_ICMPGE, l1); // if(i >= n.length) goto l1;
            mv.visitVarInsn(ALOAD, 1); // b
            mv.visitVarInsn(ALOAD, 2); // nm
            mv.visitVarInsn(ILOAD, 3); // i
            mv.visitInsn(IALOAD);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, className, "n", "[Lroottemplate/calculator/evaluator/Number;");
            mv.visitVarInsn(ILOAD, 3);
            mv.visitInsn(AALOAD); // n[i]
            mv.visitMethodInsn(INVOKEVIRTUAL, "roottemplate/calculator/evaluator/Number", "doubleValue", "()D", false);
            mv.visitInsn(DASTORE); // b[nm[i]] = n[i].doubleValue();
            mv.visitIincInsn(3, 1);
            mv.visitJumpInsn(GOTO, l0);
            mv.visitLabel(l1);
            mv.visitFrame(Opcodes.F_CHOP, 1, null, 0, null);
        }

        @Override
        public void compileOperator(String op, ExpressionElement[] elems, int bufferOffset, int resultId, ClassVisitor cw, MethodVisitor mv,
                String className, BufferCmpSet bufferRefs) throws CompileException {
            if(elems.length > 2 || elems.length == 0)
                throw new CompileException("Bad elements length: " + elems.length);

            mv.visitVarInsn(ALOAD, 1);
            mv.visitLdcInsn(resultId);

            for(ExpressionElement elem : elems) {
                int reference = -1;
                switch(elem.getElementType()) {
                    case NUMBER:
                        roottemplate.calculator.evaluator.Number n = (roottemplate.calculator.evaluator.Number) elem;
                        if(!n.isModifiable())
                            mv.visitLdcInsn(n.doubleValue());
                        else
                            reference = bufferRefs.add(n);
                        break;
                    case SYSTEM_REFERENCE:
                        reference = ((SystemReference) elem).reference + bufferOffset;
                        break;
                    case EXPRESSION:
                        Expression e = (Expression) elem;
                        Expression.compileInvokeExpression(mv, className, e.compileOperators(cw, className, bufferRefs));
                        reference = bufferRefs.getResultReference(e);
                        break;
                }

                if(reference != -1) {
                    mv.visitVarInsn(ALOAD, 1);
                    mv.visitLdcInsn(reference);
                    mv.visitInsn(DALOAD);
                }
            }

            switch(op) {
                case "+": mv.visitInsn(DADD); break;
                case "-":
                    if(elems.length == 2)
                        mv.visitInsn(DSUB);
                    else
                        mv.visitInsn(DNEG);
                    break;
                case "*": mv.visitInsn(DMUL); break;
                case "/": mv.visitInsn(DDIV); break;
                case "%": mv.visitInsn(DREM); break;

                case "round":
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", op, "(D)J", false);
                    mv.visitInsn(L2D);
                    break;
                case "toRadians":
                case "toDegrees":
                case "log10":
                case "log":
                case "abs":
                case "sqrt":
                case "cbrt":
                case "asin":
                case "acos":
                case "atan":
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", op, "(D)D", false);
                    break;
                case "^":
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
                    break;

                case "root":
                    if(elems.length == 1)
                        mv.visitLdcInsn(2L);
                    else
                        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "round", "(D)J", false);
                    mv.visitMethodInsn(INVOKESTATIC, "roottemplate/calculator/evaluator/MoreMath", "root", "(DJ)D", false);
                    break;
                case "sin":
                case "cos":
                case "tan":
                    mv.visitMethodInsn(INVOKESTATIC, "roottemplate/calculator/evaluator/MoreMath", op, "(D)D", false);
                    break;
                case "!":
                    mv.visitMethodInsn(INVOKESTATIC, "roottemplate/calculator/evaluator/MoreMath", "factorial", "(D)D", false);
                    break;
            }

            mv.visitInsn(DASTORE);
        }

        @Override
        public void compilePutIntoBuffer(MethodVisitor mv, roottemplate.calculator.evaluator.Number number, int index, BufferCmpSet bufferRefs) {
            mv.visitVarInsn(ALOAD, 1);
            mv.visitLdcInsn(index);
            if(!number.isModifiable())
                mv.visitLdcInsn(number.doubleValue());
            else {
                mv.visitVarInsn(ALOAD, 1);
                mv.visitLdcInsn(bufferRefs.add(number));
                mv.visitInsn(DALOAD);
            }
            mv.visitInsn(DASTORE);
        }

        @Override
        public void compilePutIntoBuffer(MethodVisitor mv, int ref, int index) {
            mv.visitVarInsn(ALOAD, 1);
            mv.visitLdcInsn(index);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitLdcInsn(ref);
            mv.visitInsn(DALOAD);
            mv.visitInsn(DASTORE);
        }

        @Override
        public void compileConstructNumber(MethodVisitor mv, String className, int index) {
            mv.visitTypeInsn(NEW, "roottemplate/calculator/evaluator/impls/RealNumber");
            mv.visitInsn(DUP);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitLdcInsn(index);
            mv.visitInsn(DALOAD);
            mv.visitMethodInsn(INVOKESPECIAL, "roottemplate/calculator/evaluator/impls/RealNumber", "<init>", "(D)V", false);
        }*/
    }
}
