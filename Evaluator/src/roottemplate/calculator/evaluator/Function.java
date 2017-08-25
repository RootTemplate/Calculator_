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

import roottemplate.calculator.evaluator.PriorityManager.PriorityStorage;
import roottemplate.calculator.evaluator.util.IndexedString;

public abstract class Function extends Operator {
    public static final String PRIORITY_NAME_BRACKETS = "F()";
    public static final String PRIORITY_NAME_NO_BRACKETS = "F";
    public static final boolean PRIORITY_LEFT_DIRECTION = false;
    public static final Uses FUNCTION_USES = Uses.RIGHT_NUMBER_ENUM;

    protected final PriorityStorage prStNoBrackets;
    protected final int argsCount;
    
    public Function(PriorityManager prManager, String name, int argsCount) {
        this(prManager, name, name, argsCount);
    }
    public Function(PriorityManager prManager, String realName, String name, int argsCount) {
        this(
                prManager.getPriorityStorageByFriendlyName(PRIORITY_NAME_BRACKETS),
                prManager.getPriorityStorageByFriendlyName(PRIORITY_NAME_NO_BRACKETS),
                realName, name, argsCount
        );
    }
    public Function(PriorityStorage prStBrackets, PriorityStorage prStNoBrackets, String realName, String name,
                    int argsCount) {
        super(prStBrackets, realName, name, FUNCTION_USES);
        this.prStNoBrackets = prStNoBrackets;
        this.argsCount = argsCount;
    }
    protected Function(Function bracketCopy) {
        super(bracketCopy.prStNoBrackets, bracketCopy.realName, bracketCopy.name, bracketCopy.uses);
        prStNoBrackets = null;
        this.argsCount = bracketCopy.argsCount;
    }

    @Override
    public Number eval(Number... numbers) throws EvaluatorException {
        if(argsCount != -1 && numbers.length != argsCount)
            throw new EvaluatorException("Expected " + argsCount + " but found "
                    + numbers.length + " arguments after function \"" + name + "\"");
        return eval0(numbers);
    }
    protected abstract Number eval0(Number... numbers) throws EvaluatorException;

    protected abstract Function copyForNoBrackets();

    @Override
    public Operator checkUses(Object before, IndexedString expr, boolean exactlyThis) {
        if(!exactlyThis) {
            int startingIndex = expr.index;
            boolean brackets = false;
            while(!expr.isEmpty()) {
                char at = expr.charAt();
                if(Character.isWhitespace(at))
                    expr.index++;
                else {
                    expr.index = startingIndex;
                    brackets = at == '(';
                    break;
                }
            }

            return brackets ? this : copyForNoBrackets();
        }
        return this;
    }

    @Override
    public String toString() {
        return "Function {name: " + name + ", ";
    }
}
