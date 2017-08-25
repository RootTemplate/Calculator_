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
package roottemplate.calculator.evaluator;

import java.util.ArrayList;
import roottemplate.calculator.evaluator.impls.StandardExpression;
import roottemplate.calculator.evaluator.util.BufferIndexManager;

// Warning: Expression is not an Operator. Impossible to understand if Expression contains modifiable numbers
public abstract class Expression {
    public static Expression parse(String expr, Evaluator namespace) throws EvaluatorException {
        return StandardExpression.createFromString(expr, namespace);
    }
    /*public static Expression createFromNumber(Number n) {
        try {
            return new StandardExpression(null, n, 0);
        } catch (EvaluatorException ex) {
            throw new Error(); // Will never be thrown
        }
    }
    protected static Expression createFromListStruct(ListStruct list) throws EvaluatorException {
        if(list.startNode == null || list.endNode == null) throw new EvaluatorException("Empty string given");
        // TODO: WARNING: all ExpressionElements MUST have NumberManager with THE SAME ABSTRACTION LEVEL
        
        /*ListNode curNode;
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
                                    hasResult = ((BracketsEnum) elem).containsModifiableNumbers();
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
        
        return new Expression(realInstructions, result, bufferIndex + 1);* /
        
        ArrayList<Instruction> insns = new ArrayList<>();
        BufferIndexManager indexes = new BufferIndexManager();
        ExpressionElement result = parseBrackets(list, insns, indexes);
        
        return new Expression(  insns.toArray(new Instruction[insns.size()]), result, indexes.getBufferSize()  );
    }
    protected static ExpressionElement parseBrackets(ListStruct list, List<Instruction> insns, BufferIndexManager indexes) throws EvaluatorException {
        while(list.startNode == list.endNode && list.startNode.data.getElementType() == ElementType.BRACKETS) {
            list = ((Brackets) list.startNode.data).getElements();
        }
        
        Deque<ListNode> opStack = new ArrayDeque<>();
        ListNode cur = list.startNode;
        while(cur != null) {
            ExpressionElement elem = cur.data;
            if(elem.getElementType() == ElementType.OPERATOR) {
                Operator op = (Operator) elem;
                while(opStack.size() > 0 && ((Operator) opStack.getLast().data).getPriority().wouldBeExecutedBeforeThen(op.getPriority(), true)) {
                    parseOperatorNode(opStack.pollLast(), list, insns, indexes);
                }
                opStack.add(cur);
            }
            cur = cur.next;
        }
        
        while(opStack.size() > 0) {
            parseOperatorNode(opStack.pollLast(), list, insns, indexes);
        }
        
        if(list.startNode != list.endNode)
            throw new EvaluatorException("All operators evaluated but some elements (like Numbers) were not used");
        return list.startNode.data;
    }
    private static void parseOperatorNode(ListNode opNode, ListStruct list, List<Instruction> insns, BufferIndexManager indexes)
            throws EvaluatorException {
        Operator op = (Operator) opNode.data;
        Uses u = op.getUses();
        
        ExpressionElement before = null, next = null;
        if(u.doesUseLeft()) before = checkAndRemoveSideForUses(opNode.prev, list, op, "left");
        if(u.doesUseRight()) next = checkAndRemoveSideForUses(opNode.next, list, op, "right");

        ExpressionElement[] ns;
        if(u.doesUseExprEnum()) {
            ExpressionElement ns0 = u.doesUseLeft() ? before : next;
            if(ns0.getElementType() == ElementType.EXPRESSION_ENUM) {
                BracketsEnum enum_ = (BracketsEnum) ns0;
                ns = new ExpressionElement[enum_.size()];
                System.arraycopy(enum_.enum_, 0, ns, 0, ns.length);
            } else
                ns = new ExpressionElement[] {ns0};
        } else {
            // Here: before/next cannot be BracketsEnum
            ns = new ExpressionElement[u.countUsesSides()];
            if(before != null) ns[0] = before; // setting to first
            if(next != null) ns[ns.length - 1] = next; // setting to last
        }

        for(int i = 0; i < ns.length; i++) {
            if(ns[i].getElementType() == ElementType.BRACKETS) {
                ns[i] = parseBrackets(  ((Brackets) ns[i]).getElements(), insns, indexes  );
            }
        }
        
        int refCount = 0, lastRefIndex = -1;
        boolean hasModifiableNumbers = false, hasResult = true;
        for(int i = 0; i < ns.length; i++) {
            ExpressionElement elem = ns[i];
            switch(elem.getElementType()) {
                case NUMBER: hasResult = ((Number) elem).isModifiable(); break;
                case SYSTEM_REFERENCE:
                    indexes.freeIndex(((SystemReference) elem).reference);
                    
                    hasResult = false;
                    lastRefIndex = i;
                    refCount++;
                    break;
                default:
                    throw new EvaluatorException(ns[i] + " found in the Expression, which is illegal");
            }
            if(hasResult) hasModifiableNumbers = true;
        }
        boolean hasModifiable = hasModifiableNumbers || refCount > 0;

        hasResult = false;
        if(op.getUses() == Uses.TWO_NUMBERS && op.isCommutative() && refCount == 1 && !hasModifiableNumbers) {
            SystemReference ref = (SystemReference) ns[lastRefIndex];
            Instruction lastInstr = insns.get(ref.instructionIndex);
            int n1Index;
            if(lastInstr.op.equals(op) && (n1Index = lastInstr.findUnmodifNumberIndex()) != -1) {
                lastInstr.elems[n1Index] = op.eval((Number) lastInstr.elems[n1Index],
                        (Number) ns[1 - lastRefIndex]); // lastRefIndex is either 0 or 1; so calculated index in 1 or 0 respectively
                // last reference was removed by default, return it
                opNode.data = ref;
                hasResult = true;
            }
        }

        if(!hasResult) {
            if(hasModifiable) {
                int bIndex = indexes.allocIndex();
                Instruction ins = new Instruction(op, bIndex, ns);
                insns.add(ins);
                opNode.data = new SystemReference(bIndex, insns.size() - 1); // using curNode to store result
            } else {
                opNode.data = new Instruction(op, -1, ns).do_(null);
            }
        }
    }
    private static ExpressionElement checkAndRemoveSideForUses(ListNode sideNode, ListStruct list, Operator op, String sideName)
            throws EvaluatorException {
        if(sideNode == null)
            throw new EvaluatorException(op.toString() + " uses " + sideName + " object but there is no such");
        ExpressionElement sideElem = sideNode.data;
        
        boolean successCast = op.getUses().doesUseExprEnum() || sideElem.getElementType() != ElementType.EXPRESSION_ENUM;
        if(!successCast)
            throw new EvaluatorException(op.toString() + " uses " + sideName + " " + op.getUses() + " but there is " + sideElem.getElementType());
        
        // removing side
        if(sideNode.prev != null) sideNode.prev.next = sideNode.next; else list.startNode = sideNode.next;
        if(sideNode.next != null) sideNode.next.prev = sideNode.prev; else list.endNode = sideNode.prev;
        
        return sideElem;
    }
    public static Expression createFromList(List<ExpressionElement> list) throws EvaluatorException {
        ListStruct list_ = new ListStruct();
        ListNode beforeNode = null;
        for(ExpressionElement elem : list) {
            beforeNode = list_.preudoAdd(beforeNode, elem);
        }
        list_.endNode = beforeNode;
        return createFromListStruct(list_);
    }*/
    
    /**
     * Evaluates expression.
     * <b>WARNING</b>: if any modifiable number which is used to evaluate this expression has different <code>NumberManager</code> from
     * <code>NumberManager</code> returned from <code>getNumberManager()</code>, then method's behaviour is unexpected.
     * @return result
     * @throws EvaluatorException if exception occurs
     */
    public abstract Number eval() throws EvaluatorException;

    /**
     * Returns <code>EVMCompiler</code> which have compiled this <code>Expression</code>.
     * @return EVMCompiler
     */
    public abstract EVMCompiler getEVMManager();
}
