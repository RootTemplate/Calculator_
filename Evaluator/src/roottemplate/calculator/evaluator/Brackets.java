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

import roottemplate.calculator.evaluator.util.ListStruct;
import roottemplate.calculator.evaluator.util.ListStruct.ListNode;

public class Brackets implements ExpressionElement {
    public final int exprIndex;
    private final ListStruct elements;

    public Brackets(ListStruct elements, int exprIndex) {
        this.elements = elements;
        this.exprIndex = exprIndex;
    }
    
    public ListStruct getElements() {
        return elements.copy();
    }

    public boolean isEmpty() {
        return elements.startNode == null;
    }

    @Override
    public final ElementType getElementType() {
        return ElementType.BRACKETS;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Brackets {");
        ListNode cur = elements.startNode;
        while(cur != null) {
            if(cur != elements.startNode)
                sb.append(", ");
            sb.append(cur.data.toString());
            cur = cur.next;
        }
        return sb.append("}").toString();
    }
    
}
