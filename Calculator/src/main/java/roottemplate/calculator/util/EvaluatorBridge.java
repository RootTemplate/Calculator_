/*
 * Copyright (c) 2016 RootTemplate Group 1.
 * This file is part of Calculator_.
 *
 * Calculator_ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Calculator_ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Calculator_.  If not, see <http://www.gnu.org/licenses/>.
 */

package roottemplate.calculator.util;

import java.util.HashMap;
import java.util.Map;

import roottemplate.calculator.evaluator.Evaluator;
import roottemplate.calculator.evaluator.EvaluatorException;
import roottemplate.calculator.evaluator.Number;

public class EvaluatorBridge {
    public static final int BRACKET_CLOSING_TYPE_NO = 0;
    public static final int BRACKET_CLOSING_TYPE_IFONE = 1;
    public static final int BRACKET_CLOSING_TYPE_ALWAYS = 2;

    private static final HashMap<Character, Character> replacementMap = new HashMap<>();
    static {
        replacementMap.put('\u2212', '-');
        replacementMap.put('\u00d7', '*');
        replacementMap.put('\u00f7', '/');
    }

    public static String closeUnclosedBrackets(String expr, int closingType) {
        if(closingType == BRACKET_CLOSING_TYPE_NO) return expr;

        int bracketsOpen = countRepeats(expr, '(');
        if(bracketsOpen == 0)
            return expr;
        int closed = bracketsOpen - countRepeats(expr, ')');
        if(closed == 0 || (closingType == BRACKET_CLOSING_TYPE_IFONE && closed > 1))
            return expr;

        StringBuilder sb = new StringBuilder(expr);
        for(int i = 0; i < closed; i++)
            sb.append(')');
        return sb.toString();
    }
    private static int countRepeats(String in, char needle) {
        int i = -1;
        int count = 0;
        while((i = in.indexOf(needle, i + 1)) != -1)
            count++;
        return count;
    }

    public static String replaceAppToEngine(String expr) {
        for(char c : replacementMap.keySet()) {
            expr = expr.replace(c, replacementMap.get(c));
        }
        return expr;
    }

    public static String replaceEngineToApp(String expr) {
        for(Map.Entry e : replacementMap.entrySet()) {
            expr = expr.replace((char) e.getValue(), (char) e.getKey());
        }
        expr = expr.replaceAll("Infinity", "\u221e");
        return expr;
    }

    public static Number eval(Evaluator namespace, String expr) throws EvaluatorException {
        return namespace.process(replaceAppToEngine(expr));
    }

    public static String doubleToString(double x, int maxLen) {
        int md = Math.round(maxLen * 0.8F);
        return roottemplate.calculator.evaluator.util.Util.doubleToString(x, maxLen,
                md, md);
    }
}
