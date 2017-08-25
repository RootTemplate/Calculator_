package roottemplate.calculator.evaluator;

import roottemplate.calculator.evaluator.ExpressionElement.ElementType;
import roottemplate.calculator.evaluator.impls.NegateOperator;
import roottemplate.calculator.evaluator.impls.NativeFunction;
import roottemplate.calculator.evaluator.impls.TrigonometricFunction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import roottemplate.calculator.evaluator.Number.NumberManager;
import roottemplate.calculator.evaluator.impls.ExpressionAndItsEnumReader;
import roottemplate.calculator.evaluator.impls.RootFunction;

// Возможные оптимизации: предevaluation (3*4*x -> 12*x; 3*x*4 -> 12*x)
// todo: casting numbers
// todo: Operator.isCommutative is unused

public class Evaluator {
    public static final char[] RESERVED_CHARS = "(){}[]#E.,;".toCharArray();
    public static boolean isValidName(String name) {
        if(name.isEmpty()) return false;
        
        for(char c : RESERVED_CHARS)
            if(name.indexOf(c) != -1) return false;
        char start = name.charAt(0);
        
        return !(Character.isDigit(start) || start == '.') && !Reader.containsSpaces(name);
    }
    private static void checkName(String name) {
        if(!isValidName(name))
            throw new IllegalArgumentException("Illegal name \"" + name + "\"");
    }
    
    public static Evaluator createEvaluator(List<? extends Nameable> standardNamespace,
            List<String> standardDefinitions,
            List<? extends StandardListElementBox<Modificator>> standardModificators) throws EvaluatorException {
        return createEvaluator(null, standardNamespace, standardDefinitions, standardModificators);
    }
    public static Evaluator createEvaluator(Evaluator parent, List<? extends Nameable> standardNamespace,
            List<String> standardDefinitions,
            List<? extends StandardListElementBox<Modificator>> standardModificators) throws EvaluatorException {
        Evaluator result = new Evaluator(parent);
        if(standardNamespace != null) result.addAll(standardNamespace, true);
        if(standardDefinitions != null)
            for(String definition : standardDefinitions)
                result.processor.define(definition, true);
        if(standardModificators != null)
            for(ListIterator<? extends StandardListElementBox<Modificator>> it = standardModificators.listIterator(standardModificators.size());
                    it.hasPrevious(); ) {
                StandardListElementBox<Modificator> box = it.previous();
                result.modificators.add(box.after, box.friendlyName, box.r, true);
            }
        return result;
    }
    public static class StandardListElementBox<E> {
        public final E r;
        public final String friendlyName;
        public final String after;
        public StandardListElementBox(E r) { this(null, null, r); }
        public StandardListElementBox(String friendlyName, E r) { this(null, friendlyName, r); }
        public StandardListElementBox(String after, String friendlyName, E r) {
            this.r = r;
            this.friendlyName = friendlyName;
            this.after = after;
        }
    }
    
    
    
    private final CharacterLine EMPTY_CHAR_LINE = new CharacterLine();
    public  final Options options;
    public  final PriorityManager priorityManagerOperators;
    public  final Processor processor;
    private final Evaluator parent;
    private final HashMap<Character, CharacterLine> characterMap = new HashMap<>();
    private final HashSet<Object> standardsMap = new HashSet<>(); // Warning: parents have different standard sets
    private final StandardList<Modificator> modificators = new StandardList<>();
    
    public Evaluator() { this(null); }
    public Evaluator(Evaluator parent) {
        this.parent = parent;
        if(parent == null) {
            options = new Options();
            priorityManagerOperators = new PriorityManager();
            processor = new Processor(this);
            initStandardNamespace();
        } else {
            Evaluator cur = this;
            while(cur.parent != null) cur = cur.parent;
            options = cur.options;
            priorityManagerOperators = cur.priorityManagerOperators;
            processor = cur.processor;
        }
    }
    
