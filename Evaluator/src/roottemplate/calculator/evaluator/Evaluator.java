/*
 * Copyright 2016-2017 RootTemplate Group 1
 *
 * This file is part of Calculator_ Engine (Evaluator).
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

import java.util.*;

import roottemplate.calculator.evaluator.ExpressionElement.ElementType;
import roottemplate.calculator.evaluator.namespace.*;
import static roottemplate.calculator.evaluator.PriorityManager.PriorityStorage;

public class Evaluator {
    public static final char[] RESERVED_CHARS = "(){}[]#E.,;%".toCharArray();
    public static boolean isValidName(String name) {
        if(name.isEmpty()) return false;
        
        for(char c : RESERVED_CHARS)
            if(name.indexOf(c) != -1) return false;
        char start = name.charAt(0);
        
        return !(Character.isDigit(start) || start == '.') && !Reader.containsSpaces(name);
    }
    
    
    public Evaluator.Options options;
    public final Processor processor;
    private Evaluator parent;
    private PriorityManager priorityManager;
    private final ArrayList<Number.NumberManager> numberManagers;
    private final HashMap<Character, CharacterLine> namespace;
    private final ArrayList<Modifier> modifiers;
    private final HashSet<Object> standardList;
    
    public Evaluator() {
        this(null);
    }
    public Evaluator(Evaluator parent) {
        this.parent = parent;
        this.namespace = new HashMap<>();
        this.modifiers = new ArrayList<>();
        this.standardList = new HashSet<>();
        this.processor = new Processor(this);
        this.numberManagers = parent != null ? parent.numberManagers : new ArrayList<Number.NumberManager>(2);
        
        changeParent(parent);
        if(parent == null)
            setupStandardNamespace();
    }

    private void setupStandardNamespace() {
        PriorityManager prMn = this.priorityManager;
        Evaluator.Options options = this.options;

        numberManagers.add(RealNumber.NUMBER_MANAGER);
        numberManagers.add(ComplexNumber.NUMBER_MANAGER);

        PriorityStorage st1 = prMn.create("+-", true);
        PriorityStorage st2 = prMn.create("*/", true);
        PriorityStorage st3 = prMn.create("HIGH_MULTIPLY", true);
        PriorityStorage st4 = prMn.create(Function.PRIORITY_NAME_NO_BRACKETS, false);
        PriorityStorage st5 = prMn.create("!%", true);
        PriorityStorage st6 = prMn.create(Function.PRIORITY_NAME_BRACKETS, false);
        PriorityStorage st7 = prMn.create("%_HELPER", false);

        Operator multiplyOp;
        Operator higherMultiplyOp = new Operator(st3, "*", Operator.Uses.TWO_NUMBERS);

        add(new BracketsReader(), true);
        add(new Operator(st1, "+"), true);
        Named minusOp = new Operator(st1, "-"); // Must be added after negate
        add(multiplyOp = new Operator(st2, "*"), true);
        add(new Operator(st2, "/"), true);
        add(new Operator(st4, "^", false), true);
        add(new RootFunction(st4, "\u221A"), true); // Unicode root
        add(new NegateOperator(st4), true); // Negate
        add(new Operator(st5, "!", Operator.Uses.ONE_LEFT_NUMBER), true);
        add(new PercentOperator(st5, st7), true);
        add(minusOp, true);

        add(new TrigonometricFunction(prMn, "sin", options), true);
        add(new TrigonometricFunction(prMn, "cos", options), true);
        add(new TrigonometricFunction(prMn, "tan", options), true); // tan
        add(new TrigonometricFunction(prMn, "tan", "tg", options), true); // tg
        add(new CotangentFunctions.CtgFunction(prMn, "cot", options), true);
        add(new CotangentFunctions.CtgFunction(prMn, "ctg", options), true); // ctg
        add(new TrigonometricFunction(prMn, "asin", options), true);
        add(new TrigonometricFunction(prMn, "acos", options), true);
        add(new TrigonometricFunction(prMn, "atan", options), true); // atan
        add(new TrigonometricFunction(prMn, "atan", "atg", options), true); // atg
        add(new CotangentFunctions.ArcctgFunction(prMn, "acot", options), true);
        add(new CotangentFunctions.ArcctgFunction(prMn, "actg", options), true); // actg

        add(new NativeFunction(prMn, "sinh"));
        add(new NativeFunction(prMn, "cosh"));
        add(new NativeFunction(prMn, "tanh"));
        add(new NativeFunction(prMn, "asinh"));
        add(new NativeFunction(prMn, "acosh"));
        add(new NativeFunction(prMn, "atanh"));

        add(new NativeFunction(prMn, "toRadians", "torad"), true);
        add(new NativeFunction(prMn, "toDegrees", "todeg"), true);
        add(new NativeFunction(prMn, "log10", "lg"), true);
        add(new NativeFunction(prMn, "log", "ln"), true);
        add(new LogFunction(prMn), true); // log. Base-e if 1 arg. Args: "n" or "base, n"
        add(new NativeFunction(prMn, "abs"), true);
        add(new GCDFunction(prMn), true);
        add(new NativeFunction(prMn, "floor"), true);
        add(new NativeFunction(prMn, "sqrt"), true);
        add(new NativeFunction(prMn, "cbrt"), true);
        add(new RootFunction(prMn, "root"), true); // Args: "n" or "thRoot, n"

        add(new ComplexNumberFunctions.Arg(prMn, options)); // arg(), returns argument of a complex number
        add(new ComplexNumberFunctions.Conjugate(prMn)); // conjugate()

        add(new Constant("PI", Math.PI), true);
        add(new Constant("\u03C0", Math.PI), true);
        add(new Constant("e", Math.E), true);
        add(new Constant("Infinity", Double.POSITIVE_INFINITY), true);
        add(new Constant("\u221E", Double.POSITIVE_INFINITY), true);
        add(new Constant("NaN", Double.NaN), true);
        add(new Constant("i", new ComplexNumber(0, 1)), true);
        add(new Variable("x"), true);
        add(new Variable("y"), true);

        addModifier(new StandardPreevaluator(multiplyOp, higherMultiplyOp), true);
        addModifier(new Number.NumberReader(numberManagers), true);
    }

    public void changeParent(Evaluator newParent) {
        if(newParent != null) {
            options = newParent.options;
            priorityManager = newParent.priorityManager;
        } else {
            options = new Options();
            priorityManager = new PriorityManager();
        }
        parent = newParent;
    }
    public Evaluator getParent() {
        return parent;
    }

    public PriorityManager getPriorityManager() {
        return priorityManager;
    }

    public void setStandard(Named n, boolean newValue) {
        if(newValue)
            standardList.add(n);
        else
            standardList.remove(n);
    }
    
    private CharacterLine getLine(char c, boolean createNew) {
        CharacterLine result = namespace.get(c);
        if(result == null && createNew) {
            result = new CharacterLine();
            namespace.put(c, result);
        }
        return result;
    }

    public void add(Named n) {
        add(n, false);
    }
    public void add(Named n, boolean isStandard) {
        String name = n.getName();
        if(!isStandard && !isValidName(name))
            throw new IllegalArgumentException("Unacceptable name: '" + name + "'");
        
        getLine(n.getName().charAt(0), true).add(n);
        if(isStandard)
            standardList.add(n);
    }
    
    public void addModifier(Modifier m, boolean isStandard) {
        modifiers.add(m);
        if(isStandard)
            standardList.add(m);
    }

    public void addNumberManager(Number.NumberManager nm) {
        int absLevel = nm.getAbstractionLevel();
        ListIterator<Number.NumberManager> it = numberManagers.listIterator();
        while(it.hasNext())
            if(it.next().getAbstractionLevel() > absLevel) {
                it.previous();
                it.add(nm);
                return;
            }
        it.add(nm);
    }
    
    public Named get(String name) {
        return get(name, 0, null);
    }
    public Reader getReader(String name) {
        return (Reader) get(name, 1, null);
    }
    public Operator getOperator(String name, Operator.Uses uses) {
        return (Operator) get(name, 2, uses);
    }
    public Named getOther(String name) {
        return get(name, 3, null);
    }
    private Named get(String name, int what, Operator.Uses uses) {
        if(name.isEmpty()) return null;

        char c = name.charAt(0);
        Evaluator cur = this;
        while(cur != null) {
            CharacterLine cl = cur.getLine(c, false);
            if(cl != null) {
                Named result = null;
                switch (what) {
                    case 0: result = cl.get(name); break;
                    case 1: result = (Named) cl.getReader(name); break;
                    case 2: result = cl.getOperator(name, uses); break;
                    case 3: result = cl.getOther(name); break;
                }
                if(result != null)
                    return result;
            }
            cur = cur.parent;
        }
        return null;
    }
    public Modifier getModifier(String name) {
        Evaluator cur = this;
        while(cur != null) {
            for(Modifier m : cur.modifiers) {
                String frName = m.getFriendlyName();
                if (name == null && frName == null || name != null && name.equals(frName))
                    return m;
            }
            cur = cur.parent;
        }
        return null;
    }
    
    public Iterator<Named> getAllStartingWith(char c) {
        return new CharNamespaceIterator(c);
    }
    
    public Map<Character, Collection<Named>> getNamespace() {
        HashMap<Character, Collection<Named>> res = new HashMap<>();

        for(int type = 0; type < 3; type++) {
            Evaluator curNs = Evaluator.this;

            while (curNs != null) {
                for(Map.Entry<Character, CharacterLine> entry : curNs.namespace.entrySet()) {
                    CharacterLine line = entry.getValue();
                    Collection<Named> resLine = res.get(entry.getKey());
                    if(resLine == null) {
                        resLine = new ArrayList<>(line.list.size());
                        res.put(entry.getKey(), resLine);
                    }

                    int startIndex, endIndex;
                    switch (type) {
                        case 0:
                            startIndex = 0;
                            endIndex = line.operatorIndex;
                            break;
                        case 1:
                            startIndex = line.operatorIndex;
                            endIndex = line.otherIndex;
                            break;
                        case 2:
                            startIndex = line.otherIndex;
                            endIndex = line.list.size();
                            break;
                        default:
                            throw new Error(); // Will never be executed
                    }

                    if(startIndex != endIndex)
                        resLine.addAll(line.list.subList(startIndex, endIndex));
                }

                curNs = curNs.parent;
            }
        }
        return res;
    }
    public Collection<Modifier> getModifiers() {
        if(parent == null)
            return Collections.unmodifiableCollection(modifiers);
        
        ArrayList<Modifier> result = new ArrayList<>();
        Evaluator cur = this;
        while(cur != null) {
            result.addAll(cur.modifiers);
            cur = cur.parent;
        }
        return result;
    }

    public boolean remove(Named n, boolean removeIfStandard) {
        return remove(n, removeIfStandard, false);
    }
    public boolean remove(Named n, boolean removeIfStandard, boolean checkInParents) {
        char c = n.getName().charAt(0);
        Evaluator cur = this;
        while(cur != null) {
            if(!removeIfStandard && cur.standardList.contains(n)) return false;
            
            CharacterLine cl = cur.getLine(c, false);
            if(cl != null && cl.remove(n))
                return true;

            if(!checkInParents) return false;
            cur = cur.parent;
        }
        return false;
    }

    public boolean removeNumberManager(Number.NumberManager nm) {
        return numberManagers.remove(nm);
    }

    public void clearNamespace() {
        clearNamespace(false);
    }
    public void clearNamespace(boolean clearStandards) {
        Evaluator cur = this;
        while(cur != null) {
            for(CharacterLine cl : cur.namespace.values()) {
                for(ListIterator<Named> it = cl.list.listIterator(); it.hasNext();) {
                    if(clearStandards || !cur.standardList.contains(it.next()))
                        cl.remove__(it);
                }
            }
            cur = cur.parent;
        }
    }
    
    public Number process(String expr) throws EvaluatorException {
        return processor.process(expr);
    }
    
    
    private static class CharacterLine {
        ArrayList<Named> list;
        int operatorIndex = 0;
        int otherIndex = 0;
        
        public CharacterLine() {
            this(1);
        }
        public CharacterLine(int initialLength) {
            list = new ArrayList<>(initialLength);
        }
        
        public void add(Named n) {
            switch(n.getElementType()) {
                case READER:
                    add_(n, 0, operatorIndex);
                    operatorIndex++;
                    otherIndex++;
                    break;
                case OPERATOR:
                    add_(n, operatorIndex, otherIndex);
                    otherIndex++;
                    break;
                default:
                    add_(n, otherIndex, list.size());
                    break;
            }
        }
        private void add_(Named n, int start, int end) {
            int nameLength = n.getName().length();
            ListIterator<Named> it = list.listIterator(start);
            while(it.hasNext() && it.nextIndex() < end) {
                if(it.next().getName().length() <= nameLength) {
                    it.previous();
                    it.add(n);
                    return;
                }
            }
            it.add(n); // Add to the end
        }
        
        public Named get(String name) {
            return get__(name, 0, list.size());
        }
        public Reader getReader(String name) {
            return (Reader) get__(name, 0, operatorIndex);
        }
        public Named getOther(String name) {
            return get__(name, otherIndex, list.size());
        }
        private Named get__(String name, int start, int end) {
            for(ListIterator<Named> it = list.listIterator(start); it.hasNext() && it.nextIndex() < end;) {
                Named elem = it.next();
                if(elem.getName().equals(name))
                    return elem;
            }
            return null;
        }
        
        public Operator getOperator(String name, Operator.Uses uses) {
            for(ListIterator<Named> it = list.listIterator(operatorIndex); it.hasNext() && it.nextIndex() < otherIndex;) {
                Operator op = (Operator) it.next();
                if(op.getName().equals(name) && op.getUses() == uses)
                    return op;
            }
            return null;
        }
        
        public boolean remove(Named n) {
            ElementType type = n.getElementType();
            int start = (type == ElementType.READER) ? 0 : (type == ElementType.OPERATOR) ? operatorIndex : otherIndex;
            for(ListIterator<Named> it = list.listIterator(start); it.hasNext();) {
                if(it.next() == n) {
                    remove__(it);
                    return true;
                }
            }
            return false;
        }
        private void remove__(ListIterator<Named> it) {
            it.remove();
            if(it.nextIndex() < operatorIndex)
                operatorIndex--;
            if(it.nextIndex() < otherIndex)
                otherIndex--;
        }
    }

    private class CharNamespaceIterator implements Iterator<Named> {
        private final char c;
        private Evaluator curNs;
        private byte type;
        private CharacterLine curLine;
        private int nextIndex;

        private CharNamespaceIterator(char c) {
            this.c = c;
            type = 0;

            prepareNext();
        }

        @Override
        public boolean hasNext() {
            return curLine != null;
        }

        @Override
        public Named next() {
            Named res;
            if(nextIndex < getEndIndex(type)) {
                res = curLine.list.get(nextIndex);
                prepareNext();
            } else {
                prepareNext();
                res = curLine.list.get(nextIndex);
            }
            return res;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        private void prepareNext() {
            if(curLine != null && nextIndex + 1 < getEndIndex(type)) {
                nextIndex++;
                return;
            }

            while(type < 3) {
                if(curNs != null)
                    curNs = curNs.parent;
                else
                    curNs = Evaluator.this;

                while (curNs != null) {
                    CharacterLine cl = curNs.getLine(c, false);
                    if (cl != null) {
                        curLine = cl;
                        int startIndex = getEndIndex(type - 1);
                        if(startIndex != getEndIndex(type)) {
                            nextIndex = startIndex;
                            return;
                        }
                    }

                    curNs = curNs.parent;
                }

                type++;
            }

            curLine = null;
        }

        private int getEndIndex(int type) {
            switch(type) {
                case -1:
                    return 0;
                case 0:
                    return curLine.operatorIndex;
                case 1:
                    return curLine.otherIndex;
                case 2:
                case 3:
                    return curLine.list.size();
                default:
                    return -1;
            }
        }
    }
    
    
    public static class Options {
        
        /**
         * <ul>
         *  <li>1 is radians
         *  <li>2 is degrees
         * </ul>
         */
        public int ANGLE_MEASURING_UNITS = 1;
        
        public boolean ENABLE_HASH_COMMANDS = false;

        public boolean ALLOW_OVERRIDE_CONSTANTS = true;
    }
}
