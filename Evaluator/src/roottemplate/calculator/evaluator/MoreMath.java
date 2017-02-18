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

/* !!!SOME!!! OF THE CODE BELOW HAS BEEN TAKEN FROM Arity PROJECT BY Javia */
/* PREVIOUS CLASS LOCATION: org.javia.arity.MoreMath. License: Apache License v. 2 */
/* Javia's Copyright (C) 2007-2008 Mihai Preda. */
/* http://javia.org. SOURCE: https://github.com/Xlythe/Arity/blob/master/src/org/javia/arity/MoreMath.java */
public class MoreMath {
    static final double GAMMA[] = {
        57.156235665862923517,
        -59.597960355475491248,
        14.136097974741747174,
        -0.49191381609762019978,
        .33994649984811888699e-4,
        .46523628927048575665e-4,
        -.98374475304879564677e-4,
        .15808870322491248884e-3,
        -.21026444172410488319e-3,
        .21743961811521264320e-3,
        -.16431810653676389022e-3,
        .84418223983852743293e-4,
        -.26190838401581408670e-4,
        .36899182659531622704e-5
    };
 
    public static double lgamma(double x) {
        double tmp = x + 5.2421875; //== 607/128. + .5;
        double sum = 0.99999999999999709182;
        for (int i = 0; i < GAMMA.length; ++i) {
            sum += GAMMA[i] / ++x;
        }

        return 0.9189385332046727418 //LN_SQRT2PI, ln(sqrt(2*pi))
            + Math.log(sum)
            + (tmp-4.7421875)*Math.log(tmp) - tmp
            ;
    }

    static final double FACT[] = {
        1.0,
        40320.0,
        2.0922789888E13,
        6.204484017332394E23,
        2.631308369336935E35,
        8.159152832478977E47,
        1.2413915592536073E61,
        7.109985878048635E74,
        1.2688693218588417E89,
        6.1234458376886085E103,
        7.156945704626381E118,
        1.8548264225739844E134,
        9.916779348709496E149,
        1.0299016745145628E166,
        1.974506857221074E182,
        6.689502913449127E198,
        3.856204823625804E215,
        3.659042881952549E232,
        5.5502938327393044E249,
        1.3113358856834524E267,
        4.7147236359920616E284,
        2.5260757449731984E302,
    };

    public static double factorial(double x) {
        if (x < 0) { // x <= -1 ?
            return Double.NaN;
        }
        if (x <= 170) {
            if (Math.floor(x) == x) {
                int n = (int)x;
                double extra = x;
                switch (n & 7) {
                case 7: extra *= --x;
                case 6: extra *= --x;
                case 5: extra *= --x;
                case 4: extra *= --x;
                case 3: extra *= --x;
                case 2: extra *= --x;
                case 1: return FACT[n >> 3] * extra;
                case 0: return FACT[n >> 3];
                }
            }
        }
        return Math.exp(lgamma(x));
    }
    
    private static boolean isPiMultiple(double x) {
        // x % y == 0
        final double d = x / Math.PI;
        return d == Math.floor(d);
    }

    public static double sin(double x) {
        return isPiMultiple(x) ? 0 : Math.sin(x);
    }

    public static double cos(double x) {
        return isPiMultiple(x-Math.PI/2) ? 0 : Math.cos(x);
    }

    public static double tan(double x) {
        if(isPiMultiple(x)) return 0;
        if(isPiMultiple(x-Math.PI/2))
            return x > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
        return Math.tan(x);
    }

    public static double root(double number, long round) {
        if(round == 2)
            return Math.sqrt(number);
        if(round == 3)
            return Math.cbrt(number);
        if(number < 0) {
            if(round % 2 == 0)
                return Double.NaN;
            return -Math.pow(-number, 1 / (double) round);
        } else
            return Math.pow(number, 1 / (double) round);
    }

    public static double log(double n, double base) {
        return Math.log(n) / Math.log(base);
    }
}
