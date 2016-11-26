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
package roottemplate.calculator.evaluator.impls;

import java.util.ArrayList;
import roottemplate.calculator.evaluator.Brackets;
import roottemplate.calculator.evaluator.Evaluator;
import roottemplate.calculator.evaluator.EvaluatorException;
import roottemplate.calculator.evaluator.ExpressionElement;
import roottemplate.calculator.evaluator.BracketsEnum;
import roottemplate.calculator.evaluator.Reader;
import roottemplate.calculator.evaluator.util.IndexedString;
import roottemplate.calculator.evaluator.util.ListStruct;
import roottemplate.calculator.evaluator.Named;

public class BracketsReader extends Reader implements Named {
    @Override
    public ReadResult<? extends ExpressionElement> read(IndexedString expr, Evaluator namespace, int i) throws EvaluatorException {
        ArrayList<Brackets> list = new ArrayList<>();
        int j = 1;
        int bracketsOpen = 1;
        int lastExprEnd = j;
        for(; j < expr.length() && bracketsOpen > 0; j++) {
            char at = expr.charAt(j);
            if(at == '(' || at == '{' || at == '[')
                bracketsOpen++;
            else if(at == ')' || at == '}' || at == ']')
                bracketsOpen--;
            
            boolean newEntry = true;
            if(bracketsOpen != 0 /*not end of brackets*/ && (at != ',' || bracketsOpen != 1))
                newEntry = false;

            if(newEntry) {
                ListStruct ls = Reader.readExpressionElements(expr.substring(lastExprEnd, j), namespace, i + lastExprEnd);
                list.add(new Brackets(ls));
                lastExprEnd = j + 1;
            }
        }
        
        if(bracketsOpen != 0) throw new EvaluatorException("Found " + bracketsOpen + " unclosed bracket(s)");
        
        if(list.size() == 1)
            return new ReadResult<>(list.get(0), j);
        else
            return new ReadResult<>(new BracketsEnum(list.toArray(new Brackets[list.size()])), j);
    }

    @Override
    public String getName() {
        return "(";
    }
}
