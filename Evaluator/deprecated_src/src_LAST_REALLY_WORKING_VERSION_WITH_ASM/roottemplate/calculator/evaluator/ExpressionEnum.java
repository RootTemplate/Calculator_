package roottemplate.calculator.evaluator;

import roottemplate.calculator.evaluator.Reader.IndexedString;
import roottemplate.calculator.evaluator.Reader.ReadResult;
import java.util.LinkedList;
import roottemplate.calculator.evaluator.Number.NumberManager;

public class ExpressionEnum implements ExpressionElement {
    public static ExpressionEnum parse(final IndexedString expr, Evaluator namespace, int i) throws EvaluatorException {
        final LinkedList<ReadResult<IndexedString>> list = new LinkedList<>();
        
        Reader.ReadBracketsListener l = new Reader.ReadBracketsListener() {
            private int lastExprEnd = 0;
            
            @Override
            public boolean onChar(char at, int bracketsOpen, int i) {
                boolean comma = true;
                if(expr.length() - 1 != i) {
                    if(at != ',' || (at == ',' && bracketsOpen != 0))
                        return false;
                } else
                    comma = false;

                list.add(new ReadResult<>(expr.substring(lastExprEnd, i + (comma ? 0 : 1)), lastExprEnd));
                lastExprEnd = i + 1;
                return false;
            }
        };
        Reader.readBrackets(expr, 1, l);
        
        Expression[] result = new Expression[list.size()];
        int j = 0;
        for(ReadResult<IndexedString> str : list) {
            result[j++] = Expression.parse(str.n, namespace, i);
            i += str.charsUsed;
        }
        return new ExpressionEnum(result);
    }
    public static ExpressionEnum createFromNumber(Number n) {
        Expression[] enum_ = new Expression[1];
        enum_[0] = Expression.createFromNumber(n);
        return new ExpressionEnum(enum_);
    }
    
    
    final Expression[] enum_;
    
    public ExpressionEnum(Expression[] enum_) {
        this.enum_ = enum_;
    }
    
    public int size() {
        return enum_.length;
    }
    public Expression[] getEnum() {
        return enum_.clone();
    }
    
    public boolean canBeCastedToExpression() {
        return enum_.length == 1;
    }
    
    public Expression castToExpression() {
        if(!canBeCastedToExpression()) return null;
        return enum_[0];
    }
    
    public boolean containsModifiableNumbers() {
        for(Expression expr : enum_)
            if(expr.containsModifiableNumbers())
                return true;
        return false;
    }
    
    Number _getAnyModifNumber() {
        for(Expression expr : enum_)
            if(expr._getAnyModifNumber() != null) return expr._getAnyModifNumber();
        return null;
    }
    NumberManager _getNumberManager() {
        for(Expression expr : enum_)
            if(expr._getNumberManager() != null) return expr._getNumberManager();
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ExpressionEnum {");
        boolean first = true;
        for(Expression expr : enum_) {
            if(first)
                first = false;
            else
                sb.append(", ");
            sb.append(expr.toString());
        }
        return sb.append("}").toString();
    }

    public Number[] eval() throws EvaluatorException {
        Number[] result = new Number[enum_.length];
        int i = 0;
        for(Expression expr : enum_) {
            result[i++] = expr.eval();
        }
        return result;
    }
    
    @Override
    public final ElementType getElementType() {
        return ElementType.EXPRESSION_ENUM;
    }
}
