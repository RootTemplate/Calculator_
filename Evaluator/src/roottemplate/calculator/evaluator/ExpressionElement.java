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

public interface ExpressionElement {
    ElementType getElementType();
    
    enum ElementType {
        BRACKETS, NUMBER, OPERATOR, BRACKETS_ENUM, READER, SYSTEM_REFERENCE;

        @Override
        public String toString() {
            switch(this) {
                case BRACKETS: return "Brackets";
                case NUMBER: return "Number";
                case OPERATOR: return "Operator";
                case BRACKETS_ENUM: return "BracketsEnum";
                case READER: return "Reader";
                case SYSTEM_REFERENCE: return "SystemReference";
                default: throw new IllegalStateException("Unknown state for ElementType");
            }
        }
    }
}
