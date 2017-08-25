package roottemplate.calculator.evaluator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
public class Graphbuilder {
    public static final byte SIGN_EQUALS = 1;
    
    private static final byte SOLUTION_NO_ZERO = 0;
    private static final byte SOLUTION_WEAK_ZERO = 1;
    private static final byte SOLUTION_MIDDLE_ZERO = 2;
    private static final byte SOLUTION_STRONG_ZERO = 4;
    
    public static int half(int n) {
        return Math.round(n / 2F);
    }
    
    
    public double COMPUTING_ERROR = 0.00000000001D; // 10^-11
    private final Screen scr;
    private Point[] points = null;
    private CompiledExpression expr = null;
    private Variable xVar = null;
    private Variable yVar = null;
    private byte exprSign = -1;
    private float percentComplete = 0;
    
    public Graphbuilder(Screen scr) {
        this.scr = scr;
    }
    
    public void setEquation(String expr, Evaluator namespace, String xVarName, String yVarName) throws EvaluatorException {
        Nameable x = namespace.getOther(xVarName);
        if(!(x instanceof Variable)) throw new EvaluatorException("Nameable found by x-variable's name is not a variable");
        Nameable y = namespace.getOther(yVarName);
        if(!(y instanceof Variable)) throw new EvaluatorException("Nameable found by y-variable's name is not a variable");
        setEquation(expr, namespace, (Variable) x, (Variable) y);
    }
    public void setEquation(String expr, Evaluator namespace, Variable xVar, Variable yVar) throws EvaluatorException {
        int eqIndex = expr.indexOf('=');
        if(eqIndex == -1 || expr.indexOf("=", eqIndex + 1) != -1)
            throw new EvaluatorException("Equation does not contain '=' sign or does contain more than 1 sign");
        
        exprSign = SIGN_EQUALS;
        this.expr = Expression.parse("(" + expr.substring(0, eqIndex) + ")-(" + expr.substring(eqIndex + 1) + ")", namespace).compile();
        this.xVar = xVar;
        this.yVar = yVar;
        
        onScreenSizeChanged();
    }
    
