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

package roottemplate.calculator.evaluator.util;

public class ExpressionFormatUpdater {
    public static final int DIGIT_GROUPING_NONE = 0;
    public static final int DIGIT_GROUPING_LEFT = 1;
    public static final int DIGIT_GROUPING_FRACTIONAL = 2;
    
    public final char digitDelimiter;
    public final char point;
    public Update updateContainer;
    private final StringBuilder tempSb = new StringBuilder();
    private int grouping;
    
    public ExpressionFormatUpdater(char digitDelimiter, char point, int grouping) {
        this.digitDelimiter = digitDelimiter;
        this.point = point;
        this.grouping = grouping;
    }
    
    private boolean doUpdateChar(char c) {
        return Character.isDigit(c) || c == point || c == digitDelimiter;
    }
    
    public Update updateDigitGrouping(CharSequence expr, int cursor, int updStart, int updEnd) {
        int cursorDelta = 0;
        byte number = -1; // -1 - not found; 0 - found but searching for point; 1 - processing number
        int toPoint = 0;
        int numberStart = -1;
        int digitsSkipped = 0;
        for(int i = updStart; i <= updEnd; i++) {
            char c = expr.charAt(i);
            if(c == digitDelimiter) {
                if(number != 0 && cursor > i)
                    cursorDelta--;
                digitsSkipped++;
                continue;
            }
            boolean isDigit = Character.isDigit(c);
            
            if(number == 0 && (!isDigit || i == updEnd)) {
                toPoint = i - numberStart - (i == updEnd && isDigit ? 0 : 1) - digitsSkipped;
                digitsSkipped = 0;
                i = numberStart - 1; // And after that i++ will be invoked
                number = 1;
            } else if(!isDigit && c != point) {
                tempSb.append(c);
                number = -1;
            } else {
                if(number == -1) {
                    numberStart = i;
                    number = 0;
                    digitsSkipped = 0;
                    
                    if(i == updEnd) {
                        i--;
                    }
                } else if(number == 1) {
                    if(i != numberStart) {
                        if(c == point)
                            toPoint = 0;
                        else if(toPoint == 0)
                            toPoint--;
                        else if(toPoint-- % 3 == 0 &&
                                (toPoint > 0 ? (grouping & DIGIT_GROUPING_LEFT) : (grouping & DIGIT_GROUPING_FRACTIONAL)) != 0) {
                            tempSb.append(digitDelimiter);
                            if(cursor > i)
                                cursorDelta++;
                        }
                    }
                    
                    tempSb.append(c);
                }
            }
        }
        
        Update result = (updateContainer != null) ? updateContainer : new Update();
        result.begin = updStart;
        result.end = updEnd + 1;
        result.replaceTo = tempSb.toString();
        result.cursor = cursor + cursorDelta;
       
        tempSb.delete(0, tempSb.length());
        return result;
    }
    
    public Update updateDigitGroupingInitial(CharSequence expr, int cursor, int initialUpdStart, int initialUpdEnd) {
        while(initialUpdStart > 0 && doUpdateChar(expr.charAt(initialUpdStart - 1))) {
            initialUpdStart--;
        }
        while(initialUpdEnd < expr.length() - 1 && doUpdateChar(expr.charAt(initialUpdEnd + 1))) {
            initialUpdEnd++;
        }

        return updateDigitGrouping(expr, cursor, initialUpdStart, initialUpdEnd);
    }
    
    public void setGrouping(int grouping) {
        if(this.grouping == grouping) return;
        this.grouping = grouping;
        //updateDigitGrouping(0, expr.length() - 1);
    }
    public int getGrouping() {
        return grouping;
    }
    public boolean doGroup() {
        return grouping > DIGIT_GROUPING_NONE;
    }
    
    public static class Update {
        private int begin;
        private int end;
        private String replaceTo;
        private int cursor;
        
        /**
         * @return begin point, inclusive
         */
        public int begin() {
            return begin;
        }
        
        /**
         * @return end point, exclusive
         */
        public int end() {
            return end;
        }
        
        /**
         * @return new cursor
         */
        public int cursor() {
            return cursor;
        }
        
        /**
         * @return new String of selected [begin, end) part
         */
        public String replaceTo() {
            return replaceTo;
        }
    }
}