    private void initStandardNamespace() {
        Operator higherMultiplyOp;
        ArrayList<Nameable> l = new ArrayList<>(35);
        l.add(new ExpressionAndItsEnumReader());
        l.add(new Operator(priorityManagerOperators.createPriority("+-", true), "+"));
        l.add(new Operator(priorityManagerOperators.createPriority("+-", true), "-"));
        l.add(new Operator(priorityManagerOperators.createPriority("*/", true), "*"));
        l.add(new Operator(priorityManagerOperators.createPriority("*/", true), "/"));
        higherMultiplyOp = new Operator(priorityManagerOperators.createPriority("HIGH_MULTIPLY", true), "*", Operator.Uses.TWO_NUMBERS);
        l.add(new Operator(priorityManagerOperators.createPriority("^", false), "^", false));
        
        l.add(new TrigonometricFunction(priorityManagerOperators, "sin", options));
        l.add(new TrigonometricFunction(priorityManagerOperators, "cos", options));
        l.add(new TrigonometricFunction(priorityManagerOperators, "tan", options)); // tan
        l.add(new TrigonometricFunction(priorityManagerOperators, "tan", "tg", options)); // tg
        l.add(new TrigonometricFunction(priorityManagerOperators, "asin", options));
        l.add(new TrigonometricFunction(priorityManagerOperators, "acos", options));
        l.add(new TrigonometricFunction(priorityManagerOperators, "atan", options)); // atan
        l.add(new TrigonometricFunction(priorityManagerOperators, "atan", "atg", options)); // atg
        
        l.add(new NativeFunction(priorityManagerOperators, "toRadians", "torad"));
        l.add(new NativeFunction(priorityManagerOperators, "toDegrees", "todeg"));
        l.add(new NativeFunction(priorityManagerOperators, "log10", "lg"));
        l.add(new NativeFunction(priorityManagerOperators, "log"));
        l.add(new NativeFunction(priorityManagerOperators, "abs"));
        l.add(new NativeFunction(priorityManagerOperators, "round"));
        l.add(new NativeFunction(priorityManagerOperators, "sqrt"));
        l.add(new NativeFunction(priorityManagerOperators, "cbrt"));
        l.add(new RootFunction(priorityManagerOperators, "root", false));
        
        l.add(new RootFunction(priorityManagerOperators, "\u221A", true)); // Unicode root
        l.add(new NegateOperator(priorityManagerOperators)); // Negate
        
        l.add(new Operator(priorityManagerOperators.createPriority("!", true), "!", Operator.Uses.ONE_LEFT_NUMBER));
        
        l.add(new Constant("PI", Math.PI));
        l.add(new Constant("\u03C0", Math.PI));
        l.add(new Constant("e", Math.E));
        l.add(new Constant("Infinity", Double.POSITIVE_INFINITY));
        l.add(new Constant("\u221E", Double.POSITIVE_INFINITY));
        l.add(new Constant("NaN", Double.NaN));
        l.add(new Constant("%", 0.01));
        l.add(new Variable("x"));
        l.add(new Variable("y"));
        
        addAll(l, true);
        
        modificators.add(null, "MULTIPLY_OPERATOR", new Expression.MultiplyOperatorsPreevaluator(higherMultiplyOp), true);
        modificators.add(null, "NUMBERS", new Number.NumberReader(), true);
    }
    private CharacterLine getOrCreateCharLine(char c) {
        CharacterLine result = characterMap.get(c);
        if(result == null) {
            result = new CharacterLine();
            characterMap.put(c, result);
        }
        return result;
    }
    private CharacterLine getCharLineNoCreation(char c) {
        CharacterLine result = characterMap.get(c);
        return result == null ? EMPTY_CHAR_LINE : result;
    }
    
    /**
     * Clears namespace and nameless readers (and parent's if got parent) from user's objects.
     * @return The number of removed objects
     */
    public synchronized int clear() {
        int result = 0;
        Evaluator cur = this;
        while(cur != null) {
            Iterator<CharacterLine> it;
            CharacterLine line;
            for(it = cur.characterMap.values().iterator(); it.hasNext();) {
                line = it.next();
                result += line.clearAndCount();
                if(line.isEmpty()) it.remove();
            }
            //result += cur.modificators.clear(); #onlystandardmodificators
            
            cur = cur.parent;
        }
        return result;
    }
    
    /**
     * Trims namespace. (Trims every ArrayList to their size)
     */
    public synchronized void trim() {
        Evaluator cur = this;
        while(cur != null) {
            Iterator<CharacterLine> it;
            for(it = cur.characterMap.values().iterator(); it.hasNext();) {
                if(it.next().isEmpty()) it.remove();
            }
            cur = cur.parent;
        }
    }
    
    /**
     * Checks if object is a standard object.
     * @param obj The object to check
     * @return true if object is standard, false otherwise
     */
    public synchronized boolean isStandard(Object obj) {
        Evaluator cur = this;
        while(cur != null) {
            if(cur.standardsMap.contains(obj)) return true;
            cur = cur.parent;
        }
        return false;
    }
    
