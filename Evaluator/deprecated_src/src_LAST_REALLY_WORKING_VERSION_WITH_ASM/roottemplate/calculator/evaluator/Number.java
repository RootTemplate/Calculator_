package roottemplate.calculator.evaluator;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import roottemplate.calculator.evaluator.Reader.IndexedString;
import roottemplate.calculator.evaluator.impls.RealNumber;
import roottemplate.calculator.evaluator.util.BufferCmpSet;
import roottemplate.calculator.evaluator.util.Util;

public abstract class Number extends java.lang.Number implements ExpressionElement {
    
    /**
     * Checks if this number can change its value at runtime
     * @return true if this number is modifiable
     */
    public abstract boolean isModifiable();
    
    public abstract NumberManager getNumberManager();
    
    @Override
    public final ElementType getElementType() {
        return ElementType.NUMBER;
    }
    
    @Override
    public long longValue() {
        return Math.round(doubleValue());
    }

    @Override
    public float floatValue() {
        return (float) doubleValue();
    }

    @Override
    public int intValue() {
        return (int) longValue();
    }
    
    /**
     * @return Real this number
     */
    public abstract Number toNumber();
    
    /**
     * Generates and returns a printable string of the value
     * @return the stringed value
     */
    public String stringValue() {
        return Util.doubleToString(doubleValue());
    }
    
    /**
     * Applies arithmetic operation on this number. Supported operations:
     * "+", "-" (minus), "-" (negate; <code>with</code> must be <code>null</code>), "*", "/", "%" (modulo), "^" (pow), "!",
     * "toRadians", "toDegrees", "log10", "log", "abs", "round", "sqrt", "cbrt", "sin", "cos", "tan", "asin", "acos", "atan",
     * "root" (returns the <code>with</code>-th root of this number; <code>with</code> must be equivalent to mathematical integer,
     * otherwise it will be rounded).
     * @param operation The operation to be applied
     * @param with Number with that operation will be applied
     * @return Result number
     * @throws ClassCastException if this and <code>with</code> numbers have different abstraction levels
     * (but it is not guaranteed)
     */
    public abstract Number applyOperation(String operation, Number with);
    
    public Number applyOperation(String operation) {
        return applyOperation(operation, null);
    }

    /**
     * Creates a copy (clone) of this number. Returned number is unmodifiable and has the same value as this number
     * @return a copy of this number
     */
    public abstract Number copy();

    @Override
    public String toString() {
        return "Number {value: " + doubleValue() + "}";
    }
    
    
    
    public static abstract class NumberManager {
        /**
         * Returns number abstraction level. For example, complex numbers
         * have higher level than real numbers.
         * @return number abstraction level
         */
        public abstract int getAbstractionLevel();
        
        /**
         * Casts a number with lower abstraction level to number with the same
         * abstraction level as this one.
         * @param number The number needed to be cast
         * @return Casted number
         */
        public abstract Number cast(Number number);
        
        
        
        /**
         * Initialize buffer. This method:
         * <ol>
         * <li>Initializes buffer field(s) by getting <code>FieldVisitor</code></li>
         * <li>Allocates buffer in constructor method</li>
         * </ol>
         * @param cw ClassWriter
         * @param initMw MethodVisitor to <code>&lt;init&gt;</code> method (constructor method)
         * @param className The name of visiting class
         * @param bufferLength Buffer length
         */
        public abstract void compileInitBuffer(ClassVisitor cw, MethodVisitor initMw, String className, int bufferLength);
        
        /**
         * Initialize buffer as a local variable. It declares local buffer variable in method, <code>MethodVisitor</code> of whom is given,
         * that refers to field buffer. This is necessary to reduce the number of instructions getting field buffer.
         * @param mv MethodVisitor
         * @param className The name of visiting class
         */
        public abstract void compileInitLocalBuffer(MethodVisitor mv, String className);
        
