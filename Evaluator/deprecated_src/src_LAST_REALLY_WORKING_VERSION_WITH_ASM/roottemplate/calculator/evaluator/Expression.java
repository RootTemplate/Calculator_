package roottemplate.calculator.evaluator;

import roottemplate.calculator.evaluator.Reader.ReadResult;
import roottemplate.calculator.evaluator.Operator.Uses;
import roottemplate.calculator.evaluator.Reader.IndexedString;
import roottemplate.calculator.evaluator.Reader.ListNode;
import roottemplate.calculator.evaluator.Reader.ListStruct;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import roottemplate.calculator.evaluator.Number.NumberManager;
import roottemplate.calculator.evaluator.util.CompileUtil;
import roottemplate.calculator.evaluator.util.BufferCmpSet;
import static org.objectweb.asm.Opcodes.*;
import roottemplate.calculator.evaluator.impls.ConstantCompiledExpression;
import roottemplate.calculator.evaluator.util.DynamicClassLoader;

// WarningL Expression is not an Operator. Impossible to understand if Expression contains modifiable numbers
public class Expression implements ExpressionElement {
    private static final DynamicClassLoader CLASS_LOADER = new DynamicClassLoader();
    
    public static Expression createFromNumber(Number n) {
        try {
            return new Expression(null, n, 0);
        } catch (EvaluatorException ex) {
            throw new Error(); // Will never be thrown
        }
    }
    public static Expression parse(String expr, Evaluator namespace) throws EvaluatorException {
        return parse(new IndexedString(expr), namespace, 0);
    }
    public static Expression parse(IndexedString expr, Evaluator namespace, int i) throws EvaluatorException {
        ListStruct list = new ListStruct();
        
        ExpressionElement before = null;
        ListNode beforeNode = null;
        int totalRead;
        while(!expr.isEmpty()) {
            totalRead = 0;
            ReadNextResult<?> next = readNext(before, expr, namespace, true, i);
            
            while(next != null) {
                totalRead += next.charsUsed;
                if(next.n == null) break;
                
                beforeNode = list.preudoAdd(beforeNode, next.n);
                next = next.next;
            }
            
            i += totalRead;
            expr.index += totalRead;
            before = beforeNode == null ? null : beforeNode.data;
        }
        list.endNode = beforeNode;
        
        Collection<Modificator> preevals = namespace.getModificators();
        for(Modificator m : preevals) {
            if((m.getModifies() & Modificator.MODIFIES_PREEVALUATOR) != 0) {
                for(ListNode node = list.startNode; node != null; node = node.next) {
                    m.preevaluate(node.prev == null ? null : node.prev.data, node.data, node.next == null ? null : node.next.data, node, namespace);
                }
            }
        }
        
        return createFromListStruct(list, namespace);
    }
    private static ReadNextResult<?> readNext(ExpressionElement before, IndexedString expr, Evaluator namespace,
            boolean throws_, int i) throws EvaluatorException {
        
        int startIndex = expr.index;
        ReadResult<? extends ExpressionElement> result = null;
        ReadNextResult<?> next = null;
        
        int iOffset = Reader.cutSpaces(expr);
        i += iOffset;
        
        if(expr.isEmpty()) {
            expr.index = startIndex;
            return new ReadNextResult<>(null, iOffset, null);
        }
        char at = expr.charAt();
        
        int nowIndex = expr.index;
        String name;
        for(Nameable obj : namespace.getAllStartingWith(at)) {
            expr.index = nowIndex;
            ReadNextResult<?> _next = null;
            name = obj.getName();
            if(expr.startsWith(name)) {
                if(obj.getElementType() == ElementType.OPERATOR) {
                    Operator op = (Operator) obj;
                    Uses u = op.getUses();
                    expr.index += name.length(); // Now expr is without name
                    if(!op.checkUses(before, expr, false)) continue;
                    
                    if(u.doesUseLeft()) {
                        if(before == null) continue;
                        boolean verified = false;
                        if(u == Uses.LEFT_NUMBER_ENUM) {
                            if(before.getElementType() == ElementType.EXPRESSION_ENUM)
                                verified = true;
                        }
                        if(!verified || u.doesUseNumber()) {
                            if(!Reader.isNumerable(before, op.getPriority(), true))
                                continue;
                        }
                    }
                    if(u.doesUseRight()) {
                        _next = readNext(
                                op, expr, namespace, false,
                                i + name.length());
                        if(_next.n == null) continue;
                        boolean verified = false;
                        if(u == Uses.RIGHT_NUMBER_ENUM) {
                            if(_next.n.getElementType() == ElementType.EXPRESSION_ENUM)
                                verified = true;
                        }
                        if(!verified || u.doesUseNumber()) {
                            if(!Reader.isNumerable(_next.n, op.getPriority(), false)) continue;
                        }
                    }

                    if(!op.checkUses(before, expr, true)) continue;
                }
                
                next = _next;
                if(obj.getElementType() == ElementType.READER) {
                    result = ((Reader) obj).read(expr, namespace, i);
                } else
                    result = new ReadResult<>(obj, name.length());
                break;
            }
        }
        expr.index = nowIndex;

        if(result == null)
            for(Modificator m : namespace.getModificators()) {
                if((m.getModifies() & Modificator.MODIFIES_READER) != 0) {
                    result = m.read(expr, namespace, i);
                    if(result != null) break;
                }
            }

        expr.index = startIndex;
        if(result == null)
            if(throws_)
                throw new EvaluatorException("Unknown char found at " + (i + 1) + ": " + at);
            else
                return new ReadNextResult<>(null, iOffset, null);

        return new ReadNextResult<>(result.n, result.charsUsed + iOffset, next);
    }
    private static class ReadNextResult<T extends ExpressionElement> extends ReadResult<T> {
        public final ReadNextResult<?> next;
        public ReadNextResult(T n, int charsUsed, ReadNextResult<?> next) {
            super(n, charsUsed);
            this.next = next;
        }
    }
    public static Expression createFromListStruct(ListStruct list, Evaluator namespace) throws EvaluatorException {
        if(list.startNode == null || list.endNode == null) throw new EvaluatorException("Empty string given");
        // TODO: WARNING: all ExpressionElements MUST have NumberManager with THE SAME ABSTRACTION LEVEL
        
        ListNode curNode;
        int maxOperatorPriority = -1;
        int operatorsCount = 0;
        for(curNode = list.startNode; curNode != null; curNode = curNode.next) {
            ElementType type = curNode.data.getElementType();
            if(type == ElementType.OPERATOR) {
                operatorsCount++;
                int pr = ((Operator) curNode.data).getPriority().getPriority();
                if(pr > maxOperatorPriority)
                    maxOperatorPriority = pr;
            }
        }
        
        int bufferIndex = -1;
        int instructionIndex = 0;
        Instruction[] instructions = new Instruction[operatorsCount];
        for(ListIterator<PriorityManager.PriorityStorage> prIt = namespace.priorityManagerOperators.listIterator(maxOperatorPriority + 1);
                prIt.hasPrevious(); ) {
            PriorityManager.PriorityStorage st = prIt.previous();
            boolean leftDir = st.leftDirection;
            for(curNode = (leftDir ? list.startNode : list.endNode); curNode != null; curNode = (leftDir ? curNode.next : curNode.prev)) {
                ExpressionElement before = null, next = null, now = curNode.data;
                if(now.getElementType() == ElementType.OPERATOR) {
                    Operator op = (Operator) now;
                    
                    if(op.getPriority().getPriority() == st.getPriority()) {
                        Uses u = op.getUses();
                        
                        if(u.doesUseLeft()) before = checkAndRemoveSideForUses(curNode.prev, list, op, "left");
                        if(u.doesUseRight()) next = checkAndRemoveSideForUses(curNode.next, list, op, "right");
                        
                        ExpressionElement[] ns = new ExpressionElement[u.countUsesSides()];
                        if(u.doesUseExprEnum()) {
                            ns[0] = (u.doesUseLeft() ? before : next);
                        } else {
                            if(u.doesUseLeft()) ns[0] = before; // setting to first
                            if(u.doesUseRight()) ns[ns.length - 1] = next; // setting to last
                        }
                        
                        int curBufferIndex = -1, refCount = 0, lastRefIndex = -1;
                        boolean hasModifiableNumbers = false, hasResult = true;
                        for(int i = 0; i < ns.length; i++) {
                            ExpressionElement elem = ns[i];
                            switch(elem.getElementType()) {
                                case EXPRESSION_ENUM: 
                                    hasResult = ((ExpressionEnum) elem).containsModifiableNumbers();
                                    break;
                                case EXPRESSION:
                                    hasResult = ((Expression) elem).containsModifiableNumbers();
                                    if(!hasResult)
                                        ns[i] = ((Expression) elem).eval();
                                    break;
                                case NUMBER: hasResult = ((Number) elem).isModifiable(); break;
                                case SYSTEM_REFERENCE:
                                    curBufferIndex = Math.max(curBufferIndex, ((SystemReference) elem).reference);
                                    // in fact, it doesn't matter max or min or something else
                                    hasResult = false;
                                    lastRefIndex = i;
                                    refCount++;
                                    break;
                            }
                            if(hasResult) hasModifiableNumbers = true;
                        }
                        boolean hasModifiable = hasModifiableNumbers || refCount > 0;
                        
                        hasResult = false;
                        if(op.getUses() == Uses.TWO_NUMBERS && op.isCommutative() && refCount == 1 &&
                                    !hasModifiableNumbers) {
                            SystemReference ref = (SystemReference) (ns[0].getElementType() ==
                                        ElementType.SYSTEM_REFERENCE ? ns[0] : ns[1]);
                            Instruction lastInstr = instructions[ref.instructionIndex];
                            int n1Index;
                            if(lastInstr.op.equals(op) && (n1Index = lastInstr.findUnmodifNumberIndex()) != -1) {
                                lastInstr.elems[n1Index] = op.eval((Number) lastInstr.elems[n1Index],
                                        (Number) ns[1 - lastRefIndex]); // lastRefIndex is 0 or 1; so calculated index in 1 or 0 respectively
                                // last reference was removed by default, return it
                                curNode.data = ref;
                                hasResult = true;
                            }
                        }
                        
                        if(!hasResult) {
                            if(curBufferIndex == -1 && hasModifiable) curBufferIndex = ++bufferIndex; // no references, request new index
                            Instruction ins = new Instruction(op, curBufferIndex, ns);
                            if(hasModifiable) {
                                instructions[instructionIndex++] = ins;
                                curNode.data = new SystemReference(curBufferIndex, instructionIndex - 1); // using curNode to store result
                            } else {
                                curNode.data = ins.do_(null);
                            }
                        }
                    }
                }
            }
        }
        
        if(list.startNode != list.endNode)
            throw new EvaluatorException("All operators evaluated but some objects were not used");
        ExpressionElement result = list.startNode.data;
        
        Instruction[] realInstructions = new Instruction[instructionIndex];
        System.arraycopy(instructions, 0, realInstructions, 0, instructionIndex);
        
        if(realInstructions.length == 0 && result.getElementType() == ElementType.EXPRESSION)
            return (Expression) result;
        
        return new Expression(realInstructions, result, bufferIndex + 1);
    }
    private static ExpressionElement checkAndRemoveSideForUses(ListNode sideNode, ListStruct list, Operator op, String sideName)
            throws EvaluatorException {
        if(sideNode == null)
            throw new EvaluatorException(op.toString() + " uses " + sideName + " object but there is no such");
        ExpressionElement sideElem = sideNode.data;
        
        boolean successCast = false;
        ElementType beforeType = sideElem.getElementType(), uType = op.getUses().getUsingElemType();
        if(uType == ElementType.NUMBER || uType == ElementType.EXPRESSION_ENUM) {
            successCast = (beforeType == ElementType.NUMBER || beforeType == ElementType.EXPRESSION ||
                    beforeType == ElementType.SYSTEM_REFERENCE);
        }
        if(!successCast)
            successCast = (beforeType == uType);
        if(!successCast)
            throw new EvaluatorException(op.toString() + " uses " + sideName + " " + uType + " but there is " + beforeType);
        
        // removing side
        if(sideNode.prev != null) sideNode.prev.next = sideNode.next; else list.startNode = sideNode.next;
        if(sideNode.next != null) sideNode.next.prev = sideNode.prev; else list.endNode = sideNode.prev;
        
        return sideElem;
    }
    public static Expression createFromList(List<ExpressionElement> list, Evaluator namespace) throws EvaluatorException {
        ListStruct list_ = new ListStruct();
        ListNode beforeNode = null;
        for(ExpressionElement elem : list) {
            beforeNode = list_.preudoAdd(beforeNode, elem);
        }
        list_.endNode = beforeNode;
        return createFromListStruct(list_, namespace);
    }
    
    
    