    /**
     * Adds a nameable to the namespace. The nameable can be a reader 
     * @param n The nameable
     * @return true if nameable was successfully added to the namespace
     */
    public boolean add(Nameable n) { return add(n, false); }
    /**
     * Adds all nameables from given list to the namespace.
     * @param l The list with nameables
     */
    public void addAll(List<? extends Nameable> l) { addAll(l, false); }
    synchronized boolean add(Nameable n, boolean isStandard) {
        if(isStandard(n)) return false;
        
        String name = n.getName();
        if(!isStandard) checkName(name);
        getOrCreateCharLine(name.charAt(0)).add(n);
        if(isStandard) standardsMap.add(n);
        return true;
    }
    private synchronized void addAll(List<? extends Nameable> l, boolean isStandard) {
        ListIterator<? extends Nameable> it;
        for(it = l.listIterator(l.size()); it.hasPrevious();) // saving priority
            add(it.previous(), isStandard);
    }
    
    private synchronized Nameable get(String name, Operator.Uses uses, byte what) {
        if(name.isEmpty()) return null;
        Evaluator cur = this;
        CharacterLine line;
        Nameable result;
        char starting = name.charAt(0);
        while(cur != null) {
            line = cur.characterMap.get(starting);
            if(line != null) {
                result = what == 0 ? line.getAny(name) :
                         what == 1 ? line.getReader(name) :
                         what == 2 ? line.getOperator(name, uses) :
                         what == 3 ? line.getOther(name) : null;

                if(result != null) return result;
            }
            cur = cur.parent;
        }
        return null;
    }
    /**
     * Returns the first reader-nameable with given name from namespace
     * @param name The name
     * @return Found nameable of null if there is no such
     */
    public Reader getReader(String name) { return (Reader) get(name, null, (byte) 1); }
    /**
     * Returns the first operator with given name and uses from namespace
     * @param name The name
     * @param uses Uses
     * @return Found operator or null if there is no such
     */
    public Operator getOperator(String name, Operator.Uses uses) { return (Operator) get(name, uses, (byte) 2); }
    /**
     * Returns the first other-nameable with given name from namespace
     * @param name The name
     * @return Found nameable of null if there is no such
     */
    public Nameable getOther(String name) { return get(name, null, (byte) 3); }
    /**
     * Returns the first nameable with given name from namespace
     * @param name The name
     * @return Found nameable of null if there is no such
     */
    public Nameable getAny(String name) { return get(name, null, (byte) 0); }
    /**
     * Returns all nameables that start with given character
     * @param c The character
     * @return found nameables or empty array if there is no such
     */
    public synchronized Collection<Nameable> getAllStartingWith(char c) {
        if(parent == null) {
            return Collections.<Nameable>unmodifiableCollection(getCharLineNoCreation(c));
        }
        CharacterLine result = new CharacterLine();
        Evaluator cur = this;
        while(cur != null) {
            result.addAllLine(cur.getCharLineNoCreation(c), false);
            cur = cur.parent;
        }
        return result;
    }
    Operator getMultiplyOperator() {
        return getOperator("*", Operator.Uses.TWO_NUMBERS);
    }
    
    private synchronized Collection<Nameable> getNamespace(boolean usersOnly) {
        CharacterLine result = new CharacterLine();
        Evaluator cur = this;
        while(cur != null) {
            for(CharacterLine line : cur.characterMap.values()) {
                result.addAllLine(line, usersOnly);
            }
            cur = cur.parent;
        }
        return result;
    }
    /**
     * @return Full namespace (without nameless readers and preevaluators)
     */
    public Collection<Nameable> getFullNamespace() { return getNamespace(false); }
    /**
     * @return User's namespace (without nameless readers and preevaluators)
     */
    public Collection<Nameable> getUsersNamespace() { return getNamespace(true); }
    
