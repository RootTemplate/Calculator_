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
package roottemplate.calculator.evaluator.grapher;

public class ExprSolver {
    public static final byte SOLUTION_NO = 0;
    public static final byte SOLUTION_WEAK = 1;
    public static final byte SOLUTION_MIDDLE = 2;
    public static final byte SOLUTION_STRONG = 4;
    
    public double prevDelta;
    public double prev; // difference
    public double next; // difference
    public boolean[] gapsCrossing = new boolean[2];
    
    public byte lastOppositeSolution;
    public double lastOppositeSolutionCoord;
    public boolean[] lastOppositeGapCrossing = new boolean[2];

    public ExprSolver() {
        clear();
    }

    public final void clear() {
        //prevDelta = Double.NaN;
        //prev = Double.NaN;
        next = Double.NaN;
        lastOppositeSolution = SOLUTION_NO;
        lastOppositeSolutionCoord = Double.NaN;
        //lastOppositeGapCrossing[0] = lastOppositeGapCrossing[1] = gapsCrossing[0] = gapsCrossing[1] = false;
    }
    
    private byte getSign(double n, double point) {
        if(n == point)
            return 0;
        return (byte) ((n > point) ? 1 : -1);
    }
    private double getDelta(double last, double cur) {
        double result = cur - last;
        return Math.abs(result) <= Grapher.COMPUTING_ERROR ? 0 : result;
    }
    
    public void next(double n) {
        prevDelta = next - prev;
        prev = next;
        next = n;
    }
    
    public byte was/*Crossing*/(double point) {
        byte lastSign = getSign(prev, point);
        double delta = getDelta(prev, next);
        byte sign = getSign(next, point);
        
        int result = SOLUTION_NO;
        if(next == 0)
            result += SOLUTION_WEAK;
        if(prev == 0)
            result += SOLUTION_WEAK; // Добавляем к point, а не перезаписываем его значение
        if(result == SOLUTION_NO && lastSign != -2 && /*NaN checking*/ next == next && prev == prev) {
            if(sign != lastSign && lastSign != 0)
                result = SOLUTION_STRONG;
            else {
                if(prevDelta == prevDelta && getSign(prevDelta, 0) != getSign(delta, 0) && sign != getSign(prevDelta + prev, point)) {
                    if(next > 0 && prevDelta < delta)
                        result = SOLUTION_MIDDLE;
                    else if(next < 0 && prevDelta > delta)
                        result = SOLUTION_MIDDLE;
                }
            }
        }
        
        return (byte) result;
    }
}