        /**
         * Sets up workspace in method <code>eval</code>. This method converts every <code>Number</code> from a class field "<code>n</code>" type
         * <code>Number[]</code> to a convenient form for the implementation of <code>NumberManager</code>, which must be stored to local buffer.
         * Indexes of this convenient forms in the buffer are stored in an <code>int[]</code> field of the class called "nm".
         * @param mv <code>eval()</code> method visitor
         * @param className The name of visiting class
         */
        public abstract void compileSetupEvalWorkspace(MethodVisitor mv, String className);

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
         * @param referenceOffset Every <code>SystemReference.reference</code> does not refer actually to the index of <i>using</i> buffer.
         * <i>Real</i> index is the result of following expression: "<code>SystemReference.reference + bufferOffset</code>".
         * <hr>
         * @param resultId The result of the operation should be places in the buffer under index <code>resultId</code>. You do not have to
         * append <code>bufferOffset</code> to this int to get real buffer index.
         * <hr>
         * @param cw <code>ClassWriter</code>
         * @param mv <code>MethodVisitor</code>
         * @param className Class name
         * <hr>
         * @param bufferRefs a list with information about some <code>Expression</code>s and <code>Number</code>s. For Expressions this includes:
         * id, buffer offset and buffer result index. For Numbers - its reference in the buffer. You do not need to
         * append <code>referenceOffset</code> to this indexes to get real buffer index.
         * To evaluate an <code>Expression</code>, call <code>compileInvokeExpression</code> method of <code>Expression</code> class. If this
         * list does not contain <code>Expression</code> this method need, then it can be created by calling <code>compileOperators</code>
         * method of the Expression. If this list does not contain modifiable <code>Number</code> this method need, then it can add its own
         * <code>Number</code> to this list
         * <hr>
         * @throws roottemplate.calculator.evaluator.CompileEvaluatorException if exception occurs
         */
        public abstract void compileOperator(String op, ExpressionElement[] elems, int referenceOffset, int resultId, ClassVisitor cw, MethodVisitor mv,
                String className, BufferCmpSet bufferRefs) throws CompileEvaluatorException;

        /**
         * Puts a <code>Number</code> into buffer. If <code>number</code> is unmodifiable, this method gets its value now and remembers it.
         * Otherwise, <code>number</code> is put into <code>numberRefs</code> and the value will be put into buffer at runtime.
         * @param mv <code>MethodVisitor</code>
         * @param number The number
         * @param index Absolute index under which the number must be put
         * @param bufferRefs bufferRefs
         */
        public abstract void compilePutIntoBuffer(MethodVisitor mv, Number number, int index, BufferCmpSet bufferRefs);
        
        /**
         * Puts the value of the <code>ref</code> index into a cell with index <code>index</code>.
         * @param mv <code>MethodVisitor</code>
         * @param ref Reference to buffer cell from where the value is taken
         * @param index Reference to buffer cell to where the value is put
         */
        public abstract void compilePutIntoBuffer(MethodVisitor mv, int ref, int index);

        /**
         * Constructs new <code>Number</code> from a convenient form for the implementation of <code>NumberManager</code>.
         * Constructed <code>Number</code> should be placed on the top of the stack.
         * @param mv <code>MethodVisitor</code>
         * @param className Class name
         * @param index Buffer index of convenient form of the number. 
         */
        public abstract void compileConstructNumber(MethodVisitor mv, String className, int index);
    }
    
    
    
    static class NumberReader extends Modificator {
        //public static final Pattern numberStart = Pattern.compile("(\\+|-|)(\\.|)\\d"); // only real numbers
    
        @Override
        public int getModifies() {
            return MODIFIES_READER | MODIFIES_PREEVALUATOR;
        }
        
        @Override
        public Reader.ReadResult<? extends ExpressionElement> read(IndexedString expr, Evaluator namespace, int i) throws EvaluatorException {
            /*Matcher m = numberStart.matcher(expr);
            if (!m.find()) return null;
            if(m.start() != 0) return null;*/
            
            int j;
            boolean gotPoint = false;
            boolean gotE = false;
            boolean gotSign = false;
            boolean gotDigit = false;
            for(j = 0; j < expr.length(); j++) {
                char at = expr.charAt(j);
                if(at == '+' || at == '-') {
                    if(gotSign) break;
                    gotSign = true;
                } else if(at == '.') {
                    if(gotPoint)
                        throw new EvaluatorException("Two points in one number. At: " + i);
                    else if(gotE)
                        throw new EvaluatorException("In exponent section points are prohibited");
                    else {
                        gotPoint = true;
                    }
                } else if(Character.isDigit(at)) {
                    gotSign = true;
                    gotDigit = true;
                } else if(!gotDigit)
                    return null; // Not a number
                else if(at == 'E') {
                    if(gotE)
                        throw new EvaluatorException("Two E in one number. At: " + i);
                    gotE = true;
                    gotSign = false;
                } else
                    break;
            }

            return new Reader.ReadResult<>(RealNumber.parse(expr.substring(0, j)), j);
        }

        @Override
        public void preevaluate(ExpressionElement before, ExpressionElement now, ExpressionElement next, Reader.ListNode node, Evaluator namespace) {
            if(now.getElementType() != ElementType.EXPRESSION_ENUM ||
                    !((ExpressionEnum) now).canBeCastedToExpression()) return;
            
            boolean usesExprEnum = false;
            if(before != null && before.getElementType() == ElementType.OPERATOR && ((Operator) before).getUses() == Operator.Uses.RIGHT_NUMBER_ENUM)
                usesExprEnum = true;
            if(next != null && next.getElementType() == ElementType.OPERATOR && ((Operator) next).getUses() == Operator.Uses.LEFT_NUMBER_ENUM) {
                usesExprEnum = true;
            }
            
            if(!usesExprEnum) {
                node.data = ((ExpressionEnum) now).castToExpression();
            }
        }
    }
}
