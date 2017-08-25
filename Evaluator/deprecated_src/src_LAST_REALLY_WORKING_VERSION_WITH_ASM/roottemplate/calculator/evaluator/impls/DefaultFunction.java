package roottemplate.calculator.evaluator.impls;

import java.util.Arrays;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import roottemplate.calculator.evaluator.CompileEvaluatorException;
import roottemplate.calculator.evaluator.Evaluator;
import roottemplate.calculator.evaluator.EvaluatorException;
import roottemplate.calculator.evaluator.Expression;
import roottemplate.calculator.evaluator.ExpressionElement;
import roottemplate.calculator.evaluator.Function;
import roottemplate.calculator.evaluator.Number;
import roottemplate.calculator.evaluator.PriorityManager;
import roottemplate.calculator.evaluator.SystemReference;
import roottemplate.calculator.evaluator.Variable;
import roottemplate.calculator.evaluator.util.BufferCmpSet;

public class DefaultFunction extends Function {
    private final Expression equalsTo;
    private final Variable[] vars;
        
    public DefaultFunction(Evaluator namespace, String name, String equalsTo, Variable... vars) throws EvaluatorException {
        super(namespace.priorityManagerOperators, name, vars.length);
        
        Evaluator localNamespace = new Evaluator(namespace);
        for(Variable var : vars)
            localNamespace.add(var);
        
        this.equalsTo = Expression.parse(equalsTo, localNamespace);
        this.vars = vars;
    }
    
    public DefaultFunction(PriorityManager prManager, String name, Expression equalsTo, Variable... vars) {
        super(prManager, name, vars.length);
        
        this.equalsTo = equalsTo;
        this.vars = vars;
    }

    @Override
    protected Number eval0(Number... numbers) throws EvaluatorException {
        int i = 0;
        for(Variable var : vars)
            var.changeValue(numbers[i++]);
        return equalsTo.eval();
    }

    @Override
    public void compile(ExpressionElement[] elems, int referenceOffset, int resultId, ClassVisitor cw, MethodVisitor mv, String className,
            BufferCmpSet bufferRefs, Number.NumberManager nm) throws CompileEvaluatorException {
        int i = 0;
        for(ExpressionElement elem : elems) {
            switch(elem.getElementType()) {
                case EXPRESSION:
                    elem = ((Expression) elem).compress();
                    if(elem.getElementType() == ElementType.EXPRESSION) {
                        Expression e = (Expression) elem;
                        Expression.compileInvokeExpression(mv, className,
                                e.compileOperators(cw, className, bufferRefs));
                        nm.compilePutIntoBuffer(mv, bufferRefs.getResultReference(e), bufferRefs.add(vars[i]));
                        break;
                    } // else = Number
                case NUMBER:
                    nm.compilePutIntoBuffer(mv, (Number) elem, bufferRefs.add(vars[i]), bufferRefs);
                    break;
                case SYSTEM_REFERENCE:
                    nm.compilePutIntoBuffer(mv, ((SystemReference) elem).reference + referenceOffset, bufferRefs.add(vars[i]));
                    break;
            }
            i++;
        }
        Expression.compileInvokeExpression(mv, className, equalsTo.compileOperators(cw, className, bufferRefs));
        nm.compilePutIntoBuffer(mv, bufferRefs.getResultReference(equalsTo), resultId);
    }

    @Override
    public String toString() {
        return super.toString() + "vars: " + Arrays.toString(vars) + ", " + equalsTo + "}";
    }
}