    /**
     * @return The clone of modificators list
     */
    public synchronized Collection<Modificator> getModificators() {
        if(parent == null)
            return Collections.<Modificator>unmodifiableList(modificators.list);
        
        LinkedList<Modificator> result = new LinkedList<>();
        Evaluator cur = this;
        while(cur != null) {
            result.addAll(cur.modificators.list);
            cur = cur.parent;
        }
        return result;
    }
    /**
     * Searches a nameless reader by a friendly name
     * @param friendlyName The friendly name
     * @return Found nameless reader or null, if nothing found
     */
    public synchronized Modificator getModificatorByFriendlyName(String friendlyName) {
        Evaluator cur = this;
        Modificator elem;
        StandardList<Modificator> list;
        while(cur != null) {
            list = cur.modificators;
            elem = list.getElementByName(friendlyName);
            if(elem != null) return elem;
            cur = cur.parent;
        }
        return null;
    }
    Modificator getNumberModificator() {
        return getModificatorByFriendlyName("NUMBERS");
    }
    
    private synchronized boolean remove(String name, Nameable remove, Operator.Uses uses, byte what) {
        if(name.isEmpty()) return false;
        char starting = name.charAt(0);
        
        try {
            CharacterLine line;
            Evaluator cur = this;
            Nameable removed;
            while(cur != null) {
                line = cur.characterMap.get(starting);
                if(line != null) {
                    if(what == 4) {
                        if(line.remove(remove)) return true;
                    } else {
                        removed = what == 0 ? line.removeAny(name) :
                                  what == 1 ? line.removeReader(name) :
                                  what == 2 ? line.removeOperator(name, uses) :
                                  what == 3 ? line.removeOther(name) : null;
                        if(removed != null) {
                            if(what == 2 || (what == 0 && removed.getElementType() == ElementType.OPERATOR))
                                priorityManagerOperators.removeIfNeeded(((Operator) removed).getPriority().getPriority());
                            return true;
                        }
                    }
                }
                cur = cur.parent;
            }
        } catch(IllegalStateException ex) { /* Return false */ }
        return false;
    }
    /**
     * Removes given nameable from the namespace
     * @param n The nameable to remove
     * @return true if nameable was removed successfully
     */
    public boolean remove(Nameable n) { return remove(n.getName(), n, null, (byte) 4); }
    /**
     * Removes the first nameable from the namespace with given name
     * @param name The name
     * @return true if nameable was removed successfully
     */
    public boolean removeAny(String name) { return remove(name, null, null, (byte) 0); }
    /**
     * Removes the first reader-nameable from the namespace with given name
     * @param name The name
     * @return true if nameable was removed successfully
     */
    public boolean removeReader(String name) { return remove(name, null, null, (byte) 1); }
    /**
     * Removes the first operator from the namespace with given name and uses
     * @param name The name
     * @param uses Uses
     * @return true if nameable was removed successfully
     */
    public boolean removeOperator(String name, Operator.Uses uses) { return remove(name, null, uses, (byte) 2); }
    /**
     * Removes the first other-nameable from the namespace with given name
     * @param name The name
     * @return true if nameable was removed successfully
     */
    public boolean removeOther(String name) { return remove(name, null, null, (byte) 3); }
    /* * #onlystandardmodificators
     * Removes given modificator from the list
     * @param m The modificator
     * @return true if modificator was removed successfully
     * /
    public synchronized boolean removeModificator(Modificator m) {
        Evaluator cur = this;
        while(cur != null) {
            if(cur.modificators.remove(m)) return true;
            cur = cur.parent;
        }
        return false;
    }
    /* *
     * Removes modificator with given friendly name from the list
     * @param friendlyName Friendly name
     * @return true if modificator was removed successfully
     * /
    public synchronized boolean removeModificatorByFriendlyName(String friendlyName) {
        Evaluator cur = this;
        while(cur != null) {
            if(cur.modificators.remove(friendlyName)) return true;
            cur = cur.parent;
        }
        return false;
    }//*/
    
    /**
     * Evaluates or defines expression. See eval(String) and define(String).
     * @param expr The expression string
     * @return Result number or null
     * @throws EvaluatorException if expression is bad-formed
     */
    public Number process(String expr) throws EvaluatorException {
        return processor.process(expr);
    }
    
    
    
    private class CharacterLine implements Collection<Nameable> {
        private final LinkedList<Nameable> readers = new LinkedList<>();
        private final LinkedList<Nameable> operators = new LinkedList<>();
        private final LinkedList<Nameable> others = new LinkedList<>();
        
        private LinkedList<Nameable> getListById(int id) {
            switch (id) {
                case 0: return readers;
                case 1: return operators;
                case 2: return others;
                default: return null;
            }
        }
        private void checkEditing(Nameable editing) {
            if(standardsMap.contains(editing)) throw new IllegalStateException();
        }
        
