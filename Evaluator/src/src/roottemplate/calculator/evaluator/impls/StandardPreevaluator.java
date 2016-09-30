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

import roottemplate.calculator.evaluator.Evaluator;
import roottemplate.calculator.evaluator.ExpressionElement;
import roottemplate.calculator.evaluator.ExpressionElement.ElementType;
import roottemplate.calculator.evaluator.Modificator;
import static roottemplate.calculator.evaluator.Modificator.MODIFIES_PREEVALUATOR;
import roottemplate.calculator.evaluator.Operator;
import roottemplate.calculator.evaluator.Reader;
import roottemplate.calculator.evaluator.util.ListStruct;
import roottemplate.calculator.evaluator.util.ListStruct.ListNode;
import roottemplate.calculator.evaluator.Named;

public class StandardPreevaluator extends Modificator {
    
    private final Operator hMultiplyOp;

    public StandardPreevaluator(Operator higherMultiplyOperator) {
        this.hMultiplyOp = higherMultiplyOperator;
    }

    @Override
    public void preevaluate(ListStruct list, Evaluator namespace) {
        ListNode cur = list.startNode;
        while(cur != null) {
            if(cur.next != null) {
                ExpressionElement now = cur.data, next = cur.next.data;
                if(Reader.isNumerable(now, hMultiplyOp.getPriority(), true) && Reader.isNumerable(next, hMultiplyOp.getPriority(), false) &&
                        (now.getElementType() != ElementType.NUMBER || next.getElementType() != ElementType.NUMBER ||
                          (now instanceof Named) || (next instanceof Named))) {
                    
                    ListNode node = new ListNode(cur, cur.next, hMultiplyOp);
                    cur.next.prev = node;
                    cur.next = node;
                }
            }
            cur = cur.next;
        }
    }

    @Override
    public int getModifies() {
        return MODIFIES_PREEVALUATOR;
    }
}