    private final Instruction[] instructions;
    private final ExpressionElement result;
    private final Number[] buffer;
    private final NumberManager numberManager;
    private final Number anyModifNumber;
    private CompiledExpression compiledExpr = null;
    
    final Number _getAnyModifNumber() {
        if(result.getElementType() == ElementType.NUMBER && ((Number) result).isModifiable())
            return (Number) result;
        return anyModifNumber;
    }
    final NumberManager _getNumberManager() {
        if(result.getElementType() == ElementType.NUMBER && !((Number) result).isModifiable())
            return ((Number) result).getNumberManager();
        return numberManager;
    }
    
    private Expression(Instruction[] instrs, ExpressionElement result, int bufferLength) throws EvaluatorException {
        ElementType resultType = result.getElementType();
        if(resultType != ElementType.SYSTEM_REFERENCE && resultType != ElementType.NUMBER && resultType != ElementType.EXPRESSION)
            throw new EvaluatorException("Result is not a number");
        if((instrs == null || instrs.length == 0) && result.getElementType() != ElementType.NUMBER)
            throw new EvaluatorException("Buffer is empty but result is not a number");
        if((instrs != null && instrs.length > 0) && result.getElementType() != ElementType.SYSTEM_REFERENCE)
            throw new EvaluatorException("Buffer is not empty but result is not a reference");
        
        this.instructions = instrs;
        this.result = result;
        this.buffer = instrs == null ? null : new Number[bufferLength];
        
        NumberManager nm = null;
        Number anyModifNumber = null;
        String exceptionMessage = "Numbers have different abstraction levels";
        ExpressionElement[] oneArray = new Expression[1];
        NumberManager nm_;
        for(Instruction instr : instrs)
            for(ExpressionElement elem : instr.elems)
                switch(elem.getElementType()) {
                    case NUMBER:
                        Number n = (Number) elem;
                        if(n.isModifiable()) anyModifNumber = n;
                        else if(nm == null) nm = n.getNumberManager();
                        else if(nm != n.getNumberManager())
                            throw new EvaluatorException(exceptionMessage);
                        break;
                    case EXPRESSION:
                        Expression ex = (Expression) elem;
                        if(ex._getAnyModifNumber() != null) anyModifNumber = ex._getAnyModifNumber();
                        nm_ = ex._getNumberManager();
                        if(nm_ != null)
                            if(nm == null) nm = nm_;
                            else if(nm_ != nm)
                                throw new EvaluatorException(exceptionMessage);
                        break;
                    case EXPRESSION_ENUM:
                        ExpressionEnum enum_ = (ExpressionEnum) elem;
                        Number amn = enum_._getAnyModifNumber();
                        nm_ = enum_._getNumberManager();
                        if(amn != null) anyModifNumber = amn;
                        if(nm_ != null)
                            if(nm != null && nm_ != nm)
                                throw new EvaluatorException(exceptionMessage);
                            else if(nm == null) nm = nm_;
                        break;
                }
        if(_getAnyModifNumber() != null)
            anyModifNumber = _getAnyModifNumber();
        
        this.numberManager = nm;
        this.anyModifNumber = anyModifNumber;
    }
    
