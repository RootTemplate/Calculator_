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

import roottemplate.calculator.evaluator.EvaluatorException;
import roottemplate.calculator.evaluator.impls.RealNumber;

/**
 * Real number graph builder
 * Note: graph's axises look like:
 *          ^ Y
 *          |
 *       ---+--> X
 *          |
 *          |
 * While screen's axises look like:
 *          |
 *          |
 *       ---+--> X
 *          |
 *          \/ Y
 * Graph's point (0; 0) is screen's point (0; 0)
 */
public class Grapher {
    public static final byte SIGN_EQUALS = 1;
    
    public static int half(int n) {
        return Math.round(n / 2F);
    }
    
    
    public static double COMPUTING_ERROR = 0.00000001D; // 10^-8
    private final Screen scr;
    private Point[] points;
    private boolean[] gaps;
    private GrapherExpression expr;
    private int xVarIndex = -1;
    private int yVarIndex = -1;
    private byte exprSign = -1;
    
    private boolean gapsSupported;
    
    public Grapher(Screen scr) {
        this.scr = scr;
    }
    
    /*public void setEquation(String expr, Evaluator namespace, String xVarName, String yVarName) throws EvaluatorException {
        Named x = namespace.getOther(xVarName);
        if(!(x instanceof Variable)) throw new EvaluatorException("Nameable found by x-variable's name is not a variable");
        Named y = namespace.getOther(yVarName);
        if(!(y instanceof Variable)) throw new EvaluatorException("Nameable found by y-variable's name is not a variable");
        setEquation(expr, namespace, (Variable) x, (Variable) y);
    }
    public void setEquation(String expr, Evaluator namespace, Variable xVar, Variable yVar) throws EvaluatorException {
        int eqIndex = expr.indexOf('=');
        if(eqIndex == -1 || expr.indexOf("=", eqIndex + 1) != -1)
            throw new EvaluatorException("Equation does not contain '=' sign or does contain more than 1 sign");
        
        exprSign = SIGN_EQUALS;
        this.expr = StandardExpression.createFromString("(" + expr.substring(0, eqIndex) + ")-(" + expr.substring(eqIndex + 1) + ")", namespace);
        this.xVar = xVar;
        this.yVar = yVar;
        
        onScreenSizeChanged();
    }*/
    public void setEquation(GrapherExpression expr, int xVarIndex, int yVarIndex) {
        exprSign = SIGN_EQUALS;
        this.expr = expr;
        this.xVarIndex = xVarIndex;
        this.yVarIndex = yVarIndex;
        
        onScreenSizeChanged();
    }
    
    public void onScreenSizeChanged() {
        points = new Point[scr.getWidth() * scr.getHeight()];
        gaps = new boolean[points.length];
    }
    
    /**
     * Returns x-coord of a pixel that contains given graph x-coord
     */
    public int pointXToPx(double x) {
        return ((int) Math.floor(x * scr.getScaleIntervalX())) + scr.getXOffset();
    }
    /**
     * Returns y-coord of a pixel that contains given graph y-coord
     */
    public int pointYToPx(double y) {
        return ((int) -Math.floor(y * scr.getScaleIntervalY())) + scr.getYOffset();
    }
    /**
     * Returns x-coord of a point which is located in the center of a pixel with given x-coord
     */
    public double pxToPointX(int px) {
        return (px - scr.getXOffset()) / scr.getScaleIntervalX();
    }
    /**
     * Returns y-coord of a point which is located in the center of a pixel with given y-coord
     */
    public double pxToPointY(int px) {
        return (-px + scr.getYOffset()) / scr.getScaleIntervalY();
    }
    
    
    
