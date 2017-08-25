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

import roottemplate.calculator.evaluator.util.IndexedString;
import roottemplate.calculator.evaluator.util.Util;

public class Operator implements Named {
    protected final PriorityManager.PriorityStorage priority;
    protected final String realName;
    protected final String name;
    protected final Uses uses;
    protected final boolean isCommutative;
    
    public Operator(PriorityManager.PriorityStorage prStorage, String name) {
        this(prStorage, name, name, Uses.TWO_NUMBERS);
    }
    public Operator(PriorityManager.PriorityStorage prStorage, String name, boolean isCommutative) {
        this(prStorage, name, name, Uses.TWO_NUMBERS, isCommutative);
    }
    public Operator(PriorityManager.PriorityStorage prStorage, String name, Uses uses) {
        this(prStorage, name, name, uses);
    }
    public Operator(PriorityManager.PriorityStorage prStorage, String realName, String name) {
        this(prStorage, realName, name, Uses.TWO_NUMBERS);
    }
    public Operator(PriorityManager.PriorityStorage prStorage, String realName, String name, Uses uses) {
        this(prStorage, realName, name, uses, true);
    }
    public Operator(PriorityManager.PriorityStorage prStorage, String realName, String name, Uses uses, boolean isCommutative) {
        priority = prStorage;
        this.realName = realName;
        this.name = name;
        this.uses = uses;
        this.isCommutative = isCommutative;
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public final ElementType getElementType() {
        return ElementType.OPERATOR;
    }
    public Uses getUses() {
        return uses;
    }
    public PriorityManager.PriorityStorage getPriority() {
        return priority;
    }
    
    /**
     * Checks if this operator is commutative. It is guaranteed that this method
     * will not be called if <code>uses</code> is not <code>TWO_NUMBERS</code>.
     * @return true if this operator is commutative
     */
    public boolean isCommutative() {
        return isCommutative;
    }
    
    public Number eval(Number... numbers) throws EvaluatorException {
        return numbers[0].applyOperation(realName, uses != Uses.TWO_NUMBERS ? null : numbers[1]);
    }

    /**
     * Checks if this operator can be placed in this place
     * @param before The element before
     * @param expr the string expression after the operator
     * @param exactlyThis true if name and uses matches with the string expression,
     * false if name matches but uses haven't been checked yet
     * @return {#code Operator} to place in the {#code Expression} or null if this cannot be this operator.
     */
    public Operator checkUses(Object before, IndexedString expr, boolean exactlyThis) {
        return this;
    }
    
    
    @Override
    public String toString() {
        return "Operator {name: " + name + (!name.equals(realName) ? ", realName: " + realName : "") + ", uses: " + getUses() + "}";
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Util.hashCode(this.realName);
        hash = 97 * hash + Util.hashCode(this.uses);
        hash = 97 * hash + (this.isCommutative ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) return false;
        final Operator other = (Operator) obj;
        return Util.equals(this.realName, other.realName) &&
               this.uses == other.uses &&
               this.isCommutative == other.isCommutative;
    }
    
    
    public enum Uses {
        ONE_LEFT_NUMBER(true, false, true),
        ONE_RIGHT_NUMBER(false, true, true),
        TWO_NUMBERS(true, true, true),
        LEFT_NUMBER_ENUM(true, false, false),
        RIGHT_NUMBER_ENUM(false, true, false);
        
        
        private final boolean left;
        private final boolean right;
        private final boolean number;
        Uses(boolean left, boolean right, boolean number) {
            this.left = left;
            this.right = right;
            this.number = number;
        }
        
        public boolean doesUseLeft() {
            return left;
        }
        public boolean doesUseRight() {
            return right;
        }
        public boolean doesUseNumber() {
            return number;
        }
        public boolean doesUseExprEnum() {
            return !number;
        }
        public int countUsesSides() {
            int result = 0;
            if(left) result++;
            if(right) result++;
            return result;
        }
        public ElementType getUsingElemType() {
            return number ? ElementType.NUMBER : ElementType.BRACKETS_ENUM;
        }
    }
}