        @Override
        public void clear() { clearAndCount(); }
        public int clearAndCount() {
            int result = 0;
            ListIterator<Nameable> it;
            Nameable removing;
            for(int j = 0; j < 3; j++) {
                it = getListById(j).listIterator();
                for(; it.hasNext();)
                    if(!standardsMap.contains(removing = it.next())) {
                        if(j == 1) priorityManagerOperators.removeIfNeeded(((Operator) removing).getPriority().getPriority());
                        it.remove();
                        result++;
                    }
            }
            return result;
        }
        
        @Override
        public boolean add(Nameable n) {
            switch (n.getElementType()) {
                case OPERATOR:
                    addTo(n, operators);
                    break;
                case READER:
                    addTo(n, readers);
                    break;
                default:
                    addTo(n, others);
                    break;
            }
            return true;
        } 
        private void addTo(Nameable n, LinkedList<Nameable> to) {
            int nameLength = n.getName().length();
            ListIterator<Nameable> it = to.listIterator();
            while(it.hasNext()) {
                if(it.next().getName().length() <= nameLength) {
                    it.previous();
                    it.add(n);
                    return;
                }
            }
            it.add(n); // add to the end
        }
        
        private Nameable get(List<? extends Nameable> search, String name) {
            for(Nameable r : search)
                if(r.getName().equals(name)) return r;
            return null;
        }
        public Operator getOperator(String name, Operator.Uses uses) {
            Operator op;
            for(Nameable n : operators) {
                op = (Operator) n;
                if(op.getName().equals(name) && op.getUses() == uses) return op;
            }
            return null;
        }
        public Nameable getReader(String name) { return get(readers, name); }
        public Nameable getOther(String name) { return get(others, name); }
        public Nameable getAny(String name) {
            Nameable result;
            for(int j = 0; j < 3; j++)
                if((result = get(getListById(j), name)) != null) return result;
            return null;
        }

        @Override
        public int size() {
            return readers.size() + operators.size() + others.size();
        }

        @Override
        public boolean isEmpty() {
            return readers.isEmpty() && operators.isEmpty() && others.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            if(!(o instanceof Nameable)) return false;
            Nameable n = (Nameable) o;
            switch (n.getElementType()) {
                case OPERATOR: return operators.contains(n);
                case READER: return readers.contains(n);
                default: return others.contains(n);
            }
        }

        @Override
        public Iterator<Nameable> iterator() {
            return new Itr();
        }

        @Override
        public Object[] toArray() {
            Object[] result = new Object[size()];
            return this.<Object>toArray(result);
        }

        @Override
        public <T> T[] toArray(T[] a) {
            int i = 0;
            System.arraycopy(readers.toArray(), 0, a, i, readers.size());
            i = readers.size();
            System.arraycopy(operators.toArray(), 0, a, i, operators.size());
            i += operators.size();
            System.arraycopy(others.toArray(), 0, a, i, others.size());
            return a;
        }

        @Override
        public boolean remove(Object o) {
            if(!(o instanceof Nameable)) return false;
            Nameable n = (Nameable) o;
            checkEditing(n);
            switch (n.getElementType()) {
                case OPERATOR:
                    boolean result = operators.remove(n);
                    if(result) priorityManagerOperators.removeIfNeeded(((Operator) n).getPriority().getPriority());
                    return result;
                case READER:
                    return readers.remove(n);
                default:
                    return others.remove(n);
            }
        }
        private Nameable removeIn(List<? extends Nameable> in, String name) {
            ListIterator<? extends Nameable> it;
            Nameable n;
            for(it = in.listIterator(); it.hasNext();)
                if((n = it.next()).getName().equals(name)) {
                    checkEditing(n);
                    it.remove();
                    return n;
                }
            return null;
        }
        public Nameable removeReader(String name) { return removeIn(readers, name); }
        public Nameable removeOther(String name) { return removeIn(others, name); }
        public Nameable removeAny(String name) {
            Nameable result;
            List<? extends Nameable> list;
            for(int j = 0; j < 3; j++) {
                list = getListById(j);
                if((result = removeIn(list, name)) != null) return result;
            }
            return null;
        }
        public Nameable removeOperator(String name, Operator.Uses uses) {
            ListIterator<Nameable> it;
            Operator op;
            for(it = operators.listIterator(); it.hasNext();) {
                op = (Operator) it.next();
                if(op.getName().equals(name) && op.getUses() == uses) {
                    checkEditing(op);
                    it.remove();
                    return op;
                }
            }
            return null;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            for (Object obj : c) {
                if(!contains((Nameable) obj)) return false;
            }
            return true;
        }

