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
import roottemplate.calculator.evaluator.MoreMath;
import static roottemplate.calculator.evaluator.grapher.GrapherExpression.Opcode.*;

public class GrapherExpression {
    final int[] code;
    final double[] buffer;
    final double[] stack;
    final boolean[] gaps = new boolean[2];
    
    public GrapherExpression(int[] code, double[] buffer, int stackLength) {
        this.code = code;
        this.buffer = buffer;
        stack = new double[stackLength];
    }
    
    public int getAnalyzingExprSolversCount() {
        int result = 0;
        for(int i = 0; i < code.length; i++) {
            switch(code[i]) {
                case ODIV:
                    result += 3;
                    break;
                case OPOW:
                    result += 2;
                    break;
            }
        }
        return result;
    }
    
    public double eval(ExprSolver[][] solvers, int gapsSide) throws EvaluatorException {
        gaps[0] = false;
        gaps[1] = false;
        int stackIndex = -1;
        int solversIndex = 0;
        for(int i = 0; i < code.length; i++) {
            int insn = code[i];
            if(insn < 0)
                switch(insn) {
                    case OADD:
                        stack[stackIndex - 1] += stack[stackIndex];
                        stackIndex--;
                        break;
                    case OSUB:
                        stack[stackIndex - 1] -= stack[stackIndex];
                        stackIndex--;
                        break;
                    case OMUL:
                        stack[stackIndex - 1] *= stack[stackIndex];
                        stackIndex--;
                        break;
                    case ODIV:
                        // WARNING> TODO> EXACTLY ZERO
                        if(gapsSide != 0) {
                            ExprSolver a = solvers[0][solversIndex], b = solvers[0][solversIndex + 1];
                            if(b.was(0) >= ExprSolver.SOLUTION_MIDDLE) {
                                double result;
                                if(a.was(0) >= ExprSolver.SOLUTION_MIDDLE) {
                                    // Считаем, что этот ноль был в той же точке, что и b.was(0)
                                    //result = Double.NaN;
                                    //result = (gapsSide == 1 ? -1 : 1) * 0.0001;
                                    ExprSolver resSolver = solvers[0][solversIndex + 2];
                                    double bNext = Math.abs(b.next);
                                    double bPrev = Math.abs(b.prev);
                                    result = resSolver.next + (resSolver.next - resSolver.prev) * (bNext / (bNext + bPrev));
                                } else {
                                    int denSign = gapsSide == 1 ? (b.prev > 0 ? 1 : -1) : (b.next > 0 ? 1 : -1);
                                    int numSign = gapsSide == 1 ? (a.prev > 0 ? 1 : -1) : (a.next > 0 ? 1 : -1);
                                    result = numSign * denSign / (double) 0;
                                }
                                stack[stackIndex - 1] = result;
                                if(gapsSide == 2)
                                    solvers[0][solversIndex + 2].next(result);
                            }
                        } else {
                            double result = stack[stackIndex - 1] = stack[stackIndex - 1] / stack[stackIndex];
                            for(int t = 0; t < solvers.length; t++) {
                                // = a / b
                                ExprSolver a = solvers[t][solversIndex], b = solvers[t][solversIndex + 1];
                                a.next(stack[stackIndex - 1]);
                                b.next(stack[stackIndex]);
                                if(b.was(0) >= ExprSolver.SOLUTION_MIDDLE) {
                                    gaps[t] = true;
                                } else
                                    solvers[t][solversIndex + 2].next(result);
                            }
                        }
                        
                        stackIndex--;
                        solversIndex += 3;
                        break;
                    case OREM:
                        stack[stackIndex - 1] += stack[stackIndex];
                        stackIndex--;
                        break;
                    case OPOW:
                        if(gapsSide != 0) {
                            ExprSolver a = solvers[0][solversIndex], b = solvers[0][solversIndex + 1];
                            if(a.was(0) >= ExprSolver.SOLUTION_MIDDLE && b.was(0) >= ExprSolver.SOLUTION_MIDDLE || true) {
                                stack[stackIndex - 1] = Double.NaN;
                            }
                        } else {
                            for(int t = 0; t < solvers.length; t++) {
                                ExprSolver a = solvers[t][solversIndex], b = solvers[t][solversIndex + 1];
                                a.next(stack[stackIndex - 1]);
                                b.next(stack[stackIndex]);
                                if(a.was(0) >= ExprSolver.SOLUTION_MIDDLE && b.was(0) >= ExprSolver.SOLUTION_MIDDLE) {
                                    gaps[t] = true;
                                }
                            }
                            stack[stackIndex - 1] = Math.pow(stack[stackIndex - 1], stack[stackIndex]);
                        }
                        
                        stackIndex--;
                        solversIndex += 2;
                        break;
                    case OROOT:
                        stack[stackIndex - 1] = MoreMath.root(stack[stackIndex - 1], Math.round(stack[stackIndex]));
                        stackIndex--;
                        break;
                        
                    // TODO> EVERYTHING ELSE
                    case OABS:
                        stack[stackIndex] = Math.abs(stack[stackIndex]);
                        break;
                    
                    default:
                        throw new EvaluatorException("Unknown insn: " + insn);
                }
            else
                stack[++stackIndex] = buffer[insn];
        }
        
        return stack[0];
    }
    
    
    
    public interface Opcode {
        int OADD = -1; // 1arg, 2arg
        int OSUB = -2;
        int OMUL = -3;
        int ODIV = -4;
        int OREM = -5;
        int OPOW = -6;
        int OROOT = -7; // v, n. n-th root of number v

        int ONEG = -8; // 1arg
        int OFACT = -9;

        int OROUND = -10;
        int OFLOOR = -11;
        int OCEIL = -12;
        int O_TORAD = -12 - 1;
        int O_TODEG = -14;
        int OLOG10 = -15;
        int OLOG = -16;
        int OABS = -17;
        int OSQRT = -18;
        int OCBRT = -19;

        int OSIN = -20;
        int OCOS = -21;
        int OTAN = -22;
        int O_ASIN = -23;
        int O_ACOS = -24;
        int O_ATAN = -25;
    }
}
