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

import roottemplate.calculator.evaluator.ExpressionElement.ElementType;

public class Instruction {
    public final Operator op;
    public final ExpressionElement[] elems;
    public final int resultIndex;
    public transient final Number[] cacheArray;

    public Instruction(Operator op, int resultIndex, ExpressionElement... args) {
        this.op = op;
        this.resultIndex = resultIndex;
        this.elems = args;
        cacheArray = new Number[args.length];

        int i = 0;
        for(ExpressionElement elem : args) {
            setElement(i, elem);
            i++;
        }
    }

    public void setElement(int elemIndex, ExpressionElement value) {
        ElementType type = value.getElementType();
        if(type == ElementType.NUMBER) {
            cacheArray[elemIndex] = (Number) value;
        } else if(type != ElementType.SYSTEM_REFERENCE)
            throw new IllegalArgumentException("Instruction cannot contain " + type);
        elems[elemIndex] = value;
    }

    public Number execute(Number[] buffer) throws EvaluatorException {
        Number result = op.eval(genNsArray(buffer));
        if(buffer != null) buffer[resultIndex] = result;
        return result;
    }
    
    public Number[] genNsArray(Number[] buffer) {
        int i = 0;
        for(ExpressionElement e : elems) {
            if(e.getElementType() == ElementType.SYSTEM_REFERENCE)
                cacheArray[i] = buffer[((SystemReference) e).reference];
            i++;
        }
        return cacheArray;
    }

    public int findUnmodifiableNumber() {
        int index = 0;
        for(ExpressionElement elem : elems) {
            if(elem.getElementType() == ExpressionElement.ElementType.NUMBER && !((Number) elem).isModifiable())
                return index;
            index++;
        }
        return -1;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("<");
        result.append(op.toString()).append("; ");

        boolean first = true;
        for(ExpressionElement arg : elems) {
            if(first)
                first = false;
            else
                result.append(", ");
            result.append(arg.toString());
        }

        result.append("; ").append(resultIndex).append("^").append(">");
        return result.toString();
    }
}