        @Override
        public boolean addAll(Collection<? extends Nameable> c) {
            if(c instanceof CharacterLine) {
                addAllLine((CharacterLine) c, false);
                return true;
            }
            // saving priority
            ArrayList<Nameable> list = new ArrayList<>(c.size());
            int index = c.size() - 1;
            for(Nameable n : c)
                list.add(index--, n);
            for(Nameable n : list)
                add(n);
            return true;
        }
        public void addAllLine(CharacterLine line, boolean usersOnly) {
            Nameable n;
            int length;
            ListIterator<Nameable> from;
            ListIterator<Nameable> to;
            for(int j = 0; j < 3; j++) {
                int toCurLength = -1; // length of next element
                from = line.getListById(j).listIterator();
                to = getListById(j).listIterator();
                for(; from.hasNext(); ) {
                    boolean toCurLengthChanged = false;
                    n = from.next();
                    if(usersOnly && standardsMap.contains(n)) continue;
                    length = n.getName().length();
                    while(toCurLength == -1 || length <= toCurLength) {
                        toCurLength = to.hasNext() ? to.next().getName().length() : 0;
                        toCurLengthChanged = true;
                    }
                    if(toCurLengthChanged && toCurLength != 0)
                        to.previous();
                    to.add(n);
                }
            }
        }

        @Override
        public boolean removeAll(Collection<?> c) { throw new UnsupportedOperationException(); }

        @Override
        public boolean retainAll(Collection<?> c) { throw new UnsupportedOperationException(); }
        
        private class Itr implements Iterator<Nameable> {
            private Iterator<? extends Nameable> it = readers.iterator();
            private byte curList = 0;

            private void prepareIterator() {
                for(; curList < 3 - 1; curList++) {
                    if(it.hasNext()) return;
                    it = curList == 0 ? operators.iterator() : others.iterator();
                }
            }
            
            @Override
            public boolean hasNext() {
                prepareIterator();
                return it.hasNext();
            }

            @Override
            public Nameable next() {
                prepareIterator();
                return it.next();
            }

            @Override
            public void remove() {
                it.remove();
            }
        }
    }
    
    public class StandardList<E> {
        private final LinkedList<E> list = new LinkedList<>();
        private final LinkedList<String> names = new LinkedList<>();
        
        public int clear() {
            int result = 0;
            Iterator<E> it;
            Iterator<String> itNames;
            for(it = list.iterator(), itNames = names.iterator(); it.hasNext(); itNames.next())
                if(!standardsMap.contains(it.next())) {
                    itNames.remove();
                    it.remove();
                    result++;
                }
            return result;
        }
        
        private E getElementByName(String name) {
            int index = names.indexOf(name);
            if(index == -1) return null;
            return list.get(index);
        }
        
        private boolean remove(String friendlyName) {
            int index = names.indexOf(friendlyName);
            if(index == -1) return false;
            
            E r = list.get(index);
            if(standardsMap.contains(r)) return false;
            
            list.remove(index);
            names.remove(index);
            return true;
        }
        private boolean remove(E r) {
            if(standardsMap.contains(r)) return false;
            
            int index = list.indexOf(r);
            if(index == -1) return false;
            
            list.remove(index);
            names.remove(index);
            return true;
        }
        
        public boolean add(E r) { return add(null, null, r); }
        public boolean add(String friendlyName, E r) { return add(null, friendlyName, r); }
        public boolean add(String after, String friendlyName, E r) { return add(after, friendlyName, r, false); }
        boolean add(String after, String friendlyName, E r, boolean isStandard) {
            if(names.contains(friendlyName)) return false;
            
            int index = after == null ? 0 : names.indexOf(after);
            if(index == -1) return false;
            
            list.add(index, r);
            names.add(index, friendlyName);
            if(isStandard) standardsMap.add(r);
            return true;
        }
    }
    
    
    
    
    public static class Options {
        
        /**
         * <ul>
         *  <li>1 is radians
         *  <li>2 is degrees
         * </ul>
         */
        public int ANGLE_MEASURING_UNITS = 2;
        
        public boolean ENABLE_HASH_COMMANDS = true;
    }
}
