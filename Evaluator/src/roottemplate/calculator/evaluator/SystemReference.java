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

public class SystemReference implements ExpressionElement {
    public final int reference;
    public final int instructionIndex;

    public SystemReference(int ref, int instrIndex) {
        reference = ref;
        instructionIndex = instrIndex;
    }

    @Override
    public final ExpressionElement.ElementType getElementType() {
        return ExpressionElement.ElementType.SYSTEM_REFERENCE;
    }

    @Override
    public String toString() {
        return "Reference {" + reference + "}";
    }
}
