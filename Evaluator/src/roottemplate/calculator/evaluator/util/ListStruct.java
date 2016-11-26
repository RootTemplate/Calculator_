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
package roottemplate.calculator.evaluator.util;

import roottemplate.calculator.evaluator.ExpressionElement;

public class ListStruct {
    public ListNode startNode = null;
    public ListNode endNode = null;

    public ListNode pseudoAdd(ListNode beforeNode, ExpressionElement elem) {
        ListNode curNode = new ListNode(beforeNode, null, elem);
        if(beforeNode == null) {
            startNode = curNode;
        } else {
            beforeNode.next = curNode;
        }
        return curNode;
    }
    
    public ListStruct copy() {
        ListStruct result = new ListStruct();
        ListNode before = null;
        
        ListNode cur = startNode;
        while(cur != null) {
            before = result.pseudoAdd(before, cur.data);
            cur = cur.next;
        }
        
        result.endNode = before;
        return result;
    }
    
    
    
    public static class ListNode {
        public ListNode prev;
        public ListNode next;
        public ExpressionElement data;
        
        public ListNode() {}
        public ListNode(ListNode prev, ListNode next, ExpressionElement data) {
            this.prev = prev;
            this.next = next;
            this.data = data;
        }
    }
}
