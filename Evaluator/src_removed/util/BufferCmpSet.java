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

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import roottemplate.calculator.evaluator.Expression;
import roottemplate.calculator.evaluator.ExpressionElement;
import roottemplate.calculator.evaluator.ExpressionElement.ElementType;
import roottemplate.calculator.evaluator.Number;
import roottemplate.calculator.evaluator.SystemReference;

public class BufferCmpSet { // BufferCompileSet
    /*private final HashMap<Expression, int[]> exprsMap = new HashMap<>();
    private final HashMap<Number, Integer> numbersMap = new HashMap<>();
    private int nextExprId = 0;
    private int nextBufferIndex = 0;

    public int add(Expression expr) {
        int[] l = exprsMap.get(expr);
        if(l != null) return l[0];

        ExpressionElement resultElem = expr.getResultElem();
        if(resultElem.getElementType() == ElementType.NUMBER) return -1;

        int bufferOffset = nextBufferIndex;
        int bufferSize = expr.getBufferSize();

        int resultIndex = ((SystemReference) resultElem).reference;
        exprsMap.put(expr, new int[] {nextExprId++, bufferOffset, bufferOffset + resultIndex});
        nextBufferIndex += bufferSize;
        return bufferOffset;
    }
    public int add(Number n) {
        Integer l = numbersMap.get(n);
        if(l != null) return l;

        numbersMap.put(n, nextBufferIndex);
        return nextBufferIndex++;
    }

    @Deprecated
    public int reserveNextIndex() {
        return nextBufferIndex++;
    }

    public int getId(Expression expr) {
        return getExprMapEntry(expr, 0);
    }
    public int getBufferOffset(Expression expr) {
        return getExprMapEntry(expr, 1);
    }
    public int getResultReference(Expression expr) {
        return getExprMapEntry(expr, 2);
    }
    private int getExprMapEntry(Expression expr, int index) {
        int[] e = exprsMap.get(expr);
        if(e == null) return -1;
        return e[index];
    }

    public int getReferenceOf(Number n) {
        return numbersMap.get(n);
    }

    public int getBufferSize() {
        return nextBufferIndex;
    }
    public int getNumbersCount() {
        return numbersMap.size();
    }

    @Deprecated
    public Map.Entry<Number[], int[]> getNumbersAndRefs() {
        Number[] ns = new Number[numbersMap.size()];
        int[] refs = new int[ns.length];
        int i = 0;
        for(Map.Entry<Number, Integer> entry : numbersMap.entrySet()) {
            ns[i] = entry.getKey();
            refs[i] = entry.getValue();
            i++;
        }
        return new AbstractMap.SimpleEntry<>(ns, refs);
    }
    public Map<Number, Integer> getNumbersMap() {
        return Collections.unmodifiableMap(numbersMap);
    }*/
}
