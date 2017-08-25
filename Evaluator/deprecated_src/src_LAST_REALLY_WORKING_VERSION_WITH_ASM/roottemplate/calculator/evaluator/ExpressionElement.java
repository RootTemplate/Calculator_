package roottemplate.calculator.evaluator;

public interface ExpressionElement {
    ElementType getElementType();
    
    enum ElementType {
        EXPRESSION, NUMBER, OPERATOR, EXPRESSION_ENUM, READER, SYSTEM_REFERENCE;

        @Override
        public String toString() {
            switch(this) {
                case EXPRESSION: return "Expression";
                case NUMBER: return "Number";
                case OPERATOR: return "Operator";
                case EXPRESSION_ENUM: return "ExpressionEnum";
                case READER: return "Reader";
                case SYSTEM_REFERENCE: return "SystemReference";
                default: throw new IllegalStateException("Unknown state for ElementType");
            }
        }
    }
}
