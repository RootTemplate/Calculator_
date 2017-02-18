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

import java.util.Collection;
import java.util.Iterator;

import roottemplate.calculator.evaluator.util.IndexedString;
import roottemplate.calculator.evaluator.util.ListStruct;

public abstract class Reader implements ExpressionElement {
    public static boolean isNumerable(ExpressionElement obj, PriorityManager.PriorityStorage currentPriority, boolean before) {
        ElementType type = obj.getElementType();
        if(type == ElementType.NUMBER || type == ElementType.BRACKETS) return true;
        if(type == ElementType.OPERATOR) {
            Operator op = (Operator) obj;
            boolean pr = op.getPriority().wouldBeExecutedBeforeThen(currentPriority, before);
            boolean usingThis = before ? op.getUses().doesUseRight() : op.getUses().doesUseLeft();
            if(pr && !usingThis)
                return true;
        }
        return false;
    }
    
    
    public static int cutSpaces(IndexedString str) {
        int startIndex = str.index;
        while(!str.isEmpty() && Character.isWhitespace(str.charAt())) str.index++;
        return str.index - startIndex;
    }
    public static boolean containsSpaces(String str) {
        for(char c : str.toCharArray())
            if(Character.isWhitespace(c))
                return true;
        return false;
    }
    
    public static class ReadResult<T> {
        public final T n;
        public final int charsUsed;
        public ReadResult(T n, int charsUsed) {
            this.n = n;
            this.charsUsed = charsUsed;
        }
    }
    
    public static ListStruct readExpressionElements(IndexedString str, Evaluator namespace, int i) throws EvaluatorException {
        ListStruct list = new ListStruct();
        
        ExpressionElement before = null;
        ListStruct.ListNode beforeNode = null;
        int totalRead;
        while(!str.isEmpty()) {
            totalRead = 0;
            ReadNextResult<?> next = readNext(before, str, namespace, true, i);
            
            while(next != null) {
                totalRead += next.charsUsed;
                if(next.n == null) break;
                
                beforeNode = list.pseudoAdd(beforeNode, next.n);
                next = next.next;
            }
            
            i += totalRead;
            str.index += totalRead;
            before = beforeNode == null ? null : beforeNode.data;
        }
        list.endNode = beforeNode;
        
        Collection<Modifier> preevals = namespace.getModifiers();
        for(Modifier m : preevals) {
            if((m.getModifies() & Modifier.MODIFIES_PREEVALUATOR) != 0) {
                m.preevaluate(list, namespace);
            }
        }
        
        return list;
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
        for(Iterator<? extends Named> it = namespace.getAllStartingWith(at); it.hasNext();) {
            Named obj = it.next();
            expr.index = nowIndex;
            ReadNextResult<?> _next = null;
            name = obj.getName();
            if(expr.startsWith(name)) {
                if(obj.getElementType() == ElementType.OPERATOR) {
                    Operator op = (Operator) obj;
                    Operator.Uses u = op.getUses();
                    expr.index += name.length(); // Now expr is without name
                    if(!op.checkUses(before, expr, false)) continue;
                    
                    if(u.doesUseLeft()) {
                        if(before == null) continue;
                        boolean verified = false;
                        if(u == Operator.Uses.LEFT_NUMBER_ENUM) {
                            if(before.getElementType() == ElementType.BRACKETS_ENUM)
                                verified = true;
                        }
                        if(!verified || u.doesUseNumber()) {
                            if(before.getElementType() != ElementType.BRACKETS_ENUM && // Hack for "root(3, 8) * 2"
                                    !Reader.isNumerable(before, op.getPriority(), true))
                                continue;
                        }
                    }
                    if(u.doesUseRight()) {
                        _next = readNext(
                                op, expr, namespace, false,
                                i + name.length());
                        if(_next.n == null) continue;
                        boolean verified = false;
                        if(u == Operator.Uses.RIGHT_NUMBER_ENUM) {
                            if(_next.n.getElementType() == ElementType.BRACKETS_ENUM)
                                verified = true;
                        }
                        if(!verified || u.doesUseNumber()) {
                            if(_next.n.getElementType() != ElementType.BRACKETS_ENUM && // Hack. See above
                                    !Reader.isNumerable(_next.n, op.getPriority(), false))
                                continue;
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
            for(Modifier m : namespace.getModifiers()) {
                if((m.getModifies() & Modifier.MODIFIES_READER) != 0) {
                    result = m.read(expr, namespace, i);
                    if(result != null) break;
                }
            }

        expr.index = startIndex;
        if(result == null)
            if(throws_)
                throw new EvaluatorException("Unknown char found at " + (i + 1) + ": " + at, i);
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
    
    
    
    public abstract Reader.ReadResult<? extends ExpressionElement> read(IndexedString expr, Evaluator namespace, int i) throws EvaluatorException;

    @Override
    public final ElementType getElementType() {
        return ElementType.READER;
    }
}
