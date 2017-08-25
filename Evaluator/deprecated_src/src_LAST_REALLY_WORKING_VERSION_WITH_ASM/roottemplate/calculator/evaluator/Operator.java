package roottemplate.calculator.evaluator;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import roottemplate.calculator.evaluator.Number.NumberManager;
import roottemplate.calculator.evaluator.Reader.IndexedString;
import roottemplate.calculator.evaluator.util.BufferCmpSet;
import roottemplate.calculator.evaluator.util.Util;

public class Operator implements Nameable {
    protected final PriorityManager.PriorityStorage priority;
    protected final String realName;
    protected final String name;
    protected final Uses uses;
    protected final boolean isCommutative;
    
    public Operator(PriorityManager.PriorityStorage prStorage, String name) {
        this(prStorage, name, name, Uses.TWO_NUMBERS);
    }
    public Operator(PriorityManager.PriorityStorage prStorage, String name, boolean isCommutative) {
        this(prStorage, name, name, Uses.TWO_NUMBERS, isCommutative);
    }
    public Operator(PriorityManager.PriorityStorage prStorage, String name, Uses uses) {
        this(prStorage, name, name, uses);
    }
    public Operator(PriorityManager.PriorityStorage prStorage, String realName, String name) {
        this(prStorage, realName, name, Uses.TWO_NUMBERS);
    }
    public Operator(PriorityManager.PriorityStorage prStorage, String realName, String name, Uses uses) {
        this(prStorage, realName, name, uses, true);
    }
    public Operator(PriorityManager.PriorityStorage prStorage, String realName, String name, Uses uses, boolean isCommutative) {
        priority = prStorage;
        this.realName = realName;
        this.name = name;
        this.uses = uses;
        this.isCommutative = isCommutative;
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public final ElementType getElementType() {
        return ElementType.OPERATOR;
    }
    public Uses getUses() {
        return uses;
    }
    public PriorityManager.PriorityStorage getPriority() {
        return priority;
    }
    
    /**
     * Checks if this operator is commutative. It is guaranteed that this method
     * will not be called if <code>uses</code> is not <code>TWO_NUMBERS</code>.
     * @return true if this operator is commutative
     */
    public boolean isCommutative() {
        return isCommutative;
    }
    
    /**
     * <b>WARNING</b>: IF OVERRIDE THIS, ALWAYS OVERRIDE <code>compile</code> METHOD
     */
    public Number eval(Number... numbers) throws EvaluatorException {
        return numbers[0].applyOperation(realName, uses != Uses.TWO_NUMBERS ? null : numbers[1]);
    }
    /**
     * <b>WARNING</b>: IF OVERRIDE THIS, ALWAYS OVERRIDE <code>eval</code> METHOD
     */
    public void compile(ExpressionElement[] elems, int referenceOffset, int resultId, ClassVisitor cw, MethodVisitor mv, String className,
            BufferCmpSet bufferRefs, NumberManager nm) throws CompileEvaluatorException {
        nm.compileOperator(realName, elems, referenceOffset, resultId, cw, mv, className, bufferRefs);
    }
    
    /**
     * Checks if this operator can be placed in this place
     * @param before The element before
     * @param expr the string expression after the operator
     * @param exactlyThis true if name and uses matches with the string expression,
     * false if name matches but uses haven't been checked yet
     * @return true if this can be this operator
     * @deprecated You shouldn't use this
     */
    @Deprecated
    public boolean checkUses(Object before, IndexedString expr, boolean exactlyThis) {
        return true;
    }
    
    
    @Override
    public String toString() {
        return "Operator {name: " + name + (!name.equals(realName) ? ", realName: " + realName : "") + ", uses: " + getUses() + "}";
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Util.hashCode(this.priority.getPriority());
        hash = 97 * hash + Util.hashCode(this.realName);
        hash = 97 * hash + Util.hashCode(this.uses);
        hash = 97 * hash + (this.isCommutative ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Operator other = (Operator) obj;
        if (this.priority.getPriority() != other.priority.getPriority()) {
            return false;
        }
        if (!Util.equals(this.realName, other.realName)) {
            return false;
        }
        if (this.uses != other.uses) {
            return false;
        }
        return this.isCommutative == other.isCommutative;
    }
    
    
    public enum Uses {
        ONE_LEFT_NUMBER(true, false, true),
        ONE_RIGHT_NUMBER(false, true, true),
        TWO_NUMBERS(true, true, true),
        LEFT_NUMBER_ENUM(true, false, false),
        RIGHT_NUMBER_ENUM(false, true, false);
        
        
        private final boolean left;
        private final boolean right;
        private final boolean number;
        private Uses(boolean left, boolean right, boolean number) {
            this.left = left;
            this.right = right;
            this.number = number;
        }
        
        public boolean doesUseLeft() {
            return left;
        }
        public boolean doesUseRight() {
            return right;
        }
        public boolean doesUseNumber() {
            return number;
        }
        public boolean doesUseExprEnum() {
            return !number;
        }
        public int countUsesSides() {
            int result = 0;
            if(left) result++;
            if(right) result++;
            return result;
        }
        public ElementType getUsingElemType() {
            return number ? ElementType.NUMBER : ElementType.EXPRESSION_ENUM;
        }
    }
}