    public void genPoints() throws EvaluatorException {
        if(expr == null || exprSign == -1)
            throw new IllegalStateException("Equation must be set first");
        gapsSupported = true;
        
        double xZeroError = COMPUTING_ERROR / scr.getScaleIntervalX();
        double yZeroError = COMPUTING_ERROR / scr.getScaleIntervalY();
        double centerFixX = 0.5D / scr.getScaleIntervalX();
        double centerFixY = -0.5D / scr.getScaleIntervalY();
        int width = scr.getWidth(), height = scr.getHeight();
        int halfWidth = (int) Math.ceil(width / 2);
        int halfHeight = (int) Math.ceil(height / 2);
        
        ExprSolver[] xKits = new ExprSolver[height + 1];
        ExprSolver yKit = new ExprSolver();
        for(int i = 0; i < height + 1; i++) {
            xKits[i] = new ExprSolver();
        }
        
        int exprSolversCount = expr.getAnalyzingExprSolversCount();
        ExprSolver[][] exprKits = new ExprSolver[xKits.length + 1][];
        // exprKits[0] = yExprKit
        // exprKits[n > 0] = xExprKit[n - 1]
        for(int i = 0; i < exprKits.length; i++) {
            exprKits[i] = new ExprSolver[exprSolversCount];
            for(int j = 0; j < exprSolversCount; j++)
                exprKits[i][j] = new ExprSolver();
        }
        ExprSolver[][] exprKitsArg = new ExprSolver[][] {exprKits[0], null};
        ExprSolver[][] exprSingleKitArg = new ExprSolver[][] {null};
        
        System.out.println("W = " + width + "; H = " + height);
        
        double curX, curY;
        for(int i = -1; i < width; i++) {
            curX = pxToPointX(i - halfWidth + 1) - centerFixX; // здесь нам нужны не центры пикселей, а их верхние левые границы
            if(xVarIndex >= 0) expr.buffer[xVarIndex] = curX;
            
            if(i == 125)
                i += 0;

            for(int j = -1; j < height; j++) {
                curY = pxToPointY(j - halfHeight + 1) - centerFixY;
                if(yVarIndex >= 0) expr.buffer[yVarIndex] = curY;
                
                if(j == 99)
                    j += 0;
                
                ExprSolver xKit = xKits[j + 1];
                exprKitsArg[1] = exprKits[j + 2];
                double difference = expr.eval(exprKitsArg, 0);
                double diffAbs = Math.abs(difference);
                
                int pointsCount = 0, amount = 0;
                double xCoord = 0, yCoord = 0;
                
                if(expr.gaps[0] || expr.gaps[1]) {
                    System.out.println("GAP. X: " + i + " Y: " + j);
                    /*if(i > 0 && j > 0)
                        gaps[(i - 1) * height + j - 1] = true;*/
                }
                boolean exprGaps0 = expr.gaps[0], exprGaps1 = expr.gaps[1];
                
                
                /* +--+--+--+
                   |  |  |  |   <  - lastSolX и lastSolY    |  на оси X - solX
                   +--+<<+--+   >  - solX и solY            |  на оси Y - solY
                   |  <##>  |
                   +--+>>+--+   // - пиксель, обозначенный i и j
                   |  |  |//|   ## - текущий пиксель
                   +--+--+--+  */
                
                
                if(xKit.lastOppositeSolution != ExprSolver.SOLUTION_NO) { // lastSolY
                    amount++;
                    pointsCount += xKit.lastOppositeSolution;
                    xCoord += curX - 2 * centerFixX;
                    yCoord += xKit.lastOppositeSolutionCoord;
                }
                if(yKit.lastOppositeSolution != ExprSolver.SOLUTION_NO) { // lastSolX
                    amount++;
                    pointsCount += yKit.lastOppositeSolution;
                    xCoord += yKit.lastOppositeSolutionCoord;
                    yCoord += curY - 2 * centerFixY;
                }
                
                double lastDiffX = xKit.prev;
                double lastDiffY = yKit.prev;
                byte solY = 0, solX = 0;
                
                //*
                if(exprGaps0) { // Y
                    /*if(i > 0 && j != -1)
                        gaps[(i - 1) * height + j] = true;*/
                    exprSingleKitArg[0] = exprKitsArg[0];
                    double diff_ = expr.eval(exprSingleKitArg, 1);
                    yKit.next(Math.abs(diff_) < yZeroError ? 0 : diff_);
                    
                    // solY = 0
                    solY += yKit.was(0);
                    if(solY > 0) {
                        yKit.gapsCrossing[0] = true;
                    }
                    
                    yKit.clear();
                    diff_ = expr.eval(exprSingleKitArg, 2);
                    yKit.next(Math.abs(diff_) < yZeroError ? 0 : diff_);
                }
                if(exprGaps1) { // X
                    /*if(i != 0 && j > -1)
                        gaps[i * height + j - 1] = true;*/
                    exprSingleKitArg[0] = exprKitsArg[1];
                    double diff_ = expr.eval(exprSingleKitArg, 1);
                    xKit.next(Math.abs(diff_) < xZeroError ? 0 : diff_);
                    
                    // solX = 0
                    solX += xKit.was(0);
                    if(solX > 0) {
                        xKit.gapsCrossing[0] = true;
                    }
                    
                    xKit.clear();
                    diff_ = expr.eval(exprSingleKitArg, 2);
                    xKit.next(Math.abs(diff_) < xZeroError ? 0 : diff_);
                }
                //*/
                
                byte temp;
                yKit.next(diffAbs < yZeroError ? 0 : difference);
                xKit.next(diffAbs < xZeroError ? 0 : difference);
                
                temp = yKit.was(0);
                if(temp > 0 && exprGaps0)
                    yKit.gapsCrossing[1] = true;
                solY += temp;
                
                temp = xKit.was(0);
                if(temp > 0 && exprGaps1)
                    xKit.gapsCrossing[1] = true;
                solX += temp;
                
                
                // Полная формула для расчета координат графика (для x):
                // curX - 2 * centerFixX * (|diff| / (|diff| + |kit.lastDiff|)); |...| - модуль
                // Для y все то же самое, только везде X нужно заменить на Y
                if(solX != ExprSolver.SOLUTION_NO) {
                    yKit.lastOppositeSolutionCoord = curX - 2 * centerFixX * (1 + diffAbs / Math.abs(lastDiffX));
                }
                if(solY != ExprSolver.SOLUTION_NO) {
                    xKit.lastOppositeSolutionCoord = curY - 2 * centerFixY * (1 + diffAbs / Math.abs(lastDiffY));
                }
                xKit.lastOppositeSolution = solY;
                yKit.lastOppositeSolution = solX;
                
                if(xKit.lastOppositeSolution != ExprSolver.SOLUTION_NO) { // solY
                    amount++;
                    pointsCount += xKit.lastOppositeSolution;
                    xCoord += curX;
                    yCoord += xKit.lastOppositeSolutionCoord;
                }
                if(yKit.lastOppositeSolution != ExprSolver.SOLUTION_NO) { // solX
                    amount++;
                    pointsCount += yKit.lastOppositeSolution;
                    xCoord += yKit.lastOppositeSolutionCoord;
                    yCoord += curY;
                }
                
                boolean gap = false;
                if(xKit.gapsCrossing[0] != yKit.lastOppositeGapCrossing[0] && xKit.gapsCrossing[0] != xKit.gapsCrossing[1] &&
                        yKit.lastOppositeGapCrossing[0] != yKit.lastOppositeGapCrossing[1])
                    gap = true;
                else if(xKit.gapsCrossing[0] == xKit.gapsCrossing[1] && xKit.gapsCrossing[0] == true)
                    gapsSupported = false;
                
                if(yKit.gapsCrossing[0] != xKit.lastOppositeGapCrossing[0] && yKit.gapsCrossing[0] != yKit.gapsCrossing[1] &&
                        xKit.lastOppositeGapCrossing[0] != xKit.lastOppositeGapCrossing[1])
                    gap = true;
                else if(yKit.gapsCrossing[0] == yKit.gapsCrossing[1] && yKit.gapsCrossing[0] == true)
                    gapsSupported = false;
                
                boolean[] temp1 = xKit.lastOppositeGapCrossing;
                temp1[0] = temp1[1] = false;
                xKit.lastOppositeGapCrossing = yKit.gapsCrossing;
                yKit.gapsCrossing = temp1;
                
                temp1 = yKit.lastOppositeGapCrossing;
                temp1[0] = temp1[1] = false;
                yKit.lastOppositeGapCrossing = xKit.gapsCrossing;
                xKit.gapsCrossing = temp1;
                
                if(i != -1 && j != -1) {
                    int coord = i * height + j;
                    gaps[coord] = gap;
                    if(pointsCount >= ExprSolver.SOLUTION_STRONG) {
                        points[coord] = new Point(xCoord / amount, yCoord / amount); // Среднее арифметическое
                        if(i == 125)
                            i += 0;
                    } else
                        points[coord] = null;
                }
            }
            
            yKit.clear();
            yKit.lastOppositeGapCrossing[0] = yKit.lastOppositeGapCrossing[1] = yKit.gapsCrossing[0] = yKit.gapsCrossing[1] = false;
            for(ExprSolver es : exprKits[0])
                es.clear();
        }
    }
    
    
    
    public Point[] getPoints() {
        return points;
    }
    
    public boolean[] getGaps() {
        return gaps;
    }
    
    public boolean isGapsSupported() {
        return gapsSupported;
    }
    
    
    
    private static class DoubleNumber extends RealNumber {
        public DoubleNumber(double n) {
            super(n, true);
        }
        
        @Override
        public void setValue(double n) {
            super.setValue(n);
        }
        
        @Override
        public void addDelta(double delta) {
            super.addDelta(delta);
        }
    }
    
    public static interface Screen {

        /**
         * @return width in screen's pixels
         */
        int getWidth();

        /**
         * @return height in screen's pixels
         */
        int getHeight();

        /**
         * @return offset of point x=0 from the screen's center in pixels
         */
        int getXOffset();

        /**
         * @return offset of point y=0 from the screen's center in pixels. (Y-axis is directed downward)
         */
        int getYOffset();

        /**
         * @return how many pixels accommodates an X-axis unit
         */
        double getScaleIntervalX();

        /**
         * @return how many pixels accommodates an Y-axis unit
         */
        double getScaleIntervalY();
    }
}
