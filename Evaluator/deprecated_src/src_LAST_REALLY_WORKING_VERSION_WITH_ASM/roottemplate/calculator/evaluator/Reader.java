package roottemplate.calculator.evaluator;

public abstract class Reader implements ExpressionElement {
    /**
     * @param str the string
     * @param readUntil: 0 - use as readBrackets; 1 - use just to calculate bracketsOpen var, then return is <code>null</code>
     * @param l listener
     * @return read string
     * @throws EvaluatorException
     */
    public static ReadResult<IndexedString> readBrackets(IndexedString str, int readUntil, ReadBracketsListener l) throws EvaluatorException {
        int i = readUntil == 0 ? 1 : 0;
        int bracketsOpen = readUntil == 0 ? 1 : 0;
        boolean broken = false;
        for(;i < str.length() && (readUntil != 0 || bracketsOpen > 0);i++) {
            char at = str.charAt(i);
            if(at == '(' || at == '{' || at == '[')
                bracketsOpen++;
            else if(at == ')' || at == '}' || at == ']')
                bracketsOpen--;
            if(l != null && l.onChar(at, bracketsOpen, i)) {
                broken = true;
                break;
            }
        }
        if(!broken && bracketsOpen != 0) throw new EvaluatorException("Found " + bracketsOpen + " unclosed bracket(s)");
        
        if(readUntil == 0) {
            int endIndex = !broken && readUntil == 0 ? i - 1 : i;
            return new ReadResult<>(str.substring(1, endIndex), i);
        } else
            return null;
    }
    public static interface ReadBracketsListener {
        boolean onChar(char c, int bracketsOpen, int i);
    }
    
    
    public static boolean isNumerable(ExpressionElement obj, PriorityManager.PriorityStorage currentPriority, boolean before) {
        ElementType type = obj.getElementType();
        if(type == ElementType.NUMBER || type == ElementType.EXPRESSION) return true;
        if(type == ElementType.OPERATOR) {
            Operator op = (Operator) obj;
            boolean pr = op.getPriority().willBeExecutedBefore(currentPriority, before);
            boolean usingThis = before ? op.getUses().doesUseRight() : op.getUses().doesUseLeft();
            if(pr && !usingThis)
                return true;
        }
        return false;
    }
    
    public static boolean isExprEnumable(ExpressionElement obj, PriorityManager.PriorityStorage currentPriority, boolean before) {
        return obj.getElementType() == ElementType.EXPRESSION_ENUM || isNumerable(obj, currentPriority, before);
    }
    
    public static Number[] exprEnumableToNumberArray(ExpressionElement obj) throws EvaluatorException {
        switch (obj.getElementType()) {
            case EXPRESSION_ENUM:
                return ((ExpressionEnum) obj).eval();
            case NUMBER:
                return new Number[] {((Number) obj)};
            default:
                return null;
        }
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
    public static class ListStruct {
        public ListNode startNode = null;
        public ListNode endNode = null;
        
        public ListNode preudoAdd(ListNode beforeNode, ExpressionElement elem) {
            ListNode curNode = new ListNode(beforeNode, null, elem);
            if(beforeNode == null) {
                startNode = curNode;
            } else {
                beforeNode.next = curNode;
            }
            return curNode;
        }
    }
    
    public static class ReadResult<T> {
        public final T n;
        public final int charsUsed;
        public ReadResult(T n, int charsUsed) {
            this.n = n;
            this.charsUsed = charsUsed;
        }
    }
    
    public static class IndexedString {
        public final String str;
        public int index;
        
        public IndexedString(String str) {
            this(str, 0);
        }
        public IndexedString(String str, int index) {
            this.str = str;
            this.index = index;
        }
        public IndexedString(IndexedString str, int slice) {
            this.str = str.str;
            this.index = str.index + slice;
        }

        public boolean isEmpty() {
            return str.length() <= index;
        }

        public char charAt(int i) {
            return str.charAt(index + i);
        }
        public char charAt() {
            return str.charAt(index);
        }

        public boolean startsWith(String name) {
            return str.startsWith(name, index);
        }

        public int length() {
            return str.length() - index;
        }
        
        public IndexedString substring(int startIndex, int endIndex) {
            return new IndexedString(str.substring(index + startIndex, index + endIndex));
        }

        @Override
        public String toString() {
            return str.substring(index);
        }
    }
    
    
    
    public abstract Reader.ReadResult<? extends ExpressionElement> read(IndexedString expr, Evaluator namespace, int i) throws EvaluatorException;

    @Override
    public final ElementType getElementType() {
        return ElementType.READER;
    }
}
