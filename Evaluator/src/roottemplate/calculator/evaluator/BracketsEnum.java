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

public class BracketsEnum implements ExpressionElement {
    final Brackets[] enum_;
    
    public BracketsEnum(Brackets[] enum_) {
        this.enum_ = enum_;
    }
    
    public int size() {
        return enum_.length;
    }
    public Brackets[] getEnum() {
        Brackets[] result = new Brackets[enum_.length];
        System.arraycopy(enum_, 0, result, 0, enum_.length);
        return result;
    }
    
    public boolean canBeCastedToBrackets() {
        return enum_.length == 1;
    }
    
    public Brackets castToBrackets() {
        if(!canBeCastedToBrackets()) return null;
        return enum_[0];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("BracketsEnum {");
        boolean first = true;
        for(Brackets expr : enum_) {
            if(first)
                first = false;
            else
                sb.append(", ");
            sb.append(expr.toString());
        }
        return sb.append("}").toString();
    }
    
    @Override
    public final ElementType getElementType() {
        return ElementType.BRACKETS_ENUM;
    }
}
