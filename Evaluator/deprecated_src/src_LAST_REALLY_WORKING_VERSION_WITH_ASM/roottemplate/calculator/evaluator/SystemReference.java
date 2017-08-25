package roottemplate.calculator.evaluator;

public class SystemReference implements ExpressionElement {
    public final int reference;
    public final int instructionIndex;

    public SystemReference(int ref, int instrIndex) {
        reference = ref;
        instructionIndex = instrIndex;
    }

    @Override
    public final ExpressionElement.ElementType getElementType() {
        return ExpressionElement.ElementType.SYSTEM_REFERENCE;
    }

    @Override
    public String toString() {
        return "Reference {" + reference + "}";
    }
}
