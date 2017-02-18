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

import java.util.ArrayDeque;
import java.util.Deque;
import roottemplate.calculator.evaluator.Brackets;
import roottemplate.calculator.evaluator.EVMCompiler;
import roottemplate.calculator.evaluator.EvaluatorException;
import roottemplate.calculator.evaluator.ExpressionElement;
import roottemplate.calculator.evaluator.BracketsEnum;
import roottemplate.calculator.evaluator.Operator;
import roottemplate.calculator.evaluator.util.ListStruct;

public abstract class StackEVMCompiler implements EVMCompiler {

    protected ExpressionElement compileExprElems(ListStruct list, Object kit) throws EvaluatorException {
        if(list.startNode == null)
            throw new EvaluatorException("Empty expression", 0);
        while(list.startNode == list.endNode && list.startNode.data.getElementType() == ExpressionElement.ElementType.BRACKETS) {
            list = ((Brackets) list.startNode.data).getElements();
            if(list.startNode == null)
                throw new EvaluatorException("Empty brackets", -1);
        }
        
        Deque<ListStruct.ListNode> opStack = new ArrayDeque<>();
        ListStruct.ListNode cur = list.startNode;
        while(cur != null) {
            ExpressionElement elem = cur.data;
            if(elem.getElementType() == ExpressionElement.ElementType.OPERATOR) {
                Operator op = (Operator) elem;
                while(opStack.size() > 0 && ((Operator) opStack.getLast().data).getPriority().wouldBeExecutedBeforeThen(op.getPriority(), true)) {
                    compileOperatorNode(opStack.pollLast(), list, kit);
                }
                opStack.add(cur);
            }
            cur = cur.next;
        }
        
        while(opStack.size() > 0) {
            compileOperatorNode(opStack.pollLast(), list, kit);
        }
        
        if(list.startNode != list.endNode)
            throw new EvaluatorException("All operators evaluated but some elements (like Numbers) were not used", -1);
        return list.startNode.data;
    }
    
    private void compileOperatorNode(ListStruct.ListNode opNode, ListStruct list, Object kit) throws EvaluatorException {
        Operator op = (Operator) opNode.data;
        Operator.Uses u = op.getUses();
        
        ExpressionElement before = null, next = null;
        if(u.doesUseLeft()) before = checkAndRemoveSideForUses(opNode.prev, list, op, "left");
        if(u.doesUseRight()) next = checkAndRemoveSideForUses(opNode.next, list, op, "right");

        ExpressionElement[] ns;
        if(u.doesUseExprEnum()) {
            ExpressionElement ns0 = u.doesUseLeft() ? before : next;
            if(ns0.getElementType() == ExpressionElement.ElementType.BRACKETS_ENUM) {
                BracketsEnum enum_ = (BracketsEnum) ns0;
                ns = enum_.getEnum();
            } else {
                if(ns0.getElementType() == ExpressionElement.ElementType.BRACKETS &&
                        ((Brackets) ns0).isEmpty())
                    ns = new ExpressionElement[] {}; // Empty brackets = no arguments
                else
                    ns = new ExpressionElement[]{ns0};
            }
        } else {
            // Here: before/next cannot be BracketsEnum
            ns = new ExpressionElement[u.countUsesSides()];
            if(before != null) ns[0] = before; // setting to first
            if(next != null) ns[ns.length - 1] = next; // setting to last
        }

        for(int i = 0; i < ns.length; i++) {
            if(ns[i].getElementType() == ExpressionElement.ElementType.BRACKETS) {
                Brackets brks = (Brackets) ns[i];
                if(brks.isEmpty())
                    throw new EvaluatorException("Empty brackets", brks.exprIndex);
                ns[i] = compileExprElems(brks.getElements(), kit);
            }
        }
        
        opNode.data = compileOperator(op, ns, kit);
    }
    
    protected abstract ExpressionElement compileOperator(Operator op, ExpressionElement[] ns, Object kit) throws EvaluatorException;
            
            
    private ExpressionElement checkAndRemoveSideForUses(ListStruct.ListNode sideNode, ListStruct list, Operator op, String sideName)
            throws EvaluatorException {
        if(sideNode == null)
            throw new EvaluatorException(op.toString() + " uses " + sideName + " object but there is no such", -1);
        ExpressionElement sideElem = sideNode.data;
        
        boolean successCast = op.getUses().doesUseExprEnum() || sideElem.getElementType() != ExpressionElement.ElementType.BRACKETS_ENUM;
        if(!successCast)
            throw new EvaluatorException(op.toString() + " uses " + sideName + " " + op.getUses() + " but there is " + sideElem.getElementType(), -1);
        
        // removing side
        if(sideNode.prev != null) sideNode.prev.next = sideNode.next; else list.startNode = sideNode.next;
        if(sideNode.next != null) sideNode.next.prev = sideNode.prev; else list.endNode = sideNode.prev;
        
        return sideElem;
    }
    
}