    public void onScreenSizeChanged() {
        points = new Point[scr.getWidth() * scr.getHeight()];
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
    
    private byte getSign(double n) {
        if(n == 0)
            return 0;
        return (byte) ((n > 0) ? 1 : -1);
    }
    private double getDelta(double last, double cur) {
        double result = cur - last;
        return Math.abs(result) <= COMPUTING_ERROR ? 0 : result;
    }
    
    public void genPoints() throws EvaluatorException {
        if(expr == null || xVar == null || yVar == null || exprSign == -1)
            throw new IllegalStateException("Equation must be set first");
        
        DoubleNumber x = new DoubleNumber(pxToPointX(-1));
        DoubleNumber y = new DoubleNumber(0);
        xVar.changeValue(x);
        yVar.changeValue(y);
        double xZeroError = COMPUTING_ERROR / scr.getScaleIntervalX();
        double yZeroError = COMPUTING_ERROR / scr.getScaleIntervalY();
        
        double centerFixX = 0.5D / scr.getScaleIntervalX();
        double centerFixY = -0.5D / scr.getScaleIntervalY();
        int width = scr.getWidth(), height = scr.getHeight();
        int halfWidth = (int) Math.ceil(width / 2);
        int halfHeight = (int) Math.ceil(height / 2);
        SolutionsKit[] xKits = new SolutionsKit[height + 1];
        SolutionsKit yKit = new SolutionsKit();
        
        float percentCoef = 100 / (float) (width + 1);
        percentComplete = 0;
        
        for(int i = 0; i < height + 1; i++) {
            xKits[i] = new SolutionsKit();
        }
        
        double curX, curY;
        for(int i = -1; i < width; i++) {
            curX = pxToPointX(i - halfWidth + 1) - centerFixX; // здесь нам нужны не центры пикселей, а их верхние левые границы
            x.setValue(curX);

            for(int j = -1; j < height; j++) {
                curY = pxToPointY(j - halfHeight + 1) - centerFixY;
                y.setValue(curY);
                double difference = expr.eval().doubleValue();
                double diffAbs = Math.abs(difference);

                SolutionsKit xKit = xKits[j + 1];
                
                /* +--+--+--+
                   |  |  |  |   <  - lastSolX и lastSolY    |  на оси X - solX
                   +--+<<+--+   >  - solX и solY            |  на оси Y - solY
                   |  <##>  |
                   +--+>>+--+   // - пиксель, обозначенный i и j
                   |  |  |//|   ## - текущий пиксель
                   +--+--+--+  */
                
                int pointsCount = 0, amount = 0;
                double xCoord = 0, yCoord = 0;
                
                if(xKit.lastOppositeSolution != SOLUTION_NO_ZERO) { // lastSolY
                    amount++;
                    pointsCount += xKit.lastOppositeSolution;
                    xCoord += curX - 2 * centerFixX;
                    yCoord += xKit.lastOppositeSolutionCoord;
                }
                if(yKit.lastOppositeSolution != SOLUTION_NO_ZERO) { // lastSolX
                    amount++;
                    pointsCount += yKit.lastOppositeSolution;
                    xCoord += yKit.lastOppositeSolutionCoord;
                    yCoord += curY - 2 * centerFixY;
                }
                
                double lastDiffX = xKit.lastDiff;
                double lastDiffY = yKit.lastDiff;
                byte solY = processDifference(yKit, diffAbs < yZeroError ? 0 : difference);
                byte solX = processDifference(xKit, diffAbs < xZeroError ? 0 : difference);
                // Полная формула для расчета координат графика (для x):
                // curX - 2 * centerFixX * (|diff| / (|diff| + |kit.lastDiff|)); |...| - модуль
                // Для y все то же самое, только везде X заменить на Y
                if(solX != SOLUTION_NO_ZERO) {
                    yKit.lastOppositeSolutionCoord = curX - 2 * centerFixX * (1 + diffAbs / Math.abs(lastDiffX));
                }
                if(solY != SOLUTION_NO_ZERO) {
                    xKit.lastOppositeSolutionCoord = curY - 2 * centerFixY * (1 + diffAbs / Math.abs(lastDiffY));
                }
                xKit.lastOppositeSolution = solY;
                yKit.lastOppositeSolution = solX;
                
                if(xKit.lastOppositeSolution != SOLUTION_NO_ZERO) { // solY
                    amount++;
                    pointsCount += xKit.lastOppositeSolution;
                    xCoord += curX;
                    yCoord += xKit.lastOppositeSolutionCoord;
                }
                if(yKit.lastOppositeSolution != SOLUTION_NO_ZERO) { // solX
                    amount++;
                    pointsCount += yKit.lastOppositeSolution;
                    xCoord += yKit.lastOppositeSolutionCoord;
                    yCoord += curY;
                }
                
                if(i != -1 && j != -1)
                    if(pointsCount >= SOLUTION_STRONG_ZERO) {
                        points[i * height + j] = new Point(xCoord / amount, yCoord / amount); // Среднее арифметическое
                    } else
                        points[i * height + j] = null;
            }
            
            yKit.clear();
            percentComplete = (i + 2) * percentCoef;
        }
        
        percentComplete = 100;
    }
    
    private byte processDifference(SolutionsKit kit, double diff) {
        byte lastSign = getSign(kit.lastDiff);
        double lastDelta = kit.lastDelta;
        double delta = getDelta(kit.lastDiff, diff);
        byte sign = getSign(diff);
        
        int point = SOLUTION_NO_ZERO;
        if(diff == 0)
            point += SOLUTION_WEAK_ZERO;
        if(kit.lastDiff == 0)
            point += SOLUTION_WEAK_ZERO; // Добавляем к point, а не перезаписываем его значение
        if(point == SOLUTION_NO_ZERO && lastSign != -2 && !Double.isNaN(diff) && !Double.isNaN(kit.lastDiff)) {
            if(sign != lastSign && lastSign != 0)
                point = SOLUTION_STRONG_ZERO;
            else {
                if(!Double.isNaN(lastDelta) && getSign(lastDelta) != getSign(delta) && sign != getSign(lastDelta + kit.lastDiff)) {
                    if(diff > 0 && lastDelta < delta)
                        point = SOLUTION_MIDDLE_ZERO;
                    else if(diff < 0 && lastDelta > delta)
                        point = SOLUTION_MIDDLE_ZERO;
                }
            }
        }
        
        kit.lastDelta = delta;
        kit.lastDiff = diff;
        
        return (byte) point;
    }
    
    
    
    public Point[] getPoints() {
        return points;
    }
    
    public float getPercentComplete() {
        return percentComplete;
    }
    
    
    
    private static final class SolutionsKit {
        public double lastDelta;
        public double lastDiff;
        public byte lastOppositeSolution;
        public double lastOppositeSolutionCoord;
        
        public SolutionsKit() {
            clear();
        }

        public void clear() {
            lastDelta = Double.NaN;
            lastDiff = Double.NaN;
            lastOppositeSolution = SOLUTION_NO_ZERO;
            lastOppositeSolutionCoord = Double.NaN;
        }
    }
    
    public static class Point {
        private double xCoord = Double.NaN;
        private double yCoord = Double.NaN;
        
        private Point(double x, double y) {
            this.xCoord = x;
            this.yCoord = y;
        }
        
        public double getX() { return xCoord; }
        public double getY() { return yCoord; }
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