    public synchronized Number eval() throws EvaluatorException {
        if(instructions != null) {
            for(Instruction i : instructions)
                i.do_(buffer);

            if(result.getElementType() == ElementType.SYSTEM_REFERENCE)
                return buffer[((SystemReference) result).reference];
        }
        return result.getElementType() == ElementType.EXPRESSION ?
                ((Expression) result).eval() : ((Number) result).isModifiable() ?
                ((Number) result).copy() : (Number) result;
    }
    
    public CompiledExpression compile() throws CompileEvaluatorException {
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
        
        Class<?> c = CLASS_LOADER.defineClass(className, cw.toByteArray());
        Map.Entry<Number[], int[]> nArrs = bufferRefs.getNumbersAndRefs();
        //System.out.println(Arrays.toString(nArrs.getKey()));
        //System.out.println(Arrays.toString(nArrs.getValue()));
        
        try {
            compiledExpr = (CompiledExpression) c.getConstructor(Number[].class, int[].class)
                    .newInstance((Object) nArrs.getKey(), (Object) nArrs.getValue());
            return compiledExpr;
        } catch (Exception ex) {
            throw new CompileEvaluatorException(ex);
        }
    }
    public int compileOperators(ClassVisitor cw, String className, BufferCmpSet bufferRefs) throws CompileEvaluatorException {
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
    }
    
    
    @Override
    public final ElementType getElementType() {
        return ElementType.EXPRESSION;
    }
    
