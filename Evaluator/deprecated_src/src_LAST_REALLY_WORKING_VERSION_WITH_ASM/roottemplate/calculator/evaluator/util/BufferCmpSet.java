package roottemplate.calculator.evaluator.util;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import roottemplate.calculator.evaluator.Expression;
import roottemplate.calculator.evaluator.Number;

public class BufferCmpSet { // BufferCompileSet
    private final HashMap<Expression, int[]> exprsMap = new HashMap<>();
    private final HashMap<Number, Integer> numbersMap = new HashMap<>();
    private int nextExprIndex = 0;
    private int nextBufferIndex = 0;
    
    public int add(Expression expr) {
        int[] l = exprsMap.get(expr);
        if(l != null) return l[0];
        
        int result = nextBufferIndex;
        int bufferSize = expr.getBufferSize();
        exprsMap.put(expr, new int[] {nextExprIndex++, result, result + bufferSize - 1});
        nextBufferIndex += bufferSize;
        return result;
    }
    public int add(Number n) {
        Integer l = numbersMap.get(n);
        if(l != null) return l;
        
        numbersMap.put(n, nextBufferIndex);
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
    
    public int getRef(Number n) {
        return numbersMap.get(n);
    }
    
    public int getBufferSize() {
        return nextBufferIndex;
    }
    public int getNumbersCount() {
        return numbersMap.size();
    }
    
    public Map.Entry<Number[], int[]> getNumbersAndRefs() {
        Number[] ns = new Number[numbersMap.size()];
        int[] refs = new int[ns.length];
        int i = 0;
        for(Map.Entry<Number, Integer> entry : numbersMap.entrySet()) {
            ns[i] = entry.getKey();
            refs[i] = entry.getValue();
            i++;
        }
        return new AbstractMap.SimpleEntry(ns, refs);
    }
}