    public boolean containsModifiableNumbers() {
        if(instructions == null || instructions.length == 0) {
            // Now we need to check the result Nameable
            // Now result can be only NUMBER
            return ((Number) result).isModifiable();
        }
        return true;
    }
    public ExpressionElement compress() {
        if(instructions == null || instructions.length == 0) {
            return result;
        }
        return this;
    }
    public int getBufferSize() {
        return (buffer == null || buffer.length == 0) ? 1 : buffer.length;
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
    
    
    
    private static class Instruction {
        private final Operator op;
        private final ExpressionElement[] elems;
        private final int resultIndex;
        private final Number[] cacheArray;
        
        public Instruction(Operator op, int resultIndex, ExpressionElement... args) {
            this.op = op;
            this.resultIndex = resultIndex;
            this.elems = args;
            
            int cacheLength = 0;
            for(ExpressionElement elem : args) {
                if(elem.getElementType() == ElementType.EXPRESSION_ENUM)
                    cacheLength += ((ExpressionEnum) elem).size();
                else
                    cacheLength++;
            }
            cacheArray = new Number[cacheLength];
        }
        
        public Number do_(Number[] buffer) throws EvaluatorException {
            int i = 0;
            for(ExpressionElement e : elems)
                switch(e.getElementType()) {
                    case EXPRESSION_ENUM: 
                        for(Number ex : ((ExpressionEnum) e).eval())
                            cacheArray[i++] = ex;
                        break;
                    case EXPRESSION: cacheArray[i++] = ((Expression) e).eval(); break;
                    case SYSTEM_REFERENCE: cacheArray[i++] = buffer[((SystemReference) e).reference]; break;
                    case NUMBER: cacheArray[i++] = (Number) e; break;
                }
            
            Number result = op.eval(cacheArray);
            if(buffer != null) buffer[resultIndex] = result;
            return result;
        }

        public int findUnmodifNumberIndex() {
            int index = 0;
            for(ExpressionElement elem : elems) {
                if(elem.getElementType() == ElementType.NUMBER && !((Number) elem).isModifiable())
                    return index;
                index++;
            }
            return -1;
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder("<");
            result.append(op.toString()).append("; ");
            
            boolean first = true;
            for(ExpressionElement arg : elems) {
                if(first)
                    first = false;
                else
                    result.append(", ");
                result.append(arg.toString());
            }
            
            result.append("; ").append(resultIndex).append("^").append(">");
            return result.toString();
        }
    }
    
    
    
    static class MultiplyOperatorsPreevaluator extends Modificator {
        private final Operator hMultiplyOp;
        
        public MultiplyOperatorsPreevaluator(Operator higherMultiplyOperator) {
            this.hMultiplyOp = higherMultiplyOperator;
        }
    
        @Override
        public int getModifies() {
            return MODIFIES_PREEVALUATOR;
        }

        @Override
        public void preevaluate(ExpressionElement before, ExpressionElement now, ExpressionElement next, ListNode node, Evaluator namespace) {
            if(before != null && Reader.isNumerable(before, hMultiplyOp.getPriority(), true) &&
                    Reader.isNumerable(now, hMultiplyOp.getPriority(), false) &&
                    !(before.getElementType() == ElementType.NUMBER && now.getElementType() == ElementType.NUMBER &&
                      !(before instanceof Nameable) && !(now instanceof Nameable))) {
                ListNode multiplyNode = new ListNode(node.prev, node, hMultiplyOp);
                node.prev.next = multiplyNode;
                node.prev = multiplyNode;
            }
        }
    }
}
